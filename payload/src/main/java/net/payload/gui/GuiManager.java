

package net.payload.gui;

import java.util.*;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.util.Identifier;
import net.payload.Payload;
import net.payload.gui.components.*;
import net.payload.gui.navigation.*;
import net.payload.gui.navigation.huds.*;
import net.payload.gui.navigation.windows.*;
import net.payload.gui.navigation.windows.ShulkCountHudOptionsWindow;

import net.payload.settings.types.*;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.payload.event.events.KeyDownEvent;
import net.payload.event.events.Render2DEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.KeyDownListener;
import net.payload.event.listeners.Render2DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.GridDefinition.RelativeUnit;
import net.payload.gui.colors.Color;
import net.payload.gui.colors.RainbowColor;
import net.payload.gui.colors.RandomColor;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingManager;
import net.payload.utils.input.CursorStyle;
import net.payload.utils.input.Input;
import net.payload.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;


public class GuiManager implements KeyDownListener, TickListener, Render2DListener {
	private static final MinecraftClient MC = MinecraftClient.getInstance();
	private static CursorStyle currentCursor = CursorStyle.Default;
	private static String tooltip = null;
	/// hud element private
	private DubCountOptionsWindow dubCountOptionsWindow;
	private ArmorHudOptionsWindow armorHudOptionsWindow;
	private PrideSheepOptionsWindow prideSheepOptionsWindow;
	private DVDLogoOptionsWindow dvdLogoOptionsWindow;
	private ModuleArrayListHudOptionsWindow moduleArrayListHudOptionsWindow;
	private WatermarkHudOptionsWindow watermarkHudOptionsWindow;
	private CoordsHudOptionsWindow coordsHudOptionsWindow;
	private DirectionHudOptionsWindow directionHudOptionsWindow;
	private FakeCoordsOptionsWindow fakeCoordsOptionsWindow;
	private ObfuscatedOptionsWindow obfuscatedOptionsWindow;
	private AccelerationHudOptionsWindow accelerationHudOptionsWindow;
	private SpeedNumberHudOptionsWindow speedNumberHudOptionsWindow;
	private SpeedHUDOptionsWindow speedHUDOptionsWindow;
	private IncomingPacketHudOptionsWindow incomingPacketHudOptionsWindow;
	private SentPacketHudOptionsWindow sentPacketHudOptionsWindow;
	private ShulkCountHudOptionsWindow shulkCountHudOptionsWindow;
	private PlayerModelHudOptionsWindow playerModelHudOptionsWindow;
	private NewArrayListOptionsWindow newArrayListOptionsWindow;

	///
	public KeybindSetting clickGuiButton = KeybindSetting.builder().id("key.clickgui").displayName("ClickGUI Key")
			.defaultValue(InputUtil.fromKeyCode(GLFW.GLFW_KEY_GRAVE_ACCENT, 0)).build();

	private final KeyBinding esc = new KeyBinding("key.esc", GLFW.GLFW_KEY_ESCAPE, "key.categories.payload");

	private boolean clickGuiOpen = false;
	private final HashMap<Object, Window> pinnedHuds = new HashMap<Object, Window>();

	private Framebuffer guiFrameBuffer;

	// Navigation Bar and Pages
	public NavigationBar clickGuiNavBar;
	// TODO- add images instead of these words
	public Page modulesPane = new Page("");
	public Page hudPane = new Page("");

	// Boolean Settings
	public static BooleanSetting accelShowText = BooleanSetting.builder()
			.id("accelShowText").displayName("Acceleration Text").defaultValue(true).build();
	// Boolean Settings
	public static BooleanSetting minifloat = BooleanSetting.builder()
			.id("minifloat").displayName("Mini Slider #").defaultValue(false).build();

	public static BooleanSetting guigirl = BooleanSetting.builder()
			.id("guigirl").displayName("Poster Girl").defaultValue(true).build();

	public static BooleanSetting obfuscatedpos = BooleanSetting.builder()
			.id("obfuscatedpos").displayName("Obfuscate Pos").defaultValue(false).build();
	public static BooleanSetting smallheader = BooleanSetting.builder()
			.id("smallheader").displayName("Mini Header").defaultValue(false).build();


	public static BooleanSetting packetShowText = BooleanSetting.builder()
			.id("packetShowText").displayName("Show Text").defaultValue(true).build();

	public static BooleanSetting moddescriptions = BooleanSetting.builder()
			.id("moddescriptions").displayName("Module Description").defaultValue(true).build();

	public static BooleanSetting randomposition = BooleanSetting.builder()
			.id("randomposition").displayName("Random Modifier").defaultValue(false).build();

	public static BooleanSetting sentPacketShowText = BooleanSetting.builder()
			.id("sentPacketShowText").displayName("Show Text").defaultValue(true).build();

	public static BooleanSetting showSpeedGraph = BooleanSetting.builder()
			.id("showSpeedGraph").displayName("Show Speed Graph").defaultValue(false).build();

	public static BooleanSetting speedGraphShowText = BooleanSetting.builder()
			.id("speedGraphShowText").displayName("Show Text").defaultValue(true).build();

	public static BooleanSetting copyYaw = BooleanSetting.builder()
			.id("copyYaw").displayName("Copy Yaw").defaultValue(true).build();

	public static BooleanSetting copyPitch = BooleanSetting.builder()
			.id("copyPitch").displayName("Copy Pitch").defaultValue(true).build();

