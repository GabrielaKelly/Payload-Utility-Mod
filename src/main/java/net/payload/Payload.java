package net.payload;

import net.fabricmc.api.ModInitializer;

public class Payload implements ModInitializer {
    public static PayloadClient instance;

    @Override
    public void onInitialize() {
        instance = new PayloadClient();
        instance.Initialize();
    }

    public static PayloadClient getInstance() {
        return instance;
    }
}