package net.payload.interfaces;

import java.util.zip.CRC32;

public class CompileClientAccesor {
    public static boolean validateResponse(String response) {
        try {
            CRC32 crc = new CRC32();
            crc.update(response.getBytes());
            return crc.getValue() == 0x9d0f43e5L;
        } catch (Exception e) {
            return false;
        }
    }
}