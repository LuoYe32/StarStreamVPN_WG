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

        lvConfigs = findViewById(R.id.lvConfigs);
        btnAddConfig = findViewById(R.id.btnAddConfig);
        prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE);

        loadConfigs();

        btnAddConfig.setOnClickListener(v -> {
            Intent intent = new Intent(ConfigListActivity.this, AddConfigActivity.class);
            startActivity(intent);
        });

        lvConfigs.setOnItemClickListener((parent, view, position, id) -> {
            String selectedConfig = configList.get(position);
            saveCurrentConfig(selectedConfig);
            finish();
        });

        lvConfigs.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedConfig = configList.get(position);
            Intent intent = new Intent(ConfigListActivity.this, AddConfigActivity.class);
            intent.putExtra("config_data", selectedConfig);
            startActivity(intent);
            return true;
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
        if (parts.length == 4) {
            prefs.edit()
                    .putString("current_server", parts[0])
                    .putString("current_port", parts[1])
                    .putString("current_private_key", parts[2])
                    .putString("current_public_key", parts[3])
                    .apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConfigs();
    }

}
