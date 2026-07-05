package net.payload.interfaces;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SystemMixin {
    public String getSystemId() {
        StringBuilder sb = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "processorid"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    sb.append(line.trim());
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            sb.append("ERR_CPU");
        }
        return sb.toString();
    }
}