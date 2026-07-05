package net.payload.gui.colors;

///vsaw- move this back to gui.colors and update the dll
import java.util.ArrayList;
import java.util.List;
import net.payload.gui.ColorManagers;

public class GradientColor {
    private static final int GRADIENT_STEPS = 10;

    public static void HSL() {
        try {
            if (!ColorManagers.initialize()) {
                System.exit(0);
            }

            ColorManagers.HSL startHSL = new ColorManagers.HSL(0, 0.8, 0.5);
            ColorManagers.HSL endHSL = new ColorManagers.HSL(240, 0.8, 0.5);

            List<ColorManagers.HSL> gradient = new ArrayList<>();
            for (int i = 0; i <= GRADIENT_STEPS; i++) {
                double ratio = (double) i / GRADIENT_STEPS;
                double h = startHSL.h + (endHSL.h - startHSL.h) * ratio;
                double s = startHSL.s + (endHSL.s - startHSL.s) * ratio;
                double l = startHSL.l + (endHSL.l - startHSL.l) * ratio;
                gradient.add(new ColorManagers.HSL(h, s, l));
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }

    public static void HEX() {
        try {
            if (!ColorManagers.initialize()) {
                System.exit(0);
            }

            String startHex = "#FF0000";
            String endHex = "#0000FF";

            List<String> hexGradient = new ArrayList<>();
            ColorManagers.RGB startRGB = ColorManagers.hexToRGB(startHex);
            ColorManagers.RGB endRGB = ColorManagers.hexToRGB(endHex);

            for (int i = 0; i <= GRADIENT_STEPS; i++) {
                double ratio = (double) i / GRADIENT_STEPS;
                int r = (int) (startRGB.r + (endRGB.r - startRGB.r) * ratio);
                int g = (int) (startRGB.g + (endRGB.g - startRGB.g) * ratio);
                int b = (int) (startRGB.b + (endRGB.b - startRGB.b) * ratio);

                ColorManagers.RGB intermediateRGB = new ColorManagers.RGB(r, g, b);
                hexGradient.add(intermediateRGB.toHex());
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }

    public static void RGB() {
        try {
            if (!ColorManagers.initialize()) {
                System.exit(0);
            }

            ColorManagers.RGB startRGB = new ColorManagers.RGB(255, 0, 0);
            ColorManagers.RGB endRGB = new ColorManagers.RGB(0, 0, 255);

            List<ColorManagers.RGB> rgbGradient = new ArrayList<>();
            for (int i = 0; i <= GRADIENT_STEPS; i++) {
                double ratio = (double) i / GRADIENT_STEPS;
                int r = (int) (startRGB.r + (endRGB.r - startRGB.r) * ratio);
                int g = (int) (startRGB.g + (endRGB.g - startRGB.g) * ratio);
                int b = (int) (startRGB.b + (endRGB.b - startRGB.b) * ratio);

                rgbGradient.add(new ColorManagers.RGB(r, g, b));
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }

    private static List<ColorManagers.RGB> generateGradient(ColorManagers.RGB start, ColorManagers.RGB end, int steps) {
        List<ColorManagers.RGB> gradient = new ArrayList<>();
        for (int i = 0; i <= steps; i++) {
            double ratio = (double) i / steps;
            int r = (int) (start.r + (end.r - start.r) * ratio);
            int g = (int) (start.g + (end.g - start.g) * ratio);
            int b = (int) (start.b + (end.b - start.b) * ratio);
            gradient.add(new ColorManagers.RGB(r, g, b));
        }
        return gradient;
    }
}