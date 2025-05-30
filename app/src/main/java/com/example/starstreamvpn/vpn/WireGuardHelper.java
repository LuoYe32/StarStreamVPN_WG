package com.example.starstreamvpn.vpn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.starstreamvpn.model.PersistentConnectionProperties;
import com.example.starstreamvpn.model.TunnelModel;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.BadConfigException;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Interface;
import com.wireguard.config.ParseException;
import com.wireguard.config.Peer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WireGuardHelper {

    private Context context;
    private GoBackend backend;
    private Tunnel tunnel;

    public WireGuardHelper(Context context) {
        this.context = context;
        this.backend = PersistentConnectionProperties.getInstance().getBackend();
        this.tunnel = PersistentConnectionProperties.getInstance().getTunnel();
    }

    public void connectToVpn() throws Exception {
        TunnelModel tunnelModel = loadActiveConfig();

        Intent intentPrepare = GoBackend.VpnService.prepare(context);
        if (intentPrepare != null) {
            context.startActivity(intentPrepare);
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();

                Interface.Builder interfaceBuilder = new Interface.Builder();
                interfaceBuilder
                        .addDnsServer(InetAddress.getByName(tunnelModel.dns))
                        .addAddress(InetNetwork.parse(tunnelModel.IP))
                        .parsePrivateKey(tunnelModel.privateKey);

                Peer.Builder peerBuilder = new Peer.Builder();
                peerBuilder.setPersistentKeepalive(25)
                        .addAllowedIps(tunnelModel.allowedIPs)
                        .setEndpoint(InetEndpoint.parse(tunnelModel.endpoint))
                        .parsePublicKey(tunnelModel.publicKey);

                if (tunnelModel.isPQVPN) {
                    KyberClient kyberClient = new KyberClient(context.getApplicationContext());
                    String psk = kyberClient.generateWireGuardPSK();
                    Log.d("WireGuardHelper", "Сгенерированный PSK: " + psk);
                    peerBuilder.parsePreSharedKey(psk);
                }

                backend.setState(tunnel, Tunnel.State.UP, new Config.Builder()
                        .setInterface(interfaceBuilder.build())
                        .addPeer(peerBuilder.build())
                        .build());

                long endTime = System.currentTimeMillis();
                long connectionTime = endTime - startTime;

                Log.d("WireGuardHelper", "⏱️ Время подключения к VPN: " + connectionTime + " мс");


                runOnUiThread(() ->
                        Toast.makeText(context, "Подключение к VPN...", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(context, "Ошибка подключения: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(runnable);
        } else {
            Log.w("WireGuardHelper", "Context is not an Activity. UI update skipped.");
        }
    }


    public void disconnectFromVpn() {
        AsyncTask.execute(() -> {
            try {
                backend.setState(tunnel, Tunnel.State.DOWN, null);

                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "VPN отключен", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Ошибка отключения: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private TunnelModel loadActiveConfig() {
        SharedPreferences prefs = context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE);

        TunnelModel tunnelModel = new TunnelModel();
        tunnelModel.privateKey = prefs.getString("current_private_key", "");
        tunnelModel.IP = prefs.getString("current_address", "");
        tunnelModel.dns = prefs.getString("current_dns", "");
        tunnelModel.endpoint = prefs.getString("current_server", "") + ":" + prefs.getString("current_port", "");
        tunnelModel.publicKey = prefs.getString("current_public_key", "");
        tunnelModel.isPQVPN = prefs.getString("current_is_pqvpn", "0").equals("1");

        String allowedIpsStr = prefs.getString("current_allowed_ips", "");
        for (String ip : allowedIpsStr.split(",")) {
            try {
                tunnelModel.allowedIPs.add(InetNetwork.parse(ip.trim()));
            } catch (Exception ignored) {}
        }

        return tunnelModel;
    }

}
