package net.payload.gui.navigation.huds;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.module.Module;
import net.payload.settings.SettingManager;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.payload.utils.Formatting;

import static net.payload.gui.GuiManager.*;

public class ModuleArrayListHud extends HudWindow {
	private static final int MODULE_SPACING = 20;
	private static final int VERTICAL_PADDING = 10;
	private static final long DIMENSION_UPDATE_INTERVAL = 5;

	private List<Module> cachedEnabledModules;
	private long lastDimensionUpdate;
	private float cachedWidth;
	private float currentScale = 1.0f;

	public ModuleArrayListHud(int x, int y, FloatSetting scale) {
		super("ModuleArrayListHud", x, y);
		resizeMode = ResizeMode.None;

		SettingManager.registerSetting(sortingMode);
		SettingManager.registerSetting(GuiManager.silly);
		SettingManager.registerSetting(GuiManager.moduleArraySizeMode);
		SettingManager.registerSetting(GuiManager.arraylistcolor);
		SettingManager.registerSetting(GuiManager.obfarraylistcolor);

		updateSize();
		updateDimensions(true);
	}

	private void updateSize() {
		switch (moduleArraySizeMode.getValue()) {
			case SMALL -> currentScale = 1.0f;
			case LARGE -> currentScale = 3.0f;
			case HUGE -> currentScale = 5.0f;
			default -> currentScale = 2.0f;
		}
	}

	private void updateDimensions(boolean force) {
		long currentTime = System.currentTimeMillis();
		if (!force && currentTime - lastDimensionUpdate < DIMENSION_UPDATE_INTERVAL) {
			return;
		}

		updateSize();
		float horizontalPadding = 4.0f;

		cachedEnabledModules = PAYLOAD.moduleManager.modules.stream()
				.filter(mod -> mod.state.getValue())
				.collect(Collectors.toList());

		cachedWidth = cachedEnabledModules.stream()
				.map(mod -> Render2D.getStringWidth(mod.getName()))
				.max(Float::compare)
				.orElse(0);

		float totalWidth = (cachedWidth + (horizontalPadding * 2)) * currentScale * 2;
		float totalHeight = (cachedEnabledModules.size() * MODULE_SPACING + VERTICAL_PADDING) * currentScale + 10;

		position.setValue(new Rectangle(
				position.getValue().getX(),
				position.getValue().getY(),
				totalWidth,
				totalHeight
		));

		this.setWidth(totalWidth);
		this.setHeight(totalHeight);
		this.minWidth = totalWidth;
		this.maxWidth = totalWidth;
		this.minHeight = totalHeight;
		this.maxHeight = totalHeight;

		lastDimensionUpdate = currentTime;
	}

	@Override
	public void update() {
		super.update();
		updateDimensions(false);
	}

	private List<Module> getSortedModules() {
		if (cachedEnabledModules == null) {
			updateDimensions(true);
		}

		List<Module> sortedModules = cachedEnabledModules;

		switch (sortingMode.getValue()) {
			case ALPHABET:
				sortedModules.sort(Comparator.comparing(Module::getName));
				break;
			case LENGTH:
				sortedModules.sort((m1, m2) -> Float.compare(
						Render2D.getStringWidth(m2.getName()),
						Render2D.getStringWidth(m1.getName())
				));
				break;
		}

		return sortedModules;
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (!isVisible()) {
			super.draw(drawContext, partialTicks);
			return;
		}

		Rectangle pos = position.getValue();
		if (!pos.isDrawable()) {
			super.draw(drawContext, partialTicks);
			return;
		}

		MatrixStack matrixStack = drawContext.getMatrices();
		matrixStack.push();
		try {
			matrixStack.scale(currentScale, currentScale, currentScale);

			float scaledX = pos.getX() / currentScale;
			float scaledY = pos.getY() / currentScale;

			AtomicInteger index = new AtomicInteger(0);

			switch (silly.getValue()) {
				case DISABLED:
					getSortedModules().forEach(module -> {
						String text = Formatting.BOLD + module.getName();
						float yPos = scaledY + VERTICAL_PADDING + (index.getAndIncrement() * MODULE_SPACING);

						Render2D.drawString(
								drawContext,
								text,
								scaledX,
								yPos,
								GuiManager.arraylistcolor.getValue().getColorAsInt()
						);
					});
					break;

				case ENABLED:
					getSortedModules().stream()
							.sorted(Comparator.comparing(Module::getName))
							.forEach(module -> {
								String text = Formatting.OBFUSCATED.toString() + module.getName();
								float yPos = scaledY + VERTICAL_PADDING + (index.getAndIncrement() * MODULE_SPACING);

								Render2D.drawString(
										drawContext,
										text,
										scaledX,
										yPos,
										GuiManager.obfarraylistcolor.getValue().getColorAsInt()
								);
							});
					break;
			}
		} finally {
			matrixStack.pop();
		}

		super.draw(drawContext, partialTicks);
	}
}