package com.example.starstreamvpn.model;

import com.wireguard.config.InetNetwork;

import java.util.ArrayList;
import java.util.Collection;

public class TunnelModel {
    public String privateKey;
    public String IP;
    public String dns;
    public String endpoint;
    public Collection<InetNetwork> allowedIPs = new ArrayList<>();
    public boolean isPQVPN = false;
    public String publicKey;
}
