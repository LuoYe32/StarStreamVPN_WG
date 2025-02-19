package com.example.starstreamvpn.model;

import androidx.annotation.NonNull;

import com.wireguard.android.backend.Tunnel;

public class WgTunnel implements Tunnel {

    @NonNull
    @Override
    public String getName() {
        return "wgpreconf";
    }

    @Override
    public void onStateChange(@NonNull State newState) {
    }

}
