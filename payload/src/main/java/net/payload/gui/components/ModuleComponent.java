package net.payload.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import kroppeb.stareval.function.Type;
import net.minecraft.client.MinecraftClient;
import net.payload.SoundGenerator;
import net.payload.gui.*;
import net.payload.gui.navigation.Page;
import net.payload.settings.PageGroup;
import net.payload.settings.SettingGroup;
import net.payload.settings.types.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import java.awt.Toolkit;
import java.awt.Dimension;
import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.CloseableWindow;
import net.payload.module.Module;
import net.payload.settings.Setting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import static net.payload.gui.GuiStyle.Rias;
import static net.payload.utils.render.TextureBank.*;

import java.util.ArrayList;
import java.util.Random;

public class ModuleComponent extends Component {
	private Module module;
	private boolean spinning = false;
	private float spinAngle = 0;
	private float glowPhase = 0f;
	private static final float GLOW_SPEED = 2.5f;
	private static final float BASE_ALPHA = 0.6f;
	private static final float GLOW_INTENSITY = 0.4f;
	private long lastUpdateTime;

	private CloseableWindow lastSettingsTab = null;
	private static final ArrayList<Vector2f> openwin = new ArrayList<>();
	private static final Random random = new Random();
	private Vector2f lastwindowpos = null;

	private boolean expanded = false;
	private StackPanelComponent settingsPanel;
	private float collapsedHeight = 26.0f;
	private float expandedHeight;

	public ModuleComponent(Module module) {
		super();
		this.header = module.getName();
		this.module = module;
		this.tooltip = module.getDescription();
		this.setMargin(new Margin(8f, null, 8f, null));
		this.lastUpdateTime = System.currentTimeMillis();

		if (module.hasSettings()) {
			if (GuiManager.modulesettingsstyle.getValue() == ModuleSettingsStyle.Collapsed) {
				initializeSettingsPanel();
			}
		}
	}

	private void initializeSettingsPanel() {
		settingsPanel = new StackPanelComponent();
		settingsPanel.setVisible(false);
		settingsPanel.setMargin(new Margin(15f, 10f, 8f, 5f));

		for (Setting<?> setting : module.getSettings()) {
			if (setting == module.state)
				continue;

			Component c = createSettingComponent(setting);
			if (c != null) {
				settingsPanel.addChild(c);
			}
		}

		KeybindComponent keybindComponent = new KeybindComponent(module.getBind());
		settingsPanel.addChild(keybindComponent);
		this.addChild(settingsPanel);
	}

	private Component createSettingComponent(Setting<?> setting) {
		if (setting instanceof FloatSetting) {
			return new SliderComponent((FloatSetting) setting);
		} else if (setting instanceof BooleanSetting) {
			return new CheckboxComponent((BooleanSetting) setting);
		} else if (setting instanceof ColorSetting) {
			return new ColorPickerComponent((ColorSetting) setting);
		} else if (setting instanceof BlocksSetting) {
			return new BlocksComponent((BlocksSetting) setting);
		} else if (setting instanceof EnumSetting) {
			return new EnumComponent<>((EnumSetting) setting);
		} else if (setting instanceof SettingGroup) {
			SettingGroup group = (SettingGroup) setting;
			CollapsiblePanelComponent panel = new CollapsiblePanelComponent(group.displayName);

			StackPanelComponent stackPanel = new StackPanelComponent();
			stackPanel.setMargin(new Margin(10f, 0f, 0f, 0f));

			for (Setting<?> groupSetting : group.getSettings()) {
				Component settingComponent = createSettingComponent(groupSetting);
				if (settingComponent != null) {
					stackPanel.addChild(settingComponent);
				}
			}

			panel.addChild(stackPanel);
			return panel;
		} else if (setting instanceof PageGroup) { // Add this new condition
			PageGroup pageGroup = (PageGroup) setting;
			PageComponent pageComponent = new PageComponent(pageGroup.displayName);

			for (PageGroup.Page page : pageGroup.getPages()) {
				PageComponent.Page componentPage = new PageComponent.Page(page.getName());

				for (Setting<?> pageSetting : page.getSettings()) {
					Component settingComponent = createSettingComponent(pageSetting);
					if (settingComponent != null) {
						componentPage.addComponent(settingComponent);
					}
				}

				pageComponent.addPage(componentPage);
			}

			return pageComponent;
		}
		return null;
	}

	@Override
	public void measure(Size availableSize) {
		switch (GuiManager.modulesettingsstyle.getValue()) {
			case Collapsed:
				if (expanded && settingsPanel != null) {
					settingsPanel.measure(availableSize);
					expandedHeight = collapsedHeight + settingsPanel.getPreferredSize().getHeight();
					preferredSize = new Size(availableSize.getWidth(), expandedHeight);
				} else {
					preferredSize = new Size(availableSize.getWidth(), collapsedHeight);
				}
				break;
			case Popout:
				preferredSize = new Size(availableSize.getWidth(), 26.0f);
				break;
		}
	}

