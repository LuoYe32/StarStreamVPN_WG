package com.example.starstreamvpn.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.starstreamvpn.R;
import com.example.starstreamvpn.model.PersistentConnectionProperties;
import com.example.starstreamvpn.model.TunnelModel;
import com.example.starstreamvpn.vpn.WireGuardHelper;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView tvCurrentConfig, tvVpnStatus;
    private Button btnToggleVpn, btnConfig;
    private WireGuardHelper wireGuardHelper;
    private SharedPreferences prefs;
    private boolean isVpnConnected = false; // Храним текущее состояние VPN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация UI
        tvCurrentConfig = findViewById(R.id.tvCurrentConfig);
        tvVpnStatus = findViewById(R.id.tvVpnStatus);
        btnToggleVpn = findViewById(R.id.btnConnect); // Переименуем для логики вкл/выкл
        btnConfig = findViewById(R.id.btnConfig);
        prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE);

        // Инициализация WireGuard
        try {
            GoBackend backend = PersistentConnectionProperties.getInstance().getBackend();
            backend.getRunningTunnelNames();
        } catch (NullPointerException e) {
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(this));
        }

        wireGuardHelper = new WireGuardHelper(this);

        // Загружаем текущую конфигурацию
        loadCurrentConfig();

        // Устанавливаем обработчик на кнопку (переключение VPN)
        btnToggleVpn.setOnClickListener(v -> toggleVpn());

        btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigListActivity.class);
            startActivity(intent);
        });
    }

    // Метод для переключения VPN (подключение/отключение)
    private void toggleVpn() {
        if (isVpnConnected) {
            wireGuardHelper.disconnectFromVpn();
            isVpnConnected = false;
            updateVpnStatusUI(false);
        } else {
            TunnelModel tunnelModel = new TunnelModel();
            tunnelModel.privateKey = "CGYeL7uG0mr1wmvUqzkXHE0689Kk09+17ymfSfRFlXI=";
            tunnelModel.IP = "10.2.0.2/32";
            tunnelModel.endpoint = "185.177.125.174:51820";
            tunnelModel.publicKey = "jDrSMRMm6XJgYGcSd01QqJbYiW13dyaOKp8GWDs0rTQ=";
            wireGuardHelper.connectToVpn(tunnelModel);
            isVpnConnected = true;
            updateVpnStatusUI(true);
        }
    }

    // Обновляем UI в зависимости от статуса VPN
    private void updateVpnStatusUI(boolean isConnected) {
        if (isConnected) {
            tvVpnStatus.setText("VPN Статус: Подключено ✅");
            btnToggleVpn.setText("Отключить VPN");
            Toast.makeText(this, "VPN подключен", Toast.LENGTH_SHORT).show();
        } else {
            tvVpnStatus.setText("VPN Статус: Отключено ❌");
            btnToggleVpn.setText("Подключить VPN");
            Toast.makeText(this, "VPN отключен", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentConfig() {
        String server = prefs.getString("current_server", "Не выбрано");
        String port = prefs.getString("current_port", "");
        tvCurrentConfig.setText(server.equals("Не выбрано") ? "Текущая конфигурация: Не выбрано"
                : "Текущая конфигурация:\n" + server + ":" + port);
    }

}