	// Color Settings
	public static ColorSetting accelBackgroundColor = ColorSetting.builder()
			.id("accelBackgroundColor").displayName("Background Color").defaultValue(new Color(0, 0, 0, (int)(255 * 0.7f))).build();

	public static ColorSetting atsubackround2 = ColorSetting.builder()
			.id("astubackroundcolor").displayName("GUI Background").defaultValue(new Color(0,0,0, 45)).build();

	public static ColorSetting accelhudcolor = ColorSetting.builder()
			.id("accelhudcolor").displayName("Acceleration Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting arraylistcolor = ColorSetting.builder()
			.id("arraylistcolor").displayName("Arraylist Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting newarraylistcolor = ColorSetting.builder()
			.id("newarraylistcolor1").displayName("Arraylist Color").defaultValue(new Color(179,185,245, 255)).build();


	public static ColorSetting newarraylistSecondColor = ColorSetting.builder()
			.id("newarraylistcolor2").displayName("Arraylist Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting backgroundColor = ColorSetting.builder()
			.defaultValue(new Color(255, 255, 255, 100)).build();

	public static ColorSetting borderColor = ColorSetting.builder()
			.id("hud_border_color").defaultValue(new Color(0,0,0, 255)).build();

	public static ColorSetting check = ColorSetting.builder()
			.id("check").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting closed = ColorSetting.builder()
			.id("closed").defaultValue(new Color(140,140,140, 255)).build();

	public static ColorSetting directioncolor = ColorSetting.builder()
			.id("obfarraylistcolor").displayName("Direction Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting fakeposcolor = ColorSetting.builder()
			.id("arraylistcolor").displayName("Fake Coords Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting foregroundColor = ColorSetting.builder()
			.id("hud_foreground_color").defaultValue(new Color(255, 255, 255, 255)).build();

	public static ColorSetting gearwrench = ColorSetting.builder()
			.id("gearwrench").defaultValue(new Color(140,140,140, 255)).build();

	public static ColorSetting obfarraylistcolor = ColorSetting.builder()
			.id("obfarraylistcolor").displayName("Obfuscated Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting packetBackgroundColor = ColorSetting.builder()
			.id("packetBackgroundColor").displayName("Background Color").defaultValue(new Color(0, 0, 0, (int)(255 * 0.7f))).build();

	public static ColorSetting packethudcolor = ColorSetting.builder()
			.id("packethudcolor").displayName("Graph Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting pinDark = ColorSetting.builder()
			.id("pinDark").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting poscolor = ColorSetting.builder()
			.id("obfarraylistcolor").displayName("Coords Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting sentPacketBackgroundColor = ColorSetting.builder()
			.id("sentPacketBackgroundColor").displayName("Background Color").defaultValue(new Color(0, 0, 0, (int)(255 * 0.7f))).build();

	public static ColorSetting sentPackethudcolor = ColorSetting.builder()
			.id("sentPackethudcolor").displayName("Graph Color").defaultValue(new Color(245, 179, 179, 255)).build();

	public static ColorSetting speedGraphBackgroundColor = ColorSetting.builder()
			.id("speedGraphBackgroundColor").displayName("Background Color").defaultValue(new Color(0, 0, 0, (int)(255 * 0.7f))).build();


	public static ColorSetting speedGraphColor = ColorSetting.builder()
			.id("speedGraphColor").displayName("Graph Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting speedhudcolor = ColorSetting.builder()
			.id("speedhudcolor").displayName("SpeedHud Color").defaultValue(new Color(179,185,245, 255)).build();

	public static ColorSetting thugColor = ColorSetting.builder()
			.id("thugColor").defaultValue(new Color(117,117,117, 255)).build();

	public static ColorSetting watermarkcolors = ColorSetting.builder() .id("WatermarkColor")
			.displayName("Watermark Color").defaultValue(new Color(255, 255, 255, 255)).build();

	public static ColorSetting dubLabelColor = ColorSetting.builder() .id("dubColor")
			.displayName("Label Color").defaultValue(new Color(255, 255, 255, 255)).build();
	public static ColorSetting dubCountColor = ColorSetting.builder() .id("dubCountColor")
			.displayName("Count Color").defaultValue(new Color(255, 255, 255, 255)).build();

	public static ColorSetting shulkLabelColor = ColorSetting.builder() .id("shulkLabelColor")
			.displayName("Label Color").defaultValue(new Color(255, 255, 255, 255)).build();
	public static ColorSetting shulkCountColor = ColorSetting.builder() .id("shulkCountColor")
			.displayName("Count Color").defaultValue(new Color(255, 255, 255, 255)).build();


	// Float Settings
	public static FloatSetting AccelScale = FloatSetting.builder()
			.id("AccelScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting headerheight = FloatSetting.builder()
			.id("headerheight").displayName("Header Height").defaultValue(24.0f).step(1.0f).minValue(20.0f).maxValue(34.0f).build();

	public static FloatSetting AccelUpdateDelay = FloatSetting.builder()
			.id("AccelUpdateDelay").displayName("Update Delay").defaultValue(0.05f).step(0.05f).minValue(0.05f).maxValue(1.0f).build();

	public static FloatSetting ArmorScale = FloatSetting.builder()
			.id("ArmorScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting CoordsScale = FloatSetting.builder()
			.id("CoordsScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting DirectionScale = FloatSetting.builder()
			.id("DirectionScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting dragSmoothening = FloatSetting.builder()
			.id("gui_drag_smoothening").defaultValue(1.0f).minValue(0.1f).maxValue(2.0f).step(0.1f).build();

	public static BooleanSetting matrixscaling = BooleanSetting.builder()
			.id("matrixscaling").displayName("Mini Mode").defaultValue(false).build();

	public static FloatSetting FakeArrayScale = FloatSetting.builder()
			.id("FakeArrayScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting FakeCoordsScale = FloatSetting.builder()
			.id("FakeCoordsScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting fakexvalue = FloatSetting.builder()
			.id("fakexvalue").displayName("X-Axis Offset").defaultValue(1.0f).step(0.1f).minValue(-10.0f).maxValue(10.0f).build();

	public static FloatSetting fakezvalue = FloatSetting.builder()
			.id("fakezvalue").displayName("Z-Axis Offset").defaultValue(1.0f).step(0.1f).minValue(-10.0f).maxValue(10.0f).build();

	public static FloatSetting ModuleArrayScale = FloatSetting.builder()
			.id("ArraylistScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting PacketScale = FloatSetting.builder()
			.id("PacketScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(0.5f).maxValue(5.0f).build();

	public static FloatSetting PacketUpdateDelay = FloatSetting.builder()
			.id("PacketUpdateDelay").displayName("Update Delay").defaultValue(0.05f).step(0.05f).minValue(0.05f).maxValue(1.0f).build();

	public static FloatSetting roundingRadius = FloatSetting.builder()
			.id("hud_rounding_radius").defaultValue(0f).minValue(0f).maxValue(10f).step(1f).build();

	public static FloatSetting SentPacketScale = FloatSetting.builder()
			.id("SentPacketScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(0.5f).maxValue(5.0f).build();

	public static FloatSetting SentPacketUpdateDelay = FloatSetting.builder()
			.id("SentPacketUpdateDelay").displayName("Update Delay").defaultValue(0.05f).step(0.05f).minValue(0.05f).maxValue(1.0f).build();

	public static FloatSetting SheepScale = FloatSetting.builder()
			.id("SheepScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting SpeedGraphScale = FloatSetting.builder()
			.id("SpeedGraphScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting SpeedGraphUpdateDelay = FloatSetting.builder()
			.id("SpeedGraphUpdateDelay").displayName("Update Delay").defaultValue(0.05f).step(0.05f).minValue(0.05f).maxValue(1.0f).build();

	public static FloatSetting SpeedScale = FloatSetting.builder()
			.id("SpeedScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting SpeedUpdateDelay = FloatSetting.builder()
			.id("SpeedUpdateDelay").displayName("Update Delay").defaultValue(0.25f).step(0.05f).minValue(0.05f).maxValue(2.0f).build();

	public static FloatSetting WatermarkScale = FloatSetting.builder()
			.id("WatermarkScale").displayName("Scale").defaultValue(1.0f).step(0.1f).minValue(1.0f).maxValue(5.0f).build();

	public static FloatSetting customYaw = FloatSetting.builder()
			.id("customYaw").displayName("Custom Yaw").defaultValue(0f).step(10f).minValue(-180f).maxValue(180f).build();

	public static FloatSetting customPitch = FloatSetting.builder()
			.id("customPitch").displayName("Custom Pitch").defaultValue(0f).step(5f).minValue(-90f).maxValue(90f).build();

	// Enum Settings
	public static EnumSetting<CenterOrientation> centerOrientation = EnumSetting.<CenterOrientation>builder()
			.id("centerOrientation").displayName("Orientation").defaultValue(CenterOrientation.South).build();

	public static EnumSetting<ArmorEnum> armorEnum = EnumSetting.<ArmorEnum>builder()
			.id("ArmorEnum").displayName("Durability").defaultValue(ArmorEnum.PERCENT).build();

	public static EnumSetting<ArmorPosition> armorPosition = EnumSetting.<ArmorPosition>builder()
			.id("armorPosition").displayName("Style").defaultValue(ArmorPosition.VERTICAL).build();

	public static EnumSetting<TextRenderFormatting> textrenderformat = EnumSetting.<TextRenderFormatting>builder()
			.id("armorPosition").displayName("Text Format").defaultValue(TextRenderFormatting.NORMAL).build();

	public static EnumSetting<CoordStyles> coordStyles = EnumSetting.<CoordStyles>builder()
			.id("coordStyles").displayName("Style").defaultValue(CoordStyles.PAYLOAD).build();
	public static EnumSetting<FakeCoordStyles> fakecoordStyles = EnumSetting.<FakeCoordStyles>builder()
			.id("fakecoordStyles").displayName("Style").defaultValue(FakeCoordStyles.PAYLOAD).build();

	public static EnumSetting<DirectionStyles> directionStyles = EnumSetting.<DirectionStyles>builder()
			.id("directionStyles").displayName("Style").defaultValue(DirectionStyles.PAYLOAD).build();
	public static EnumSetting<DirectionSize> directionSize = EnumSetting.<DirectionSize>builder()
			.id("directionSize").displayName("Size").defaultValue(DirectionSize.NORMAL).build();
	public static EnumSetting<PacketHudSize> packetsizeMode = EnumSetting.<PacketHudSize>builder()
			.id("packetsizeMode").displayName("Size").defaultValue(PacketHudSize.NORMAL).build();

	public static EnumSetting<CoordsSize> coordssizeMode = EnumSetting.<CoordsSize>builder()
			.id("CoordsSize").displayName("Size").defaultValue(CoordsSize.NORMAL).build();

	public static EnumSetting<IncPacketHudSize> incpacketsizeMode = EnumSetting.<IncPacketHudSize>builder()
			.id("incpacketsizeMode").displayName("Size").defaultValue(IncPacketHudSize.NORMAL).build();

	public static EnumSetting<ArmorSizing> armorSizing = EnumSetting.<ArmorSizing>builder()
			.id("ArmorSizing").displayName("Size").defaultValue(ArmorSizing.NORMAL).build();

	public static EnumSetting<ShulkSize> shulkSize = EnumSetting.<ShulkSize>builder()
			.id("ShulkSize").displayName("Size").defaultValue(ShulkSize.NORMAL).build();

	public static EnumSetting<DubCountTextStyle> dubCountTextStyle = EnumSetting.<DubCountTextStyle>builder()
			.id("DubCountTextStyle").displayName("Text Format").defaultValue(DubCountTextStyle.NORMAL).build();

	public static EnumSetting<ShulkCountTextStyle> shulkCountTextStyle = EnumSetting.<ShulkCountTextStyle>builder()
			.id("DubCountTextStyle").displayName("Text Format").defaultValue(ShulkCountTextStyle.NORMAL).build();

	public static EnumSetting<AccelHudSize> accelSizeMode = EnumSetting.<AccelHudSize>builder()
			.id("accelHudSize").displayName("Size").defaultValue(AccelHudSize.NORMAL).build();

	public static EnumSetting<DubSize> dubSize = EnumSetting.<DubSize>builder()
			.id("dubSize").displayName("Size").defaultValue(DubSize.NORMAL).build();

	public static EnumSetting<SheepHudSize> sheepSizeMode = EnumSetting.<SheepHudSize>builder()
			.id("sheepSizeMode").displayName("Size").defaultValue(SheepHudSize.SMALL).build();
	public static EnumSetting<GuiStyle> guistyle = EnumSetting.<GuiStyle>builder()
			.id("Mode Choice")
			.displayName("Gui Theme")
			.defaultValue(GuiStyle.Atsu)
			.build();

	public static EnumSetting<Silly> silly = EnumSetting.<Silly>builder()
			.id("silly").displayName("Obfuscate").defaultValue(Silly.DISABLED).build();

	public static EnumSetting<ArrayListGradientMode> arraylistgradientmode = EnumSetting.<ArrayListGradientMode>builder()
			.id("silly").displayName("Custom Gradient").defaultValue(ArrayListGradientMode.OFF).build();

	public static  EnumSetting<ModuleArraySize> moduleArraySizeMode =  EnumSetting.<ModuleArraySize>builder()
			.id("ModuleArraySize").displayName("Size").defaultValue(ModuleArraySize.NORMAL).build();

	public static  EnumSetting<NewModuleArraySize> newmoduleArraySizeMode =  EnumSetting.<NewModuleArraySize>builder()
			.id("ModuleArraySize").displayName("Size").defaultValue(NewModuleArraySize.NORMAL).build();

	public static  EnumSetting<SpeedSize> speedSize =  EnumSetting.<SpeedSize>builder()
			.id("SpeedSize").displayName("Size").defaultValue(SpeedSize.NORMAL).build();

	public static EnumSetting<SortingMode> sortingMode = EnumSetting.<SortingMode>builder()
			.id("ModuleArraylist_sorting").displayName("Sorting").defaultValue(SortingMode.ALPHABET).build();

	public static EnumSetting<NewSortingMode> newsortingMode = EnumSetting.<NewSortingMode>builder()
			.id("ModuleArraylist_sorting").displayName("Sorting").defaultValue(NewSortingMode.ALPHABET).build();

	public static EnumSetting<SpeedUnit> speedUnit = EnumSetting.<SpeedUnit>builder()
			.id("speedUnit").displayName("Speed Unit").defaultValue(SpeedUnit.KMPH).build();

	public static EnumSetting<SpeedHudSize> speedSizeMode = EnumSetting.<SpeedHudSize>builder()
			.id("SpeedHudSize").displayName("Size").defaultValue(SpeedHudSize.NORMAL).build();

	public static EnumSetting<ModuleSettingsStyle> modulesettingsstyle = EnumSetting.<ModuleSettingsStyle>builder()
			.id("ModuleSettingsStyle").displayName("Editing Style").defaultValue(ModuleSettingsStyle.Collapsed).build();

	public static EnumSetting<WatermarkSize> sizeMode = EnumSetting.<WatermarkSize>builder()
			.id("sizeMode").displayName("Size").defaultValue(WatermarkSize.NORMAL).build();

	public static EnumSetting<PlayerModelSize> playerModelSizeMode = EnumSetting.<PlayerModelSize>builder()
			.id("sizeMode").displayName("Size").defaultValue(PlayerModelSize.NORMAL).build();

	public static EnumSetting<FakeCoordsSize> fakecoordsizeMode = EnumSetting.<FakeCoordsSize>builder()
			.id("fakecoordsizeMode").displayName("Size").defaultValue(FakeCoordsSize.NORMAL).build();

	public static EnumSetting<WatermarkStyle> watermarkStyleEnumSetting = EnumSetting.<WatermarkStyle>builder()
			.id("watermarkStyleEnumSetting").displayName("Mode").defaultValue(WatermarkStyle.GRADIENT).build();

	public static RainbowColor rainbowColor = new RainbowColor();
	public static RandomColor randomColor = new RandomColor();
	public FakeCoords fakeCoords;
	public ArmorHud armorHud;
	public PlayerModelHud playerModelHud;
	public TimeHud timeHud;
	public Sheep sheep;
	public DVDLogo dvdLogo;
	public IncomingPacketHud incomingPacketHud;
	public SentPacketHud sentPacketHud;
	public HaloHud halohud;
	public ModuleArrayListHud moduleArrayListHud;
	public WatermarkHud watermarkHud;
	public CoordsHud coordsHud;
	public SpeedNumberHud speedNumberHud;
	public AccelerationHud accelerationHud;
	public DirectionHud directionHud;
	public SpeedHUD SpeedHUD;
	public ChestCountHud chestCountHud;
	public ShulkerCountHud shulkerCountHud;
	public NewArrayList newArrayList;

	public GuiManager() {
		clickGuiNavBar = new NavigationBar();
		SettingManager.registerGlobalSetting(borderColor);
		SettingManager.registerGlobalSetting(backgroundColor);
		SettingManager.registerGlobalSetting(roundingRadius);
		SettingManager.registerSetting(clickGuiButton);
		SettingManager.registerGlobalSetting(foregroundColor);
		/// HUD SETTING REGISTRIES BELOW
		SettingManager.registerGlobalSetting(modulesettingsstyle);
		SettingManager.registerGlobalSetting(fakexvalue);
		SettingManager.registerGlobalSetting(fakezvalue);
		SettingManager.registerGlobalSetting(sortingMode);
		SettingManager.registerGlobalSetting(arraylistcolor);
		SettingManager.registerGlobalSetting(obfarraylistcolor);
		SettingManager.registerGlobalSetting(speedhudcolor);
		SettingManager.registerGlobalSetting(fakeposcolor);
		SettingManager.registerGlobalSetting(poscolor);
		SettingManager.registerSetting(randomposition);
		SettingManager.registerSetting(obfuscatedpos);

		net.minecraft.client.util.Window window = MC.getWindow();

		guiFrameBuffer = new SimpleFramebuffer(window.getWidth(), window.getHeight(), false);

		Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(Render2DListener.class, this);
	}

	public void Initialize() {
		System.out.println("Initializing");
		fakeCoords = new FakeCoords(0, 0);



		int screenHeight =MC.getWindow().getHeight();
		int screenWidth = MC.getWindow().getWidth();
		int sheepX = screenWidth / 2;
		int sheepY = 0;
		int speedNumberHudx = 0;
		int speedNumberHudy = screenHeight / 2;
		int armorHudX = screenWidth;
		int armorHudY = screenHeight / 2;
		int coordsHudX = 0;
		int coordsHudY = screenHeight;
		int directionhudheight = 23;
		int directionhudy = screenHeight - directionhudheight;
		int directionhudx = 0;
		int sentpackethudx = screenWidth;
		int sentpackethudy = screenHeight +2;
		int incomingpackethudx = screenWidth;
		int incomingpackethudy = screenHeight - 102;
		int accelerationhudx = screenWidth;
		int accelerationhudy = screenHeight - 202;
		int speedhudx = screenWidth;
		int speedhuy = screenHeight - 302;

/// properly stored default locations
		newArrayList = new NewArrayList(0,0);
		chestCountHud = new ChestCountHud(0,0);
		sheep = new Sheep(sheepX, sheepY);
		shulkerCountHud = new ShulkerCountHud(0,0);
		armorHud = new ArmorHud(armorHudX, armorHudY);
		speedNumberHud = new SpeedNumberHud(speedNumberHudx, speedNumberHudy);
		dvdLogo = new DVDLogo(0, 0);
		halohud = new HaloHud();
		moduleArrayListHud = new ModuleArrayListHud(0, 50, ModuleArrayScale);
		playerModelHud = new PlayerModelHud(500, 500);
		watermarkHud = new WatermarkHud(0, 0);
		accelerationHud = new AccelerationHud(accelerationhudx, accelerationhudy);
		SpeedHUD = new SpeedHUD(speedhudx, speedhuy);
		sentPacketHud = new SentPacketHud(sentpackethudx, sentpackethudy);
		incomingPacketHud = new IncomingPacketHud(incomingpackethudx, incomingpackethudy);
		directionHud = new DirectionHud(directionhudx, directionhudy);
		coordsHud = new CoordsHud(coordsHudX, coordsHudY);
/// hud element control

		newArrayListOptionsWindow = new NewArrayListOptionsWindow();
		shulkCountHudOptionsWindow = new ShulkCountHudOptionsWindow();
		dubCountOptionsWindow = new DubCountOptionsWindow();
		armorHudOptionsWindow = new ArmorHudOptionsWindow();
		prideSheepOptionsWindow = new PrideSheepOptionsWindow();
		dvdLogoOptionsWindow = new DVDLogoOptionsWindow();
		speedNumberHudOptionsWindow = new SpeedNumberHudOptionsWindow();
		watermarkHudOptionsWindow = new WatermarkHudOptionsWindow();
		coordsHudOptionsWindow = new CoordsHudOptionsWindow();
		fakeCoordsOptionsWindow = new FakeCoordsOptionsWindow();
		obfuscatedOptionsWindow = new ObfuscatedOptionsWindow();
		moduleArrayListHudOptionsWindow = new ModuleArrayListHudOptionsWindow();
		accelerationHudOptionsWindow = new AccelerationHudOptionsWindow();
		speedHUDOptionsWindow = new SpeedHUDOptionsWindow();
		incomingPacketHudOptionsWindow = new IncomingPacketHudOptionsWindow();
		sentPacketHudOptionsWindow = new SentPacketHudOptionsWindow();
		directionHudOptionsWindow = new DirectionHudOptionsWindow();
		playerModelHudOptionsWindow = new PlayerModelHudOptionsWindow();
		ArrayList<HudWindow> huds = Lists.newArrayList(watermarkHud, newArrayList, moduleArrayListHud,coordsHud,directionHud,speedNumberHud, armorHud, chestCountHud, shulkerCountHud, fakeCoords, sheep, halohud,
				sentPacketHud, incomingPacketHud, accelerationHud, SpeedHUD, playerModelHud);

	//	ArrayList<HudWindow> huds = Lists.newArrayList(sentPacketHud, incomingPacketHud,directionHud,SpeedHUD,accelerationHud, halohud, fakeCoords, sheep, armorHud,
	//			moduleArrayListHud, watermarkHud, coordsHud, speedNumberHud);
	//	hudPane.addWindow(new HudColorsWindow());
		hudPane.addWindow(new TextHudsTab(huds));
		hudPane.addWindow(new ImageHudsTab(huds));
		Map<String, Category> categories = Category.getAllCategories();
		float xOffset = 50;

		for (Category category : categories.values()) {
			Window tab = new Window(category.getName(), xOffset, 75.0f);
			StackPanelComponent stackPanel = new StackPanelComponent();
			stackPanel.setMargin(new Margin(null, 30f, null, null));


//vsaw - HERE IS THE ENTIRE HEADER CODE
			GridComponent gridComponent = new GridComponent();
			gridComponent.addColumnDefinition(new GridDefinition(0, RelativeUnit.Absolute));
			gridComponent.addColumnDefinition(new GridDefinition(0, RelativeUnit.Relative));
			HeaderStringComponent title = new HeaderStringComponent(category.getName());
			title.setIsHitTestVisible(false);
			gridComponent.addChild(title);
			HeaderStringComponent placeholder = new HeaderStringComponent("");
			gridComponent.addChild(placeholder);
			stackPanel.addChild(gridComponent);
			SeparatorComponent separator = new SeparatorComponent();
			separator.setIsHitTestVisible(false);
			stackPanel.addChild(separator);
//vsaw - HERE IS THE ENTIRE HEADER CODE




			for (Module module : Payload.getInstance().moduleManager.modules) {
				if (module.getCategory().equals(category)) {
					ModuleComponent button = new ModuleComponent(module);
					stackPanel.addChild(button);
				}
			}

			tab.addChild(stackPanel);
			///vsaw-> clickgui width/height
		//	tab.setMaxWidth(230f);
			tab.setMaxWidth(230f);
			modulesPane.addWindow(tab);

			xOffset += tab.getMinWidth() + 10;
		}

		//hudPane.addWindow(new SettingsWindow());

		clickGuiNavBar.addPane(modulesPane);

		clickGuiNavBar.addPane(hudPane);

		modulesPane.initialize();

		hudPane.initialize();

		clickGuiNavBar.setSelectedIndex(0);
	}

	public static CursorStyle getCursor() {
		return currentCursor;
	}

	public static void setCursor(CursorStyle cursor) {
		currentCursor = cursor;
		Input.setCursorStyle(currentCursor);
	}

	public static String getTooltip() {
		return tooltip;
	}

	void onStyleChanged(ModuleSettingsStyle newStyle) {
		GuiManager.modulesettingsstyle.setValue(newStyle);
		closeAllModuleSettings();
	}

	public static void setTooltip(String tt) {
		if (tooltip != tt)
			tooltip = tt;
	}

	public void addWindow(Window hud, String pageName) {
		for (Page page : clickGuiNavBar.getPanes()) {
			if (page.getTitle().equals(pageName)) {
				page.addWindow(hud);
				page.moveToFront(hud);
				hud.initialize();
				break;
			}
		}
	}

	public void removeWindow(Window hud, String pageName) {
		for (Page page : clickGuiNavBar.getPanes()) {
			if (page.getTitle().equals(pageName)) {
				page.removeWindow(hud);
				break;
			}
		}
	}

	private void closeAllModuleSettings() {
		for (Window window : modulesPane.getWindows()) {
			for (UIElement element : window.getChildren()) {
				if (element instanceof StackPanelComponent) {
					StackPanelComponent stackPanel = (StackPanelComponent) element;
					for (UIElement child : stackPanel.getChildren()) {
						if (child instanceof ModuleComponent) {
							((ModuleComponent) child).closeAllSettingsWindows();
						}
					}
				}
			}
		}
	}
	public void removehudElementWindow(Window hud, String pageName) {
		for (Page page : clickGuiNavBar.getPanes()) {
			if (page.getTitle().equals(pageName)) {
				break;
			}
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (clickGuiButton.getValue().getCode() == event.GetKey() && MC.currentScreen == null) {
			setClickGuiOpen(!this.clickGuiOpen);
			this.toggleMouse();
		}
	}

	public void setHudActive(HudWindow hud, boolean state) {
		if (state) {
			pinnedHuds.put(hud.getClass(), hud);
			hud.activated.silentSetValue(true);
			hudPane.addWindow(hud);
		} else {
			this.pinnedHuds.remove(hud.getClass());
			hud.activated.silentSetValue(false);
			hudPane.removeWindow(hud);
		}
	}

	@Override
	public void onTick(TickEvent.Pre event) {

	}

	@Override
	public void onTick(TickEvent.Post event) {
		/**
		 * Moves the selected Tab to where the user moves their mouse.
		 */
		if (this.clickGuiOpen) {
			clickGuiNavBar.update();
		}

		/**
		 * Updates each of the Tab GUIs that are currently on the screen.
		 */
		for (Window hud : pinnedHuds.values()) {
			hud.update();
		}

		if (this.esc.isPressed() && this.clickGuiOpen) {
			this.clickGuiOpen = false;
			this.toggleMouse();
		}
	}
	private static final Identifier ATSU_IDENTIFIER = Identifier.of("payload", "textures/namesake.png");
	private static final Identifier MASHIRO_IDENTIFIER = Identifier.of("payload", "textures/rias.png");
	private static final Identifier LEON_IDENTIFIER = Identifier.of("payload", "textures/captain.png");

	@Override
	public void onRender(Render2DEvent event) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		DrawContext drawContext = event.getDrawContext();
		float tickDelta = event.getRenderTickCounter().getTickDelta(false);

		MatrixStack matrixStack = drawContext.getMatrices();
		matrixStack.push();

		int guiScale = MC.getWindow().calculateScaleFactor(MC.options.getGuiScale().getValue(), MC.forcesUnicodeFont());
		float scale = 1.0f / guiScale;
		matrixStack.scale(scale, scale, 1.0f);

		net.minecraft.client.util.Window window = (net.minecraft.client.util.Window) MC.getWindow();
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();

		if (this.clickGuiOpen) {
			Render2D.drawBox(matrix, 0, 0, window.getWidth(), window.getHeight(), new Color(26, 26, 26, 0));
			clickGuiNavBar.draw(drawContext, tickDelta);
		}

		if (!this.clickGuiOpen) {
			for (Window hud : pinnedHuds.values()) {
				hud.draw(drawContext, tickDelta);
			}
		}

		if (this.clickGuiOpen && MC.getWindow().isFullscreen() && GuiManager.guigirl.getValue()) {
			int windowWidth = MC.getWindow().getWidth();
			int windowHeight = MC.getWindow().getHeight();
			int atsuimageWidth = 525;
			int atsuimageHeight = 1000;
			int riasimageWidth = 726; //1192;
			int riasimageHeight = 491; //670;
			int leonimageWidth = 417; //1192;
			int leonimageHeight = 600; //670;
			float x = windowWidth - atsuimageWidth - -10;
			float y = windowHeight - atsuimageHeight;
			float riasY = windowHeight - riasimageHeight;
			float riasX = windowWidth - riasimageWidth;
			float leonX = windowWidth - leonimageWidth;
			float leonY = 500;


				switch (GuiManager.guistyle.getValue()) {
					case Atsu:
						RenderSystem.setShaderTexture(0, ATSU_IDENTIFIER);
						Render2D.drawTexturedQuad(matrixStack.peek().getPositionMatrix(),
								ATSU_IDENTIFIER, x, y, 525, 1000,
								new Color(255, 255, 255, 255));
						break;
					case Rias:
						RenderSystem.setShaderTexture(1, MASHIRO_IDENTIFIER);
						Render2D.drawTexturedQuad(matrixStack.peek().getPositionMatrix(),
								MASHIRO_IDENTIFIER, riasX + 15, riasY, riasimageWidth, riasimageHeight,
								new Color(255, 255, 255, 255));
						break;
					case Captain:
						RenderSystem.setShaderTexture(2, LEON_IDENTIFIER);
						Render2D.drawTexturedQuad(matrixStack.peek().getPositionMatrix(),
								LEON_IDENTIFIER, leonX, leonY, leonimageWidth, leonimageHeight,
								new Color(255, 255, 255, 255));
						break;
				}
				RenderSystem.enableCull();
				RenderSystem.disableBlend();
			}
		renderTooltip(drawContext, matrixStack);
		matrixStack.pop();
		}

	private void renderTooltip(DrawContext drawContext, MatrixStack matrixStack) {
		if (tooltip != null && GuiManager.moddescriptions.getValue()) {
			int mouseX = (int) MC.mouse.getX();
			int mouseY = (int) MC.mouse.getY();
			int tooltipWidth = Render2D.getStringWidth(tooltip) + 2;
			int tooltipHeight = 10;

			// Save the current matrix state
			matrixStack.push();

			// Make sure we're rendering on top
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableCull();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			// Draw tooltip background
			Render2D.drawRoundedBox(matrixStack.peek().getPositionMatrix(),
					mouseX + 12, mouseY + 12,
					(tooltipWidth + 4) * 2,
					(tooltipHeight + 4) * 2,
					GuiManager.roundingRadius.getValue(),
					GuiManager.atsubackround2.getValue());

			// Draw tooltip text
			int startColor;
			int endColor;
			switch (GuiManager.guistyle.getValue()) {
				case Atsu:
					startColor = new Color(182, 220, 255, 255).getColorAsInt();
					endColor = new Color(185, 182, 229, 255).getColorAsInt();
					break;
				case Rias:
					startColor = new Color(210, 41, 44, 255).getColorAsInt();
					endColor = new Color(86, 24, 27, 255).getColorAsInt();
					break;
				case Captain:
					startColor = new Color(147, 90, 160, 255).getColorAsInt();
					endColor = new Color(137, 166, 210, 255).getColorAsInt();
					break;
				default:
					startColor = new Color(182, 220, 255, 255).getColorAsInt();
					endColor = new Color(185, 182, 229, 255).getColorAsInt();
			}
			Render2D.drawGradientString(drawContext, tooltip, mouseX + 18, mouseY + 18, startColor, endColor);

			// Restore the matrix state
			matrixStack.pop();

			// Reset render states
			RenderSystem.enableCull();
			RenderSystem.disableBlend();
		}
	}
	/**
	 * Gets whether or not the Click GUI is currently open.
	 *
	 * @return State of the Click GUI.
	 */
	public boolean isClickGuiOpen() {
		return this.clickGuiOpen;
	}
	public void setClickGuiOpen(boolean state) {
		this.clickGuiOpen = state;
		setTooltip(null);

		if (!state) {
			// Close all open HUD options windows when closing the GUI
			List<String> openHudIds = new ArrayList<>(openHudOptionsIds);
			for (String hudId : openHudIds) {
				hideHudOptionsWindow(hudId);
			}
		}
	}

	private final Set<String> openHudOptionsIds = new HashSet<>();

	public void showHudOptionsWindow(String hudElementId) {
		switch (hudElementId) {
			case "PlayerModelHud":
				hudPane.addWindow(playerModelHudOptionsWindow);
				break;
			case "NewArrayList":
				hudPane.addWindow(newArrayListOptionsWindow);
				break;
			case "ArmorHud":
				hudPane.addWindow(armorHudOptionsWindow);
				break;
			case "PrideSheep":
				hudPane.addWindow(prideSheepOptionsWindow);
				break;
			case "DVDLogo":
				hudPane.addWindow(dvdLogoOptionsWindow);
				break;
			case "DubCount":
				hudPane.addWindow(dubCountOptionsWindow);
				break;
			case "ShulkerCount":
				hudPane.addWindow(shulkCountHudOptionsWindow);
				break;
			case "ModuleArrayListHud":
				hudPane.addWindow(moduleArrayListHudOptionsWindow);
				break;
			case "WatermarkHud":
				hudPane.addWindow(watermarkHudOptionsWindow);
				break;
			case "CoordsHud":
				hudPane.addWindow(coordsHudOptionsWindow);
				break;
			case "Speedometer":
				hudPane.addWindow(speedNumberHudOptionsWindow);
				break;
			case "AccelerationHUD":
				hudPane.addWindow(accelerationHudOptionsWindow);
				break;
			case "SentPacketHUD":
				hudPane.addWindow(sentPacketHudOptionsWindow);
				break;
			case "SpeedHUD":
				hudPane.addWindow(speedHUDOptionsWindow);
				break;
			case "FakeCoords":
				hudPane.addWindow(fakeCoordsOptionsWindow);
				break;
			case "IncomingPacketHUD":
				hudPane.addWindow(incomingPacketHudOptionsWindow);
				break;
			case "DirectionHud":
				hudPane.addWindow(directionHudOptionsWindow);
				break;
			default:

				System.out.println("No options window for HUD: " + hudElementId);
				return;
		}

		openHudOptionsIds.add(hudElementId);
	}

	public void hideHudOptionsWindow(String hudElementId) {
		switch (hudElementId) {
			case "PlayerModelHud":
				hudPane.removeWindow(playerModelHudOptionsWindow);
				break;
			case "NewArrayList":
				hudPane.removeWindow(newArrayListOptionsWindow);
				break;
			case "ArmorHud":
				hudPane.removeWindow(armorHudOptionsWindow);
				break;
			case "AccelerationHUD":
				hudPane.removeWindow(accelerationHudOptionsWindow);
				break;
			case "SpeedHUD":
				hudPane.removeWindow(speedHUDOptionsWindow);
				break;
			case "SentPacketHUD":
				hudPane.removeWindow(sentPacketHudOptionsWindow);
				break;
			case "IncomingPacketHUD":
				hudPane.removeWindow(incomingPacketHudOptionsWindow);
				break;
			case "Obfuscated":
				hudPane.removeWindow(obfuscatedOptionsWindow);
				break;
			case "DubCount":
				hudPane.removeWindow(dubCountOptionsWindow);
				break;
			case "ShulkerCount":
				hudPane.removeWindow(shulkCountHudOptionsWindow);
				break;
			case "FakeCoords":
				hudPane.removeWindow(fakeCoordsOptionsWindow);
				break;
			case "PrideSheep":
				hudPane.removeWindow(prideSheepOptionsWindow);
				break;
			case "DVDLogo":
				hudPane.removeWindow(dvdLogoOptionsWindow);
				break;
			case "ModuleArrayListHud":
				hudPane.removeWindow(moduleArrayListHudOptionsWindow);
				break;
			case "WatermarkHud":
				hudPane.removeWindow(watermarkHudOptionsWindow);
				break;
			case "CoordsHud":
				hudPane.removeWindow(coordsHudOptionsWindow);
				break;
			case "Speedometer":
				hudPane.removeWindow(speedNumberHudOptionsWindow);
				break;
			case "DirectionHud":
				hudPane.removeWindow(directionHudOptionsWindow);
				break;
			default:
				System.out.println("No options window to close for HUD: " + hudElementId);
				return;
		}

		openHudOptionsIds.remove(hudElementId);
	}

	public boolean isHudOptionsWindowOpen(String hudElementId) {
		return openHudOptionsIds.contains(hudElementId);
	}

	public Framebuffer getFrameBuffer() {
		return guiFrameBuffer;
	}

	public List<UIElement> getAllVisibleElements() {
		List<UIElement> visibleElements = new ArrayList<>();
		for (Window hud : pinnedHuds.values()) {
			if (hud.isVisible()) {
				visibleElements.add(hud);
			}
		}
		return visibleElements;
	}
	/**
	 * Locks and unlocks the Mouse.
	 */
	public void toggleMouse() {
		if (MC.mouse.isCursorLocked()) {
			MC.mouse.unlockCursor();
		} else {
			MC.mouse.lockCursor();
		}
	}
}