	@Override
	public void arrange(Rectangle finalSize) {
		super.arrange(finalSize);

		if (GuiManager.modulesettingsstyle.getValue() == ModuleSettingsStyle.Collapsed
				&& expanded && settingsPanel != null) {
			Rectangle settingsBounds = new Rectangle(
					finalSize.getX(),
					finalSize.getY() + collapsedHeight,
					finalSize.getWidth(),
					expandedHeight - collapsedHeight
			);
			settingsPanel.arrange(settingsBounds);
		}
	}

	@Override
	public void update() {
		super.update();
		long currentTime = System.currentTimeMillis();
		float deltaTime = (currentTime - lastUpdateTime) / 1000f;


		boolean shouldSpin = spinning &&
				(GuiManager.modulesettingsstyle.getValue() == ModuleSettingsStyle.Popout ||
						(GuiManager.modulesettingsstyle.getValue() == ModuleSettingsStyle.Collapsed && expanded));

		if (shouldSpin) {
			glowPhase += GLOW_SPEED * deltaTime;
			spinAngle = (spinAngle + 3) % 360;
		} else {
			glowPhase = 0f;
			spinAngle = 0;
			spinning = false;
		}

		lastUpdateTime = currentTime;

		if (GuiManager.modulesettingsstyle.getValue() == ModuleSettingsStyle.Collapsed
				&& expanded && settingsPanel != null) {
			settingsPanel.update();
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		super.draw(drawContext, partialTicks);

		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		Color hoverColor = new Color(200, 200, 200, 55);

		if (hovered) {
			float padding = 2.0f;
			Render2D.drawBox(matrix4f, actualX - padding - 5, actualY + 3 - padding,
					actualWidth + 10 + padding * 2, 24 + padding * 2, hoverColor);
		}

		if (this.header != null) {
			if (module.state.getValue()) {
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
						break;
				}

				Render2D.drawGradientString(drawContext, this.header,
						Math.round(actualX),
						Math.round(actualY + 8),
						startColor, endColor);
			} else {
				Render2D.drawString(drawContext, this.header,
						Math.round(actualX),
						Math.round(actualY + 8),
						hovered ? 0xFFFFFF : 0x9B9B9B);
			}
		}

		if (module.hasSettings()) {
			if (spinning) {
				float alphaMod = (float) (Math.sin(glowPhase) * GLOW_INTENSITY) + BASE_ALPHA;
				int alpha = (int) (255 * alphaMod);

				matrixStack.push();
				matrixStack.translate(actualX + actualWidth - 6, actualY + 14, 0);
				matrixStack.multiply(new Quaternionf().rotateZ((float) Math.toRadians(spinAngle)));
				matrixStack.translate(-(actualX + actualWidth - 6), -(actualY + 14), 0);

				Color glowColor = new Color(255, 255, 255, alpha);
				switch (GuiManager.guistyle.getValue()) {
					case Atsu:
						Render2D.drawTexturedQuad(
								matrixStack.peek().getPositionMatrix(),
								thugbait,
								Math.round(actualX + actualWidth - 14),
								Math.round(actualY + 6),
								16, 16,
								glowColor);
						break;
					case Rias:
						Render2D.drawTexturedQuad(
								matrixStack.peek().getPositionMatrix(),
								riascog,
								Math.round(actualX + actualWidth - 14),
								Math.round(actualY + 6),
								16, 16,
								glowColor);
						break;
					case Captain:
						Render2D.drawTexturedQuad(
								matrixStack.peek().getPositionMatrix(),
								captaincog,
								Math.round(actualX + actualWidth - 14),
								Math.round(actualY + 6),
								16, 16,
								glowColor);
						break;
				}
				matrixStack.pop();
			}
		}

		// Draw settings panel if expanded in Collapsed mode
		if (GuiManager.modulesettingsstyle.getValue() == ModuleSettingsStyle.Collapsed
				&& expanded && settingsPanel != null) {
			settingsPanel.draw(drawContext, partialTicks);
		}
	}
	@Override
	public void onMouseClick(MouseClickEvent event) {
		if (hovered) {
			if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
				module.toggle();
				event.cancel();
			}
			else if (event.button == MouseButton.RIGHT && event.action == MouseAction.DOWN) {
				// Close any other module's settings first
				if (getParent() != null) {
					for (UIElement child : getParent().getChildren()) {
						if (child instanceof ModuleComponent && child != this) {
							((ModuleComponent) child).closeAllSettingsWindows();
						}
					}
				}

				spinning = true;

				switch (GuiManager.modulesettingsstyle.getValue()) {
					case Popout:
						handlePopoutClick();
						break;
					case Collapsed:
						handleCollapsedClick();
						break;
				}
				event.cancel();
			}
		}

