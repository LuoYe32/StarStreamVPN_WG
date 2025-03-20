package com.example.starstreamvpn.vpn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

public class WireGuardHelper {

    private Context context;
    private GoBackend backend;
    private Tunnel tunnel;

    public WireGuardHelper(Context context) {
        this.context = context;
        this.backend = PersistentConnectionProperties.getInstance().getBackend();
        this.tunnel = PersistentConnectionProperties.getInstance().getTunnel();
    }

//    public void connectToVpn(TunnelModel tunnelModel) throws UnknownHostException, BadConfigException, ParseException {
//        Intent intentPrepare = GoBackend.VpnService.prepare(context);
//        if (intentPrepare != null) {
//            context.startActivity(intentPrepare);
//            return;
//        }
//
//        Interface.Builder interfaceBuilder = new Interface.Builder();
//        interfaceBuilder.addDnsServer(InetAddress.getByName("10.2.0.1"))
//                .addAddress(InetNetwork.parse("10.2.0.2/32")) // IP Address
//                .parsePrivateKey("aHMSvexwq8EEf8DLuQXO7KF+1lYknfX2JULh4pjhGEc="); // PrivateKey
//
//        Peer.Builder peerBuilder = new Peer.Builder();
//        peerBuilder.setPersistentKeepalive(25) // PersistentKeepalive
//                .addAllowedIps(Collections.singleton(InetNetwork.parse("0.0.0.0/0")))// Allowed IPs
//                .setEndpoint(InetEndpoint.parse("169.150.218.55:51820")) // Endpoint
//                .parsePublicKey("+bLlZyXzg3fqOcI7d41IYI4LON2+oDm3Yv6y8lNQWE4="); // PublicKey
//
//        AsyncTask.execute(() -> {
//            try {
//                backend.setState(tunnel, Tunnel.State.UP, new Config.Builder()
//                        .setInterface(interfaceBuilder.build())
//                        .addPeer(peerBuilder.build())
//                        .build());
//
//                ((Activity) context).runOnUiThread(() ->
//                        Toast.makeText(context, "Подключение к VPN...", Toast.LENGTH_SHORT).show()
//                );
//            } catch (Exception e) {
//                e.printStackTrace();
//                ((Activity) context).runOnUiThread(() ->
//                        Toast.makeText(context, "Ошибка подключения: " + e.getMessage(), Toast.LENGTH_LONG).show()
//                );
//            }
//        });
//    }

    public void connectToVpn() throws UnknownHostException, BadConfigException, ParseException {
        TunnelModel tunnelModel = loadActiveConfig(); // Загружаем активную конфигурацию

        Intent intentPrepare = GoBackend.VpnService.prepare(context);
        if (intentPrepare != null) {
            context.startActivity(intentPrepare);
            return;
        }

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

        AsyncTask.execute(() -> {
            try {
                backend.setState(tunnel, Tunnel.State.UP, new Config.Builder()
                        .setInterface(interfaceBuilder.build())
                        .addPeer(peerBuilder.build())
                        .build());

                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Подключение к VPN...", Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Ошибка подключения: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
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

        String allowedIpsStr = prefs.getString("current_allowed_ips", "");
        for (String ip : allowedIpsStr.split(",")) {
            try {
                tunnelModel.allowedIPs.add(InetNetwork.parse(ip.trim()));
            } catch (Exception ignored) {}
        }

        return tunnelModel;
    }



}
