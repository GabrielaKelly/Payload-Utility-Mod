

package net.payload.gui.navigation.huds;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.MathHelper;
import net.payload.gui.ArmorEnum;
import net.payload.gui.ArmorPosition;
import net.payload.gui.Rectangle;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import org.joml.Matrix4f;

import static net.payload.gui.ArmorPosition.*;
import static net.payload.gui.GuiManager.*;

public class ArmorHud extends HudWindow {
	private static final float ITEM_SIZE = 16f;
	private static final float DURABILITY_TEXT_SCALE = 0.50f;
	private static final int DURABILITY_HIGH = 0xFF00FF00;
	private static final int DURABILITY_MEDIUM = 0xFFFFFF00;
	private static final int DURABILITY_LOW = 0xFFFF6600;
	private static final int DURABILITY_CRITICAL = 0xFFFF0000;

	public ArmorHud(int x, int y) {
		super("ArmorHud", x, y);

		SettingManager.registerSetting(armorEnum);
		SettingManager.registerSetting(armorPosition);
		SettingManager.registerSetting(armorSizing);

		updateDimensions();
	}

	private void updateDimensions() {
		float scale = switch (armorSizing.getValue()) {
			case SMALL -> 1.0f;
			case NORMAL -> 2.0f;
			case LARGE -> 3.0f;
			case HUGE -> 5.0f;
		};

		boolean isHorizontal = armorPosition.getValue() == HORIZONTAL;
		float baseWidth = isHorizontal ? 64f : 36f;
		float baseHeight = isHorizontal ? 16f : 64f;

		this.minWidth = this.maxWidth = baseWidth * scale;
		this.minHeight = this.maxHeight = baseHeight * scale;

		Rectangle current = position.getValue();
		position.setValue(new Rectangle(
				current.getX(),
				current.getY(),
				this.minWidth,
				this.minHeight
		));
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks) {
		if (!isVisible() || MC.player == null) {
			super.draw(drawContext, partialTicks);
			return;
		}

		Rectangle pos = position.getValue();
		if (pos.isDrawable()) {
			DefaultedList<ItemStack> armors = MC.player.getInventory().armor;
			float scale = switch (armorSizing.getValue()) {
				case SMALL -> 1.0f;
				case NORMAL -> 2.0f;
				case LARGE -> 3.0f;
				case HUGE -> 5.0f;
			};

			MatrixStack matrixStack = drawContext.getMatrices();
			matrixStack.push();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			try {
				matrixStack.scale(scale, scale, 1.0f);

				if (armorPosition.getValue() == HORIZONTAL) {
					float x = pos.getX() / scale;
					float y = pos.getY() / scale;

					for (int i = armors.size() - 1; i >= 0; i--) {
						ItemStack armor = armors.get(i);
						if (armor.getItem() != Items.AIR) {
							float itemX = x + (ITEM_SIZE * (armors.size() - 1 - i));

							if (armorEnum.getValue() != ArmorEnum.BAR) {
								Render2D.drawItem(drawContext, armor, itemX, y);
							}

							if (armor.isDamageable()) {
								int maxDurability = armor.getMaxDamage();
								int currentDurability = maxDurability - armor.getDamage();
								float percentage = (float) currentDurability / maxDurability * 100;

								final int color = percentage > 75 ? DURABILITY_HIGH :
										percentage > 50 ? DURABILITY_MEDIUM :
												percentage > 25 ? DURABILITY_LOW : DURABILITY_CRITICAL;

								switch (armorEnum.getValue()) {
									case BAR -> {
										if (percentage <= 99) {
											Matrix4f matrix = matrixStack.peek().getPositionMatrix();
											Render2D.drawItem(drawContext, armor, itemX, y);

											Render2D.drawBox(matrix, itemX + 1.34f, y + 12.4f, ITEM_SIZE * 0.8f + 0.3f, ITEM_SIZE * 0.05f + 1.5f,
													new Color(0, 0, 0, 255));

											float barWidth = ITEM_SIZE * (percentage / 100.0f);
											int hsvColor = MathHelper.hsvToRgb(percentage / 300.0f, 1.0f, 1.0f);
											Color barColor = new Color(
													(hsvColor >> 16 & 255),
													(hsvColor >> 8 & 255),
													(hsvColor & 255),
													255
											);

											Render2D.drawBox(matrix, itemX + 1.34f, y + 12.4f,
													barWidth * 0.8f, ITEM_SIZE * 0.05f + 0.4f, barColor);
										}
									}
									case NUMBER -> {
										String text = String.valueOf(currentDurability);
										float textScale = DURABILITY_TEXT_SCALE;
										if (text.length() > 2) textScale *= 0.85f;

										float textWidth = MC.textRenderer.getWidth(text) * textScale;
										float textX = (itemX + (ITEM_SIZE - textWidth) / 2f);
										float textY = (y + ITEM_SIZE + 2);

										Render2D.drawStringWithScale(drawContext, text, textX - 3, textY,
												color, 0.3f);
									}
									case PERCENT -> {
										String text = String.format("%.0f%%", percentage);
										float textScale = DURABILITY_TEXT_SCALE * 0.75f;

										float textWidth = MC.textRenderer.getWidth(text) * textScale;
										float textX = (itemX + (ITEM_SIZE - textWidth) / 2f);
										float textY = (y + ITEM_SIZE + 2);

										Render2D.drawStringWithScale(drawContext, text, textX - 3, textY,
												color, 0.3f);
									}
								}
							}
						}
					}
				} else {
					float x = pos.getX() / scale;
					float y = (pos.getY() + pos.getHeight()) / scale;

					for (int i = 0; i < armors.size(); i++) {
						ItemStack armor = armors.get(i);
						if (armor.getItem() != Items.AIR) {
							float itemY = y - ITEM_SIZE - (ITEM_SIZE * i);

							if (armorEnum.getValue() != ArmorEnum.BAR) {
								Render2D.drawItem(drawContext, armor, x, itemY);
							}

							if (armor.isDamageable()) {
								int maxDurability = armor.getMaxDamage();
								int currentDurability = maxDurability - armor.getDamage();
								float percentage = (float) currentDurability / maxDurability * 100;

								final int color = percentage > 75 ? DURABILITY_HIGH :
										percentage > 50 ? DURABILITY_MEDIUM :
												percentage > 25 ? DURABILITY_LOW : DURABILITY_CRITICAL;

								switch (armorEnum.getValue()) {
									case BAR -> {
										if (percentage <= 99) {
											Matrix4f matrix = matrixStack.peek().getPositionMatrix();
											Render2D.drawItem(drawContext, armor, x, itemY);

											Render2D.drawBox(matrix, x + 1.34f, itemY + 12.4f,
													ITEM_SIZE * 0.8f + 0.3f, ITEM_SIZE * 0.05f + 1.5f,
													new Color(0, 0, 0, 255));

											float barWidth = ITEM_SIZE * (percentage / 100.0f);
											int hsvColor = MathHelper.hsvToRgb(percentage / 300.0f, 1.0f, 1.0f);
											Color barColor = new Color(
													(hsvColor >> 16 & 255),
													(hsvColor >> 8 & 255),
													(hsvColor & 255),
													255
											);

											Render2D.drawBox(matrix, x + 1.34f, itemY + 12.4f,
													barWidth * 0.8f, ITEM_SIZE * 0.05f + 0.4f, barColor);
										}
									}
									case NUMBER -> {
										String text = String.valueOf(currentDurability);
										float textScale = DURABILITY_TEXT_SCALE;

										float textX = x + ITEM_SIZE + 2;
										Render2D.drawStringWithScale(drawContext, text, textX, (itemY + 5), color, 0.5f);
									}
									case PERCENT -> {
										String text = String.format("%.0f%%", percentage);
										float textScale = DURABILITY_TEXT_SCALE;

										float textX = x + ITEM_SIZE + 2;
										Render2D.drawStringWithScale(drawContext, text, textX,
												(itemY + 5), color, 0.5f);
									}
								}
							}
						}
					}
				}
			} finally {
				matrixStack.pop();
				RenderSystem.disableBlend();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}

		super.draw(drawContext, partialTicks);
	}

	@Override
	public void update() {
		super.update();
		updateDimensions();
	}
}