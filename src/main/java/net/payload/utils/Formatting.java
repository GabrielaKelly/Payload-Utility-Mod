package net.payload.utils;

public class Formatting {
//formatting styles
    public static final Formatting RESET = new Formatting("\u00A7r");
    public static final Formatting BOLD = new Formatting("\u00A7l");
    public static final Formatting ITALIC = new Formatting("\u00A7o");
    public static final Formatting UNDERLINE = new Formatting("\u00A7n");
    public static final Formatting STRIKETHROUGH = new Formatting("\u00A7m");
    public static final Formatting OBFUSCATED = new Formatting("\u00A7k");
//formatting colors
    public static final Formatting BLACK = new Formatting("\u00A70");
    public static final Formatting DARK_BLUE = new Formatting("\u00A71");
    public static final Formatting DARK_GREEN = new Formatting("\u00A72");
    public static final Formatting DARK_AQUA = new Formatting("\u00A73");
    public static final Formatting DARK_RED = new Formatting("\u00A74");
    public static final Formatting DARK_PURPLE = new Formatting("\u00A75");
    public static final Formatting GOLD = new Formatting("\u00A76");
    public static final Formatting GRAY = new Formatting("\u00A77");
    public static final Formatting DARK_GRAY = new Formatting("\u00A78");
    public static final Formatting BLUE = new Formatting("\u00A79");
    public static final Formatting GREEN = new Formatting("\u00A7a");
    public static final Formatting AQUA = new Formatting("\u00A7b");
    public static final Formatting RED = new Formatting("\u00A7c");
    public static final Formatting LIGHT_PURPLE = new Formatting("\u00A7d");
    public static final Formatting YELLOW = new Formatting("\u00A7e");
    public static final Formatting WHITE = new Formatting("\u00A7f");

    private final String code;

  //constructer
    private Formatting(String code) {
        this.code = code;
    }




    @Override
    public String toString() {
        return code;
    }

    //concatenate
    public Formatting combine(Formatting other) {
        return new Formatting(this.code + other.code);
    }
}

/* formatting example:

String testtext = "Test text to be formatted";
String textFormat = Formatting.BOLD.toString();
String text = textFormat + testtext;
Render2D.drawString(drawContext, text, x,y, GuiManager.foregroundColor.getValue().getColorAsInt());iteration.incrementAndGet();

 */