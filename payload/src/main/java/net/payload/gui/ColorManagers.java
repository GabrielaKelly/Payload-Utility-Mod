package net.payload.gui;

public class ColorManagers {

    public static boolean initialize() {
        return true;
    }

    public static class RGB {
        public int r;
        public int g;
        public int b;

        public RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public String toHex() {
            return String.format("#%02X%02X%02X", r, g, b);
        }
    }

    public static class HSL {
        public double h;
        public double s;
        public double l;

        public HSL(double h, double s, double l) {
            this.h = h;
            this.s = s;
            this.l = l;
        }
    }

    public static RGB hexToRGB(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return new RGB(r, g, b);
    }
}
