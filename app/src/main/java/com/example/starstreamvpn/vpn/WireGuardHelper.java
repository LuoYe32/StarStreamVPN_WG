package com.example.starstreamvpn.vpn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.starstreamvpn.model.PersistentConnectionProperties;
import com.example.starstreamvpn.model.TunnelModel;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Interface;
import com.wireguard.config.Peer;

public class WireGuardHelper {

    private Context context;
    private GoBackend backend;
    private Tunnel tunnel;

    public WireGuardHelper(Context context) {
        this.context = context;
        this.backend = PersistentConnectionProperties.getInstance().getBackend();
        this.tunnel = PersistentConnectionProperties.getInstance().getTunnel();
    }

    public void connectToVpn(TunnelModel tunnelModel) {
        Intent intentPrepare = GoBackend.VpnService.prepare(context);
        if (intentPrepare != null) {
            context.startActivity(intentPrepare);
        }

        Interface.Builder interfaceBuilder = new Interface.Builder();
        Peer.Builder peerBuilder = new Peer.Builder();

        AsyncTask.execute(() -> {
            try {
                backend.setState(tunnel, Tunnel.State.UP, new Config.Builder()
                        .setInterface(interfaceBuilder
                                .addAddress(InetNetwork.parse(tunnelModel.IP))
                                .parsePrivateKey(tunnelModel.privateKey)
                                .build())
                        .addPeer(peerBuilder
                                .addAllowedIps(tunnelModel.allowedIPs)
                                .setEndpoint(InetEndpoint.parse(tunnelModel.endpoint))
                                .parsePublicKey(tunnelModel.publicKey)
                                .build())
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

}
