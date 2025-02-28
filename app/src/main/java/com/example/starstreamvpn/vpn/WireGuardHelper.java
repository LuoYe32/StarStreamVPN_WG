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

//    public void connectToVpn(TunnelModel tunnelModel) throws UnknownHostException, BadConfigException {
//        Intent intentPrepare = GoBackend.VpnService.prepare(context);
//        if (intentPrepare != null) {
//            context.startActivity(intentPrepare);
//            return;
//        }
//
//        Interface.Builder interfaceBuilder = new Interface.Builder();
//        interfaceBuilder.addDnsServer(InetAddress.getByName("10.2.0.1"));
//
//        Peer.Builder peerBuilder = new Peer.Builder();
//        peerBuilder.setPersistentKeepalive(25);
//
//        AsyncTask.execute(() -> {
//            try {
//                backend.setState(tunnel, Tunnel.State.UP, new Config.Builder()
//                        .setInterface(interfaceBuilder
//                                .addAddress(InetNetwork.parse(tunnelModel.IP))
//                                .parsePrivateKey(tunnelModel.privateKey)
//                                .build())
//                        .addPeer(peerBuilder
//                                .addAllowedIps(tunnelModel.allowedIPs)
//                                .setEndpoint(InetEndpoint.parse(tunnelModel.endpoint))
//                                .parsePublicKey(tunnelModel.publicKey)
//                                .build())
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
public void connectToVpn(TunnelModel tunnelModel) throws UnknownHostException, BadConfigException, ParseException {
    Intent intentPrepare = GoBackend.VpnService.prepare(context);
    if (intentPrepare != null) {
        context.startActivity(intentPrepare);
        return;
    }

    Interface.Builder interfaceBuilder = new Interface.Builder();
    interfaceBuilder.addDnsServer(InetAddress.getByName("10.2.0.1")) // DNS
            .addAddress(InetNetwork.parse("10.2.0.2/32")) // IP Address
            .parsePrivateKey("aHMSvexwq8EEf8DLuQXO7KF+1lYknfX2JULh4pjhGEc="); // PrivateKey

    // Создаем peer с хардкодим значениями
    Peer.Builder peerBuilder = new Peer.Builder();
    peerBuilder.setPersistentKeepalive(25) // PersistentKeepalive
            .addAllowedIps(Collections.singleton(InetNetwork.parse("0.0.0.0/0")))// Allowed IPs
            .setEndpoint(InetEndpoint.parse("169.150.218.55:51820")) // Endpoint
            .parsePublicKey("+bLlZyXzg3fqOcI7d41IYI4LON2+oDm3Yv6y8lNQWE4="); // PublicKey

    // Выполнение в отдельном потоке для асинхронного подключения
    AsyncTask.execute(() -> {
        try {
            // Сетап подключения
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

}
