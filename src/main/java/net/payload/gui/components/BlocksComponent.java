package net.payload.gui.components;

import net.payload.Payload;
import org.joml.Matrix4f;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.events.MouseScrollEvent;
import net.payload.event.events.KeyDownEvent;
import net.payload.event.listeners.MouseScrollListener;
import net.payload.event.listeners.KeyDownListener;
import net.payload.gui.GuiManager;
import net.payload.gui.Margin;
import net.payload.gui.Rectangle;
import net.payload.gui.Size;
import net.payload.gui.colors.Color;
import net.payload.settings.types.BlocksSetting;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;
import java.util.*;

import static net.payload.gui.GuiStyle.Rias;

public class BlocksComponent extends Component implements MouseScrollListener, KeyDownListener {
	private static final float BLOCK_WIDTH = 32f;
	private static final float BLOCK_MARGIN = 4f;
	private static final float COLLAPSED_HEIGHT = 30f;
	private static final float EXPANDED_HEIGHT = 165f;
	private static final float SEARCH_HEIGHT = 36f;  // Changed from 30f to 36f (1.2x)
	private static final float SEARCH_TEXT_SCALE = 0.96f;  // Changed from 0.8f to 0.96f (1.2x)
	private static final long DOT_ANIMATION_INTERVAL = 500;
	private static final float ANIMATION_SPEED = 12f;
	private static final float SNAP_THRESHOLD = 0.001f;
	private static final long SEARCH_TIMEOUT = 5000; // 5 seconds in milliseconds
	private long lastTypeTime = 0;
	private boolean collapsed = true;
	private final BlocksSetting blocks;
	private final String text;
	private final List<Block> filteredBlocks;
	private final Map<Block, Float> blockAnimations = new HashMap<>();
	private int visibleRows;
	private int visibleColumns;
	private int scroll = 0;
	private boolean isSearchFocused = false;
	private String searchText = "";
	private long lastDotAnimation = System.currentTimeMillis();
	private long lastUpdateTime = System.currentTimeMillis();
	private int dotCount = 1;    public BlocksComponent(BlocksSetting setting) {
		super();
		this.text = setting.displayName;
		this.blocks = setting;
		this.filteredBlocks = new ArrayList<>();
		this.setMargin(new Margin(4f, null, 4f, null));
		updateFilteredBlocks();
	}

	private void updateFilteredBlocks() {
		filteredBlocks.clear();
		String search = searchText.toLowerCase();

		for (Block block : Registries.BLOCK) {
			String blockName = block.getName().getString().toLowerCase();
			if (search.isEmpty() || blockName.contains(search)) {
				filteredBlocks.add(block);
			}
		}
	}

