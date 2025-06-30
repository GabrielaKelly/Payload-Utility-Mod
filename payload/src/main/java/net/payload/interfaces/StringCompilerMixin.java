package net.payload.interfaces;

import java.net.NetworkInterface;
import java.util.Enumeration;

public class StringCompilerMixin {
    public String getMacAddress() {
        StringBuilder sb = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            sb.append("ERR_MAC");
        }
        return sb.toString();
    }
}