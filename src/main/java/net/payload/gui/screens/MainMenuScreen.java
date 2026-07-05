package net.payload.gui.screens;

import static net.payload.PayloadClient.MC;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.payload.PayloadClient;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;

import net.payload.api.IAddon;
import net.payload.gui.GuiManager;
import net.payload.gui.components.widgets.PayloadButtonWidget;
import net.payload.gui.components.widgets.PayloadImageButtonWidget;
import net.payload.gui.screens.addons.AddonScreen;
import net.payload.utils.render.Render2D;
import net.payload.utils.render.TextureBank;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;

public class MainMenuScreen extends Screen {
	protected static final CubeMapRenderer PAYLOAD_PANORAMA_RENDERER = new CubeMapRenderer(TextureBank.mainmenu_panorama);
	protected static final RotatingCubeMapRenderer PAYLOAD_ROTATING_PANORAMA_RENDERER = new RotatingCubeMapRenderer(
			PAYLOAD_PANORAMA_RENDERER);

	final int LOGO_HEIGHT = 70;
	final int BUTTON_WIDTH = 185;
	final int BUTTON_HEIGHT = 30;
	final int SPACING = 5;

	int smallScreenHeightOffset = 0;

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private String fetchedVersion = null;

	public MainMenuScreen() {
		super(Text.of("Payload Client Main Menu"));

		// Async fetch latest release from GitHub
		executor.execute(new Runnable() {
			@Override
			public void run() {
				fetchLatestVersion();
			}
		});
	}

	private static URI createURI(String url) {
		try {
			URI uri = new URI(url);
			return uri;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void fetchLatestVersion() {
		try {
			HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.NORMAL)
					.build();

			HttpRequest request = HttpRequest
					.newBuilder(
							createURI("https://api.github.com/repos/coltonk9043/Payload-MC-Hacked-Client/releases/latest"))
					.header("User-Agent",
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
					.header("Accept", "application/json").header("Content-Type", "application/x-www-form-urlencoded")
					.GET().build();

			HttpResponse<String> response;
			response = client.send(request, BodyHandlers.ofString());
			String responseString = response.body();

			int status = response.statusCode();
			if (status != HttpURLConnection.HTTP_OK) {
				throw new IllegalArgumentException("Device token could not be fetched. Invalid status code " + status);
			}

			JsonObject json = new Gson().fromJson(responseString, JsonObject.class);
			String tagName = json.get("tag_name").getAsString();
			if (tagName != null)
				fetchedVersion = tagName;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void init() {
		super.init();

		if (this.height <= 650)
			smallScreenHeightOffset = 40;
		else
			smallScreenHeightOffset = 0;

		float widgetHeight = ((BUTTON_HEIGHT + SPACING) * 5);
		int startX = (int) ((this.width - this.BUTTON_WIDTH) / 2.0f);
		int startY = (int) ((this.height - widgetHeight) / 2) + smallScreenHeightOffset;

		// TODO: Left Alignment uses X coordinate of 50. Use this once news is done!
		PayloadButtonWidget singleplayerButton = new PayloadButtonWidget(startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT,
				Text.of("Singleplayer"));
		singleplayerButton.setPressAction(b -> client.setScreen(new SelectWorldScreen(this)));
		this.addDrawableChild(singleplayerButton);

		PayloadButtonWidget multiplayerButton = new PayloadButtonWidget(startX, startY + BUTTON_HEIGHT + SPACING,
				BUTTON_WIDTH, BUTTON_HEIGHT, Text.of("Multiplayer"));
		multiplayerButton.setPressAction(b -> client.setScreen(new MultiplayerScreen(this)));
		this.addDrawableChild(multiplayerButton);

		PayloadButtonWidget settingsButton = new PayloadButtonWidget(startX, startY + ((BUTTON_HEIGHT + SPACING) * 2),
				BUTTON_WIDTH, BUTTON_HEIGHT, Text.of("Settings"));
		settingsButton.setPressAction(b -> client.setScreen(new OptionsScreen(this, MC.options)));
		this.addDrawableChild(settingsButton);

		PayloadButtonWidget quitButton = new PayloadButtonWidget(startX, startY + ((BUTTON_HEIGHT + SPACING) * 4),
				BUTTON_WIDTH, BUTTON_HEIGHT, Text.of("Quit"));
		quitButton.setPressAction(b -> client.stop());
		this.addDrawableChild(quitButton);
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		super.render(drawContext, mouseX, mouseY, delta);

		RenderSystem.disableCull();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float widgetHeight = ((BUTTON_HEIGHT + SPACING) * 5);
		int startX = (int) ((this.width - BUTTON_WIDTH) / 2.0f);
		int startY = (int) ((this.height - widgetHeight) / 2) - LOGO_HEIGHT - 10 + smallScreenHeightOffset;

		drawContext.drawTexture(RenderLayer::getGuiTextured, TextureBank.mainmenu_logo, startX, startY, 0, 0,
				BUTTON_WIDTH, LOGO_HEIGHT, 185, LOGO_HEIGHT);

	}

	@Override
	protected void renderPanoramaBackground(DrawContext context, float delta) {
		PAYLOAD_ROTATING_PANORAMA_RENDERER.render(context, this.width, this.height, 1.0f, delta);
	}
}