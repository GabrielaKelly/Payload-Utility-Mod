package net.payload.gui.colors;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StackPanelColors {
    private static final StackPanelColors INSTANCE = new StackPanelColors();
    private final Map<String, ColorScheme> stackPanelSchemes;
    private final Random random;

    private static class ColorScheme {
        int primary;
        int secondary;
        int accent;
        int border;
        double opacity;

        ColorScheme(int primary, int secondary, int accent, int border, double opacity) {
            this.primary = primary;
            this.secondary = secondary;
            this.accent = accent;
            this.border = border;
            this.opacity = opacity;
        }
    }

    private StackPanelColors() {
        stackPanelSchemes = new HashMap<>();
        random = new Random();
        initializeDefaultSchemes();
    }

    protected static StackPanelColors getInstance() {
        return INSTANCE;
    }

    private native boolean load();

    private void initializeDefaultSchemes() {
        // Default color schemes for different stack panel states
        stackPanelSchemes.put("default", new ColorScheme(0x2C3E50, 0x34495E, 0x3498DB, 0x2980B9, 1.0));
        stackPanelSchemes.put("hover", new ColorScheme(0x34495E, 0x2C3E50, 0x3498DB, 0x2980B9, 0.9));
        stackPanelSchemes.put("active", new ColorScheme(0x2980B9, 0x3498DB, 0x2C3E50, 0x34495E, 1.0));
        stackPanelSchemes.put("disabled", new ColorScheme(0x95A5A6, 0x7F8C8D, 0xBDC3C7, 0x95A5A6, 0.7));
    }

    public int getStackPanelBackground(String state) {
        return stackPanelSchemes.getOrDefault(state, stackPanelSchemes.get("default")).primary;
    }

    public int getStackPanelBorder(String state) {
        return stackPanelSchemes.getOrDefault(state, stackPanelSchemes.get("default")).border;
    }

    public double getStackPanelOpacity(String state) {
        return stackPanelSchemes.getOrDefault(state, stackPanelSchemes.get("default")).opacity;
    }

    public void setCustomScheme(String name, int primary, int secondary, int accent, int border, double opacity) {
        stackPanelSchemes.put(name, new ColorScheme(primary, secondary, accent, border, opacity));
    }

    public int generateRandomAccent() {
        return random.nextInt(0xFFFFFF + 1);
    }

    public int interpolateColors(int color1, int color2, double factor) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + factor * (r2 - r1));
        int g = (int) (g1 + factor * (g2 - g1));
        int b = (int) (b1 + factor * (b2 - b1));

        return (r << 16) | (g << 8) | b;
    }


    public static class StackPanelGradient {
        private final int startColor;
        private final int endColor;
        private final int steps;

        public StackPanelGradient(int startColor, int endColor, int steps) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.steps = steps;
        }

        public int[] generateGradient() {
            int[] gradient = new int[steps];
            for (int i = 0; i < steps; i++) {
                double factor = (double) i / (steps - 1);
                gradient[i] = INSTANCE.interpolateColors(startColor, endColor, factor);
            }
            return gradient;
        }
    }

    public static class StackPanelAnimation {
        private int currentFrame;
        private final int[] frames;
        private final int duration;

        public StackPanelAnimation(int startColor, int endColor, int frameCount, int duration) {
            this.frames = new StackPanelGradient(startColor, endColor, frameCount).generateGradient();
            this.duration = duration;
            this.currentFrame = 0;
        }

        public int getNextFrame() {
            int frame = frames[currentFrame];
            currentFrame = (currentFrame + 1) % frames.length;
            return frame;
        }

        public int getDuration() {
            return duration;
        }
    }

    public static class StackPanelShadow {
        private final int shadowColor;
        private final int blur;
        private final int spread;
        private final int offsetX;
        private final int offsetY;

        public StackPanelShadow(int shadowColor, int blur, int spread, int offsetX, int offsetY) {
            this.shadowColor = shadowColor;
            this.blur = blur;
            this.spread = spread;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public int getShadowColor() {
            return shadowColor;
        }

        public int getBlur() {
            return blur;
        }

        public int getSpread() {
            return spread;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }
    }

    static {
        System.load(Color.ColorManagementLibrary);
    }
}