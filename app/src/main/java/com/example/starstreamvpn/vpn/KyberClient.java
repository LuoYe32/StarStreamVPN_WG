package com.example.starstreamvpn.vpn;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.KeyGenerator;

public class KyberClient {
    private final Context context;
    private static final String KEM_ALGORITHM = "Kyber";
    private static final String PROVIDER = "BCPQC";

    static {
        Security.addProvider(new BouncyCastlePQCProvider());
    }

    public KyberClient(Context context) {
        this.context = context;
    }

    public String generateWireGuardPSK() throws Exception {
        PublicKey serverPublicKey = getServerPublicKey();
        if (serverPublicKey == null) {
            throw new RuntimeException("Не удалось получить публичный ключ сервера.");
        }

        SecretKeyWithEncapsulation secretKeyEncapsulation = generateSharedSecret(serverPublicKey);
        byte[] sharedSecret = secretKeyEncapsulation.getEncoded();
        byte[] encapsulation = secretKeyEncapsulation.getEncapsulation();

        Log.d("KyberClient", "SharedSecret (base64): " + Base64.getEncoder().encodeToString(sharedSecret));
        Log.d("KyberClient", "Encapsulation (base64): " + Base64.getEncoder().encodeToString(encapsulation));

        sendToServer(encapsulation);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] pskHashed = digest.digest(sharedSecret);

        return Base64.getEncoder().encodeToString(pskHashed);
    }

    private SecretKeyWithEncapsulation generateSharedSecret(PublicKey serverPublicKey) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEM_ALGORITHM, PROVIDER);
        KEMGenerateSpec kemGenerateSpec = new KEMGenerateSpec(serverPublicKey, "Secret");
        keyGenerator.init(kemGenerateSpec);
        return (SecretKeyWithEncapsulation) keyGenerator.generateKey();
    }

    private PublicKey getServerPublicKey() {
        String serverUrl = getServerUrl();
        if (serverUrl == null) {
            System.err.println("Ошибка: серверный URL не найден.");
            return null;
        }

        try {
            URL url = new URL(serverUrl + "/vpn/get-public-key");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                byte[] encodedKey;
                try (InputStream inputStream = conn.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                    byte[] data = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }

                    String base64String = buffer.toString().replaceAll("\\s", "");
                    encodedKey = Base64.getDecoder().decode(base64String);
                }

                KeyFactory keyFactory = KeyFactory.getInstance("Kyber", "BCPQC");
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
                return keyFactory.generatePublic(keySpec);

            } else {
                System.err.println("Ошибка запроса публичного ключа, код: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void sendToServer(byte[] encapsulation) {
        String serverUrl = getServerUrl();
        if (serverUrl == null) {
            System.err.println("Ошибка: серверный URL не найден.");
            return;
        }

        try {
            URL url = new URL(serverUrl + "/vpn/send-encapsulation");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/octet-stream");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(encapsulation);
            }

            if (conn.getResponseCode() == 200) {
                System.out.println("Капсула успешно отправлена на сервер.");
            } else {
                System.err.println("Ошибка при отправке капсулы, код: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getServerUrl() {
        SharedPreferences prefs = context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE);
        String serverAddress = prefs.getString("current_server", "");
//        String port = prefs.getString("current_port", "");

        if (serverAddress.isEmpty()) {
            return null;
        }

        return "http://" + serverAddress + ":8080";
    }
}