		if (GuiManager.modulesettingsstyle.getValue() == ModuleSettingsStyle.Collapsed
				&& expanded) {
			super.onMouseClick(event);
		}
	}

	public void closeAllSettingsWindows() {
		if (lastSettingsTab != null) {
			Payload.getInstance().guiManager.removeWindow(lastSettingsTab, "");
			spinning = false;
			openwin.remove(lastwindowpos);
			lastwindowpos = null;
			lastSettingsTab = null;
		}

		if (expanded) {
			expanded = false;
			if (settingsPanel != null) {
				settingsPanel.setVisible(false);
			}
			invalidateMeasure();
		}
	}

	private void handlePopoutClick() {
		// Close any existing Collapsed style panels first
		if (expanded) {
			expanded = false;
			if (settingsPanel != null) {
				settingsPanel.setVisible(false);
			}
			invalidateMeasure();
		}

		// Then handle Popout window
		if (lastSettingsTab == null) {
			Vector2f newPosition = randomwindowpos();
			createPopoutWindow(newPosition);
		} else {
			closePopoutWindow();
		}
	}

	private void handleCollapsedClick() {
		// Close any existing Popout windows first
		if (lastSettingsTab != null) {
			closePopoutWindow();
		}

		// Toggle expanded state
		expanded = !expanded;

		// Update spinning state based on expanded state
		spinning = expanded; // The gear should only spin when settings are expanded

		if (settingsPanel != null) {
			settingsPanel.setVisible(expanded);
		}
		invalidateMeasure();
	}

	private void createPopoutWindow(Vector2f position) {
		lastSettingsTab = new CloseableWindow(this.module.getName(), position.x, position.y);
		lastSettingsTab.setMinWidth(320.0f);

		StackPanelComponent stackPanel = new StackPanelComponent();
		HeaderStringComponent titleComponent = new HeaderStringComponent(module.getName());
		titleComponent.setIsHitTestVisible(false);
		stackPanel.addChild(titleComponent);
		stackPanel.addChild(new SeparatorComponent());

		// Add settings
		for (Setting<?> setting : this.module.getSettings()) {
			if (setting == this.module.state)
				continue;

			Component c = createSettingComponent(setting);
			if (c != null) {
				stackPanel.addChild(c);
			}
		}

		KeybindComponent keybindComponent = new KeybindComponent(module.getBind());
		stackPanel.addChild(keybindComponent);
		lastSettingsTab.addChild(stackPanel);

		lastSettingsTab.setOnClose(() -> {
			spinning = false;
			openwin.remove(lastwindowpos);
		});

		// Set window size based on module
		if (this.module.getName().equalsIgnoreCase("XRay") ||
				this.module.getName().equalsIgnoreCase("Nuker") ||
				this.module.getName().equalsIgnoreCase("StashFinder") ||
				this.module.getName().equalsIgnoreCase("Search")) {
			lastSettingsTab.setMinWidth(500.0f);
			lastSettingsTab.setMaxWidth(700.0f);
		} else {
			lastSettingsTab.setMinWidth(250.0f);
			lastSettingsTab.setMaxWidth(250.0f);
		}

		Payload.getInstance().guiManager.addWindow(lastSettingsTab, "");
		lastSettingsTab.initialize();

		openwin.add(position);
		lastwindowpos = position;
	}

	private void closePopoutWindow() {
		Payload.getInstance().guiManager.removeWindow(lastSettingsTab, "");
		spinning = false;
		openwin.remove(lastwindowpos);
		lastwindowpos = null;
		lastSettingsTab = null;
	}

	private Vector2f randomwindowpos() {
		Vector2f randomPosition;
		boolean overlapDetected;
		MinecraftClient client = MinecraftClient.getInstance();
		int screenWidth = client.getWindow().getWidth();
		int screenHeight = client.getWindow().getHeight();
		float minX = screenWidth / 3.5f;
		float minY = screenHeight / 2.0f;
		float maxX = screenWidth - 510;
		float maxY = screenHeight - 200;

		do {
			overlapDetected = false;
			float randomX = random.nextInt((int) (maxX - minX)) + minX;
			float randomY = random.nextInt((int) (maxY - minY)) + minY;
			randomPosition = new Vector2f(randomX, randomY);

			for (Vector2f position : openwin) {
				if (position.distance(randomPosition) < 100) {
					overlapDetected = true;
					break;
				}
			}
		} while (overlapDetected);

		return randomPosition;
	}

	private void invalidateLayout() {
		if (getParent() != null) {
			Size currentSize = new Size(
					getParent().getActualSize().getWidth(),
					getParent().getActualSize().getHeight()
			);
			getParent().measure(currentSize);
			getParent().arrange(getParent().getActualSize());
		}
	}
}