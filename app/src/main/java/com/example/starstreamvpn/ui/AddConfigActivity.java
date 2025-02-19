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

    private EditText etServerIP, etServerPort;
    private Button btnSaveConfig;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_config);

        // Инициализация UI
        etServerIP = findViewById(R.id.etServerIP);
        etServerPort = findViewById(R.id.etServerPort);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);
        prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE);

        // Обработчик кнопки "Сохранить"
        btnSaveConfig.setOnClickListener(v -> saveConfig());
    }

    private void saveConfig() {
        String serverIP = etServerIP.getText().toString().trim();
        String serverPort = etServerPort.getText().toString().trim();

        if (serverIP.isEmpty() || serverPort.isEmpty()) {
            Toast.makeText(this, "Введите IP и порт", Toast.LENGTH_SHORT).show();
            return;
        }

        String newConfig = serverIP + ":" + serverPort;

        // Получаем текущий список конфигураций
        Set<String> savedConfigs = prefs.getStringSet("config_list", new HashSet<>());
        savedConfigs = new HashSet<>(savedConfigs); // Создаем новый объект, чтобы избежать проблем с мутацией

        // Добавляем новую конфигурацию
        savedConfigs.add(newConfig);

        // Сохраняем обновленный список и делаем новую конфигурацию текущей
        prefs.edit()
                .putStringSet("config_list", savedConfigs)
                .putString("current_server", serverIP)
                .putString("current_port", serverPort)
                .apply();

        Toast.makeText(this, "Конфигурация сохранена!", Toast.LENGTH_SHORT).show();
        finish();
    }

}
