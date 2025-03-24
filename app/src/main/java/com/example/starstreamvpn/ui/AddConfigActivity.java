package com.example.starstreamvpn.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.starstreamvpn.R;

import java.util.HashSet;
import java.util.Set;

public class AddConfigActivity extends AppCompatActivity {

    private EditText etConfigName, etServerIP, etServerPort, etPrivateKey, etPublicKey, etAddress,
            etDNS, etAllowedIPs, etPersistentKeepalive, etMTU, etPreSharedKey;
    private Button btnSaveConfig, btnBack;
    private SharedPreferences prefs;
    private String oldConfig = null; // Храним старую конфигурацию

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_config);

        etConfigName = findViewById(R.id.etConfigName);
        etServerIP = findViewById(R.id.etServerIP);
        etServerPort = findViewById(R.id.etServerPort);
        etPrivateKey = findViewById(R.id.etPrivateKey);
        etPublicKey = findViewById(R.id.etPublicKey);
        etAddress = findViewById(R.id.etAddress);
        etDNS = findViewById(R.id.etDNS);
        etAllowedIPs = findViewById(R.id.etAllowedIPs);
        etPersistentKeepalive = findViewById(R.id.etPersistentKeepalive);
        etMTU = findViewById(R.id.etMTU);
        etPreSharedKey = findViewById(R.id.etPreSharedKey);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);
        btnBack = findViewById(R.id.btnBack);
        prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE);

        // Проверяем, передана ли конфигурация для редактирования
        if (getIntent().hasExtra("config_data")) {
            oldConfig = getIntent().getStringExtra("config_data");
            assert oldConfig != null;
            loadConfigData(oldConfig);
        }

        btnSaveConfig.setOnClickListener(v -> saveConfig());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadConfigData(String config) {
        String[] parts = config.split(":");
        if (parts.length == 11) {
            etConfigName.setText(parts[0]);
            etServerIP.setText(parts[1]);
            etServerPort.setText(parts[2]);
            etPrivateKey.setText(parts[3]);
            etPublicKey.setText(parts[4]);
            etAddress.setText(parts[5]);
            etDNS.setText(parts[6]);
            etAllowedIPs.setText(parts[7]);
            etPersistentKeepalive.setText(parts[8]);
            etMTU.setText(parts[9]);
            etPreSharedKey.setText(parts[10]);
        }
    }

    private void saveConfig() {
        String configName = etConfigName.getText().toString().trim();
        String serverIP = etServerIP.getText().toString().trim();
        String serverPort = etServerPort.getText().toString().trim();
        String privateKey = etPrivateKey.getText().toString().trim();
        String publicKey = etPublicKey.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dns = etDNS.getText().toString().trim();
        String allowedIPs = etAllowedIPs.getText().toString().trim();
        String persistentKeepalive = etPersistentKeepalive.getText().toString().trim();
        String mtu = etMTU.getText().toString().trim().isEmpty() ? "0" : etMTU.getText().toString().trim();
        String preSharedKey = etPreSharedKey.getText().toString().trim().isEmpty() ? "none" : etPreSharedKey.getText().toString().trim();

        if (configName.isEmpty() || serverIP.isEmpty() || serverPort.isEmpty() || privateKey.isEmpty() || publicKey.isEmpty()) {
            Toast.makeText(this, "Введите все данные", Toast.LENGTH_SHORT).show();
            return;
        }

        String newConfig = configName + ":" + serverIP + ":" + serverPort + ":" + privateKey + ":" +
                publicKey + ":" + address + ":" + dns + ":" + allowedIPs + ":" +
                persistentKeepalive + ":" + mtu + ":" + preSharedKey;

        Set<String> savedConfigs = prefs.getStringSet("config_list", new HashSet<>());
        savedConfigs = new HashSet<>(savedConfigs);

        if (oldConfig != null) {
            savedConfigs.remove(oldConfig);
        }

        savedConfigs.add(newConfig);

        prefs.edit().putStringSet("config_list", savedConfigs).apply();

        // ✅ Устанавливаем новую конфигурацию активной
        prefs.edit()
                .putString("current_config_name", configName)
                .putString("current_server", serverIP)
                .putString("current_port", serverPort)
                .putString("current_private_key", privateKey)
                .putString("current_public_key", publicKey)
                .putString("current_address", address)
                .putString("current_dns", dns)
                .putString("current_allowed_ips", allowedIPs)
                .putString("current_persistent_keepalive", persistentKeepalive)
                .putString("current_mtu", mtu)
                .putString("current_pre_shared_key", preSharedKey)
                .apply();

        Toast.makeText(this, "Конфигурация сохранена", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AddConfigActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
