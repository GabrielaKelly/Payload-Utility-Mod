package net.payload.interfaces;

import java.io.BufferedReader;

public interface IMinecartBaseEntity {
    int getResponseCode() throws Exception;
    BufferedReader getReader() throws Exception;
}
