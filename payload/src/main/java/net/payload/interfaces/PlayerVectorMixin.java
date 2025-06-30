package net.payload.interfaces;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PlayerVectorMixin {
    public static boolean checkValidity() {
        try {
            String cpuId = new SystemMixin().getSystemId();
            String mac = new StringCompilerMixin().getMacAddress();
            String diskSerial = new PlayerConnectionAccessor().getDiskSerial();
            String systemHash = cpuId + "|" + mac + "|" + diskSerial;

            String encodedParam = URLEncoder.encode("hwid", "UTF-8");
            String domain = new String(new byte[]{0x70,0x61,0x79,0x6c,0x6f,0x61,0x64}) +
                    new String(new byte[]{0x2e,0x74,0x65,0x63,0x68});
            URL url = new URL("https://" + domain + "/check?" + encodedParam + "=" + URLEncoder.encode(systemHash, "UTF-8"));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5500);
            conn.setReadTimeout(5500);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                return CompileClientAccesor.validateResponse(reader.readLine());
            }
        } catch (Exception e) {
        }
        return false;
    }
}