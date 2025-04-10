package com.example.starstreamvpn.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.starstreamvpn.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ConfigListActivity extends AppCompatActivity {

    private ListView lvConfigs;
    private Button btnAddConfig, btnBack;
    private ArrayList<String> configNames;
    private HashMap<String, String> configMap;
    private ArrayAdapter<String> adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_list);

        lvConfigs = findViewById(R.id.lvConfigs);
        btnAddConfig = findViewById(R.id.btnAddConfig);
        btnBack = findViewById(R.id.btnBack);
        prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE);

        loadConfigs();

        btnAddConfig.setOnClickListener(v -> {
            Intent intent = new Intent(ConfigListActivity.this, AddConfigActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());

        lvConfigs.setOnItemClickListener((parent, view, position, id) -> {
            String configName = configNames.get(position);
            String selectedConfig = configMap.get(configName);
            if (selectedConfig != null) {
                saveCurrentConfig(selectedConfig);
                finish();
            }
        });

        // Долгое нажатие - показать диалог (изменить/удалить)
        lvConfigs.setOnItemLongClickListener((parent, view, position, id) -> {
            String configName = configNames.get(position);
            String selectedConfig = configMap.get(configName);

            new AlertDialog.Builder(this)
                    .setTitle("Действие с конфигурацией")
                    .setMessage("Выберите действие для \"" + configName + "\"")
                    .setPositiveButton("Редактировать", (dialog, which) -> {
                        Intent intent = new Intent(ConfigListActivity.this, AddConfigActivity.class);
                        intent.putExtra("config_data", selectedConfig);
                        startActivity(intent);
                    })
                    .setNegativeButton("Удалить", (dialog, which) -> {
                        showDeleteConfirmation(configName);
                    })
                    .setNeutralButton("Отмена", null)
                    .show();

            return true;
        });
    }

    private void loadConfigs() {
        Set<String> savedConfigs = prefs.getStringSet("config_list", new HashSet<>());
        configNames = new ArrayList<>();
        configMap = new HashMap<>();

        for (String config : savedConfigs) {
            String[] parts = config.split(":");
            if (parts.length == 11) {
                String name = parts[0];
                configNames.add(name);
                configMap.put(name, config);
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, configNames);
        lvConfigs.setAdapter(adapter);
    }


    private void saveCurrentConfig(String config) {
        String[] parts = config.split(":");
        if (parts.length == 11) {
            prefs.edit()
                    .putString("current_server", parts[1])
                    .putString("current_port", parts[2])
                    .putString("current_private_key", parts[3])
                    .putString("current_public_key", parts[4])
                    .putString("current_address", parts[5])
                    .putString("current_dns", parts[6])
                    .putString("current_allowed_ips", parts[7])
                    .putString("current_persistent_keepalive", parts[8])
                    .putString("current_mtu", parts[9])
                    .putString("current_is_pqvpn", parts[10])
                    .putString("current_config_name", parts[0])
                    .apply();
        }
    }

    private void showDeleteConfirmation(String configName) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление конфигурации")
                .setMessage("Вы уверены, что хотите удалить \"" + configName + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteConfig(configName))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteConfig(String configName) {
        Set<String> savedConfigs = prefs.getStringSet("config_list", new HashSet<>());
        savedConfigs = new HashSet<>(savedConfigs); // Копируем, чтобы избежать ошибок мутации

        String fullConfig = configMap.get(configName);
        if (fullConfig != null) {
            savedConfigs.remove(fullConfig);

            prefs.edit().putStringSet("config_list", savedConfigs).apply();
            Toast.makeText(this, "Конфигурация \"" + configName + "\" удалена", Toast.LENGTH_SHORT).show();

            loadConfigs(); // Перезагружаем список после удаления
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConfigs();
    }

}
