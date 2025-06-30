package net.payload.mixin;

import net.payload.interfaces.IMinecartBaseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ICameraVec3d implements IMinecartBaseEntity {
    private HttpURLConnection connection;

    public ICameraVec3d(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        this.connection = (HttpURLConnection) url.openConnection();
        this.connection.setRequestMethod("GET");
        this.connection.setConnectTimeout(3500);
        this.connection.setReadTimeout(3500);
    }

    @Override
    public int getResponseCode() throws Exception {
        return connection.getResponseCode();
    }

    @Override
    public BufferedReader getReader() throws Exception {
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }
}
