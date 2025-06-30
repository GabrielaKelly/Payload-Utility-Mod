package net.payload.gui;

public enum SpeedUnit {
    KMPH("km/h"),
    BLOCKS("blocks/s");

    private final String display;

    SpeedUnit(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}