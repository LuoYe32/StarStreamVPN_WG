package com.example.starstreamvpn.ui;

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

    private EditText etConfigName, etServerIP, etServerPort, etPrivateKey, etPublicKey;
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
        if (parts.length == 5) {
            etConfigName.setText(parts[0]);
            etServerIP.setText(parts[1]);
            etServerPort.setText(parts[2]);
            etPrivateKey.setText(parts[3]);
            etPublicKey.setText(parts[4]);
        }
    }

    private void saveConfig() {
        String configName = etConfigName.getText().toString().trim();
        String serverIP = etServerIP.getText().toString().trim();
        String serverPort = etServerPort.getText().toString().trim();
        String privateKey = etPrivateKey.getText().toString().trim();
        String publicKey = etPublicKey.getText().toString().trim();

        if (configName.isEmpty() || serverIP.isEmpty() || serverPort.isEmpty() || privateKey.isEmpty() || publicKey.isEmpty()) {
            Toast.makeText(this, "Введите все данные", Toast.LENGTH_SHORT).show();
            return;
        }

        String newConfig = configName + ":" + serverIP + ":" + serverPort + ":" + privateKey + ":" + publicKey;

        Set<String> savedConfigs = prefs.getStringSet("config_list", new HashSet<>());
        savedConfigs = new HashSet<>(savedConfigs); // Создаем новый объект, чтобы избежать мутации

        // Если редактируем, удаляем старую конфигурацию
        if (oldConfig != null) {
            savedConfigs.remove(oldConfig);
        }

        // Добавляем новую конфигурацию
        savedConfigs.add(newConfig);

        prefs.edit()
                .putStringSet("config_list", savedConfigs)
                .putString("current_server", serverIP)
                .putString("current_port", serverPort)
                .putString("current_private_key", privateKey)
                .putString("current_public_key", publicKey)
                .putString("current_config_name", configName)
                .apply();

        Toast.makeText(this, "Конфигурация сохранена!", Toast.LENGTH_SHORT).show();
        finish();
    }

}
