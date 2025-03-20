package com.example.starstreamvpn.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.starstreamvpn.R;
import com.example.starstreamvpn.model.PersistentConnectionProperties;
import com.example.starstreamvpn.model.TunnelModel;
import com.example.starstreamvpn.vpn.WireGuardHelper;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.config.BadConfigException;
import com.wireguard.config.ParseException;

import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView tvCurrentConfig, tvVpnStatus;
    private Button btnToggleVpn, btnConfig;
    private WireGuardHelper wireGuardHelper;
    private SharedPreferences prefs;
    private boolean isVpnConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrentConfig = findViewById(R.id.tvCurrentConfig);
        tvVpnStatus = findViewById(R.id.tvVpnStatus);
        btnToggleVpn = findViewById(R.id.btnConnect);
        btnConfig = findViewById(R.id.btnConfig);
        prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE);

        try {
            GoBackend backend = PersistentConnectionProperties.getInstance().getBackend();
            if (backend == null) {
                PersistentConnectionProperties.getInstance().setBackend(new GoBackend(this));
            }
        } catch (Exception e) {
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(this));
        }

        wireGuardHelper = new WireGuardHelper(this);

        loadCurrentConfig();

        btnToggleVpn.setOnClickListener(v -> {
            try {
                toggleVpn();
            } catch (BadConfigException | ParseException e) {
                throw new RuntimeException(e);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });

        btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigListActivity.class);
            startActivity(intent);
        });
    }

    private void toggleVpn() throws BadConfigException, UnknownHostException, ParseException {
        if (isVpnConnected) {
            wireGuardHelper.disconnectFromVpn();
            isVpnConnected = false;
            updateVpnStatusUI(false);
        } else {
            wireGuardHelper.connectToVpn();
            isVpnConnected = true;
            updateVpnStatusUI(true);
        }
    }


    private void updateVpnStatusUI(boolean isConnected) {
        tvVpnStatus.setText(isConnected ? "VPN Статус: Подключено ✅" : "VPN Статус: Отключено ❌");
        btnToggleVpn.setText(isConnected ? "Отключить VPN" : "Подключить VPN");
        Toast.makeText(this, isConnected ? "VPN подключен" : "VPN отключен", Toast.LENGTH_SHORT).show();
    }

    private void loadCurrentConfig() {
        String configName = prefs.getString("current_config_name", "Не выбрано");
        String server = prefs.getString("current_server", "");
        String port = prefs.getString("current_port", "");

        if (configName.equals("Не выбрано")) {
            tvCurrentConfig.setText("Текущая конфигурация: Не выбрано");
        } else {
            tvCurrentConfig.setText(configName + "\n" + server + ":" + port);
        }
    }
    private TunnelModel getCurrentTunnelConfig() {
        String server = prefs.getString("current_server", null);
        String port = prefs.getString("current_port", null);
        String privateKey = prefs.getString("current_private_key", null);
        String publicKey = prefs.getString("current_public_key", null);

        if (server == null || port == null || privateKey == null || publicKey == null) {
            return null;
        }

        TunnelModel tunnelModel = new TunnelModel();
        tunnelModel.privateKey = privateKey;
        tunnelModel.IP = "10.2.0.2/32";
        tunnelModel.endpoint = server + ":" + port;
        tunnelModel.publicKey = publicKey;

        return tunnelModel;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentConfig();
    }

}