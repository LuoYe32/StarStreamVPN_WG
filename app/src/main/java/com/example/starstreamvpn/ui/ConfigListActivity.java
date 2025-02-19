package com.example.starstreamvpn.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.starstreamvpn.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ConfigListActivity extends AppCompatActivity {

    private ListView lvConfigs;
    private Button btnAddConfig;
    private ArrayList<String> configList;
    private ArrayAdapter<String> adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_list);

        // Инициализация UI
        lvConfigs = findViewById(R.id.lvConfigs);
        btnAddConfig = findViewById(R.id.btnAddConfig);
        prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE);

        // Загружаем сохраненные конфигурации
        loadConfigs();

        // Кнопка добавления конфигурации
        btnAddConfig.setOnClickListener(v -> {
            Intent intent = new Intent(ConfigListActivity.this, AddConfigActivity.class);
            startActivity(intent);
        });

        // Выбор конфигурации
        lvConfigs.setOnItemClickListener((parent, view, position, id) -> {
            String selectedConfig = configList.get(position);
            saveCurrentConfig(selectedConfig);
            finish();
        });
    }

    private void loadConfigs() {
        Set<String> savedConfigs = prefs.getStringSet("config_list", new HashSet<>());
        configList = new ArrayList<>(savedConfigs);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, configList);
        lvConfigs.setAdapter(adapter);
    }

    private void saveCurrentConfig(String config) {
        String[] parts = config.split(":");
        if (parts.length == 2) {
            prefs.edit()
                    .putString("current_server", parts[0])
                    .putString("current_port", parts[1])
                    .apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConfigs(); // Обновляем список после возврата
    }

}