	@Override
	public void update() {
		super.update();

		long currentTime = System.currentTimeMillis();

		if (isSearchFocused && (currentTime - lastTypeTime) > SEARCH_TIMEOUT) {
			isSearchFocused = false;
			if (Payload.getInstance().eventManager.isListenerRegistered(KeyDownListener.class, this)) {
				Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
			}
		}

		if (currentTime - lastDotAnimation > DOT_ANIMATION_INTERVAL) {
			dotCount = (dotCount % 3) + 1;
			lastDotAnimation = currentTime;
		}


		float deltaTime = (currentTime - lastUpdateTime) / 1000f;
		lastUpdateTime = currentTime;

		Iterator<Map.Entry<Block, Float>> it = blockAnimations.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Block, Float> entry = it.next();
			Block block = entry.getKey();
			float animation = entry.getValue();

			float targetValue = blocks.getValue().contains(block) ? 1.0f : 0.0f;
			float factor = 1 - (float) Math.exp(-ANIMATION_SPEED * deltaTime);
			float newValue = lerp(animation, targetValue, factor);

			if (Math.abs(newValue - targetValue) < SNAP_THRESHOLD) {
				if (targetValue == 0f) {
					it.remove();
				} else {
					entry.setValue(targetValue);
				}
			} else {
				entry.setValue(newValue);
			}
		}
	}

	private float lerp(float start, float end, float factor) {
		return start + factor * (end - start);
	}

	@Override
	public void measure(Size availableSize) {
		if (collapsed) {
			preferredSize = new Size(availableSize.getWidth(), COLLAPSED_HEIGHT);
		} else {
			preferredSize = new Size(availableSize.getWidth(), EXPANDED_HEIGHT + 85f);
		}
		visibleColumns = (int) Math.floor((preferredSize.getWidth() - 8) / (BLOCK_WIDTH + BLOCK_MARGIN));
		visibleRows = (int) Math.floor((preferredSize.getHeight() - (25 + SEARCH_HEIGHT)) / (BLOCK_WIDTH + BLOCK_MARGIN));
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		MatrixStack matrixStack = drawContext.getMatrices();
		Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

		float actualX = this.getActualSize().getX();
		float actualY = this.getActualSize().getY();
		float actualWidth = this.getActualSize().getWidth();
		Render2D.drawString(drawContext, collapsed ? ">>" : "<<",
				Math.round(actualX + actualWidth - 24),
				Math.round(actualY + 6),
				GuiManager.foregroundColor.getValue().getColorAsInt());
		if (!collapsed) {
			// Draw header
			Render2D.drawString(drawContext, text, Math.round(actualX), Math.round(actualY + 6), 0xFFFFFF);

			// Draw search box
			float searchY = actualY + 25;
			switch (GuiManager.guistyle.get()) {
				case Atsu:
					Render2D.drawHorizontalGradient(matrix4f, actualX + 4, searchY, actualWidth - 8, SEARCH_HEIGHT - 8,
							new Color(182, 220, 255, 155),
							new Color(185, 182, 229, 155));

					if (isSearchFocused) {
						Render2D.drawBoxOutline(matrix4f, actualX + 4, searchY, actualWidth - 8, SEARCH_HEIGHT - 8,
								new Color(255, 255, 255, 255));
					} else {
						Render2D.drawBoxOutline(matrix4f, actualX + 4, searchY, actualWidth - 8, SEARCH_HEIGHT - 8,
								GuiManager.borderColor.getValue());
					}
					break;
				case Rias:
					Render2D.drawHorizontalGradient(matrix4f, actualX + 4, searchY, actualWidth - 8, SEARCH_HEIGHT - 8,
							new Color(255, 0, 0, 155),
							new Color(66, 0, 0, 155));

					if (isSearchFocused) {
						Render2D.drawBoxOutline(matrix4f, actualX + 4, searchY, actualWidth - 8, SEARCH_HEIGHT - 8,
								new Color(255, 255, 255, 255));
					} else {
						Render2D.drawBoxOutline(matrix4f, actualX + 4, searchY, actualWidth - 8, SEARCH_HEIGHT - 8,
								GuiManager.borderColor.getValue());
					}
			}
			matrixStack.push();
			float scaledX = actualX + 8;
			float scaledY = searchY + (SEARCH_HEIGHT - 8) / 2 - 6; // Adjusted for new height
			matrixStack.translate(scaledX, scaledY, 0);
			matrixStack.scale(SEARCH_TEXT_SCALE, SEARCH_TEXT_SCALE, 1.0f);
			matrixStack.translate(-scaledX, -scaledY, 0);

			if (!searchText.isEmpty()) {
				Render2D.drawString(drawContext, searchText,
						Math.round(scaledX),
						Math.round(scaledY),
						0xFFFFFF);
			} else if (!isSearchFocused) {
				String dots = "Search" + ".".repeat(dotCount);
				Render2D.drawString(drawContext, dots,
						Math.round(scaledX),
						Math.round(scaledY),
						0xFFFFFF);
			}
			matrixStack.pop();
			float blocksStartY = actualY + 25 + SEARCH_HEIGHT;

			// Draw blocks with animations
			for (int i = 0; i < filteredBlocks.size(); i++) {
				int row = i / visibleColumns;
				int col = i % visibleColumns;

				if (row >= scroll && row < scroll + visibleRows) {
					Block block = filteredBlocks.get(i);
					float blockX = actualX + 4 + (col * (BLOCK_WIDTH + BLOCK_MARGIN));
					float blockY = blocksStartY + ((row - scroll) * (BLOCK_WIDTH + BLOCK_MARGIN));

					// Draw base background
					Color baseColor = new Color(22, 22, 22, 200);
					Render2D.drawBox(matrix4f, blockX, blockY, BLOCK_WIDTH + 4, BLOCK_WIDTH + 4, baseColor);

					// Draw animated selection
					if (blocks.getValue().contains(block)) {
						float animation = blockAnimations.computeIfAbsent(block, k -> 1.0f);
						switch (GuiManager.guistyle.getValue()) {
							case Atsu:
								Color startColor = new Color(182, 220, 255, (int) (155 * animation));
								Color endColor = new Color(185, 182, 229, (int) (155 * animation));
								Render2D.drawHorizontalGradient(matrix4f, blockX, blockY, BLOCK_WIDTH, BLOCK_WIDTH,
										startColor, endColor);

								Color outlineColor = new Color(100, 181, 255, (int) (255 * animation));
								Render2D.drawBoxOutline(matrix4f, blockX, blockY, BLOCK_WIDTH, BLOCK_WIDTH, outlineColor);
								break;
							case Rias:
								Color riasstart = new Color(210, 41, 44, (int) (155 * animation));
								Color riasend = new Color(86, 24, 27, (int) (155 * animation));
								Render2D.drawHorizontalGradient(matrix4f, blockX, blockY, BLOCK_WIDTH, BLOCK_WIDTH,
										riasstart, riasend);

								Color riasoutline = new Color(155, 0, 0, (int) (255 * animation));
								Render2D.drawBoxOutline(matrix4f, blockX, blockY, BLOCK_WIDTH, BLOCK_WIDTH, riasoutline);
								break;
						}

					}
				}
			}

			// Draw items
			matrixStack.push();
			matrixStack.scale(2.0f, 2.0f, 2.0f);

			for (int i = 0; i < filteredBlocks.size(); i++) {
				int row = i / visibleColumns;
				int col = i % visibleColumns;

				if (row >= scroll && row < scroll + visibleRows) {
					Block block = filteredBlocks.get(i);
					float blockX = actualX + 4 + (col * (BLOCK_WIDTH + BLOCK_MARGIN));
					float blockY = blocksStartY + ((row - scroll) * (BLOCK_WIDTH + BLOCK_MARGIN));

					Render2D.drawItem(drawContext,
							new ItemStack(block.asItem()),
							(int) ((blockX + 1) / 2.0f),
							(int) ((blockY + 1) / 2.0f));
				}
			}
			matrixStack.pop();
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		if (event.button != MouseButton.LEFT || event.action != MouseAction.DOWN || !hovered)
			return;

		float mouseX = (float) event.mouseX;
		float mouseY = (float) event.mouseY;
		float actualX = actualSize.getX();
		float actualY = actualSize.getY();
		float actualWidth = actualSize.getWidth();
		float actualHeight = actualSize.getHeight();
		Rectangle collapseHitbox = new Rectangle(actualX + 4, actualY, actualWidth - 8, 24.0f);
		if (collapseHitbox.intersects(mouseX, mouseY)) {
			collapsed = !collapsed;
			invalidateMeasure();
			event.cancel();
			return;
		}
		if (!collapsed) {
			Rectangle searchBox = new Rectangle(actualX + 4, actualY + 25, actualWidth - 8, SEARCH_HEIGHT - 8);
			if (searchBox.intersects(mouseX, mouseY)) {
				isSearchFocused = true;
				lastTypeTime = System.currentTimeMillis(); // Reset timer when focusing
				if (!Payload.getInstance().eventManager.isListenerRegistered(KeyDownListener.class, this)) {
					Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
				}
				event.cancel();
				return;
			} else {
				isSearchFocused = false;
				if (Payload.getInstance().eventManager.isListenerRegistered(KeyDownListener.class, this)) {
					Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
				}
			}
		}

		float blocksStartY = actualY + 25 + SEARCH_HEIGHT;
		float blocksArea = actualHeight - (25 + SEARCH_HEIGHT);
		Rectangle blockHitbox = new Rectangle(actualX + 4, blocksStartY, actualWidth - 8, blocksArea);

		if (blockHitbox.intersects(mouseX, mouseY)) {
			float relativeX = mouseX - (actualX + 4);
			float relativeY = mouseY - blocksStartY;

			int col = (int) (relativeX / (BLOCK_WIDTH + BLOCK_MARGIN));
			int row = (int) (relativeY / (BLOCK_WIDTH + BLOCK_MARGIN)) + scroll;

			if (col >= 0 && col < visibleColumns) {
				int index = (row * visibleColumns) + col;
				if (index >= 0 && index < filteredBlocks.size()) {
					Block block = filteredBlocks.get(index);
					if (block != null) {
						if (blocks.getValue().contains(block)) {
							blocks.getValue().remove(block);
							blockAnimations.put(block, 1.0f);
						} else {
							blocks.getValue().add(block);
							blockAnimations.put(block, 0.0f);
						}
						blocks.update();
						event.cancel();
					}
				}
			}
		}
	}

	@Override
	public void onMouseScroll(MouseScrollEvent event) {
		if (Payload.getInstance().guiManager.isClickGuiOpen() && this.hovered) {
			int maxScroll = Math.max(0, (filteredBlocks.size() / visibleColumns) - visibleRows);

			if (event.GetVertical() > 0 && scroll > 0) {
				scroll--;
				event.cancel();
			} else if (event.GetVertical() < 0 && scroll < maxScroll) {
				scroll++;
				event.cancel();
			}
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (!isSearchFocused) return;

		event.cancel();
		int key = event.GetKey();
		lastTypeTime = System.currentTimeMillis();

		if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_GRAVE_ACCENT) {
			isSearchFocused = false;
			Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
			return;
		}

		if (key == GLFW.GLFW_KEY_BACKSPACE) {
			if (!searchText.isEmpty()) {
				searchText = searchText.substring(0, searchText.length() - 1);
				updateFilteredBlocks();
				scroll = 0;
			}
		} else if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
			char keyChar = (char) key;
			if (!Screen.hasShiftDown()) {
				keyChar = Character.toLowerCase(keyChar);
			}
			searchText += keyChar;
			updateFilteredBlocks();
			scroll = 0;
		} else if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
			searchText += (char) key;
			updateFilteredBlocks();
			scroll = 0;
		} else if (key == GLFW.GLFW_KEY_SPACE) {
			searchText += " ";
			updateFilteredBlocks();
			scroll = 0;
		}
	}

	@Override
	public void onVisibilityChanged() {
		super.onVisibilityChanged();
		if (this.isVisible()) {
			Payload.getInstance().eventManager.AddListener(MouseScrollListener.class, this);
		} else {
			Payload.getInstance().eventManager.RemoveListener(MouseScrollListener.class, this);
			Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
			isSearchFocused = false;
			searchText = "";
		}
	}
}