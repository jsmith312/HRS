package com.example.jordansmith.homeroutersecurity;

import android.content.Context;
import android.util.Base64;

import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPHelper {
    public String username;
    public String password;
    public String ipAddress;
    public String mac;
    public Context context;

    public HTTPHelper(String username, String password, String ipAddress,
                      String MAC, Context context) {
        this.setUsername(username);
        this.setPassword(password);
        this.setIpAddress(ipAddress);
        this.setMac(MAC);
        this.setContext(context);
    }

    public int getResponse() throws Exception {
        String url = getIpAddress();
        String pw = getPassword();
        String u = getUsername();
        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        String userCredentials = u+":"+pw;
        String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), 0));
        con.setRequestProperty ("Authorization", basicAuth);
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        return responseCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
