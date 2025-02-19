package com.example.starstreamvpn.model;

import com.wireguard.android.backend.GoBackend;

public class PersistentConnectionProperties {

    private static PersistentConnectionProperties instance = null;
    private WgTunnel tunnel;
    private GoBackend backend;

    public WgTunnel getTunnel() {
        if (tunnel == null) {
            tunnel = new WgTunnel();
        }
        return tunnel;
    }

    public GoBackend getBackend() {
        return backend;
    }

    public void setBackend(GoBackend backend) {
        this.backend = backend;
    }

    public static synchronized PersistentConnectionProperties getInstance() {
        if (instance == null) {
            instance = new PersistentConnectionProperties();
        }
        return instance;
    }

}
