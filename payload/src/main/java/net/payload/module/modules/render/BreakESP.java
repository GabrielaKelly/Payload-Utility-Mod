
package net.payload.module.modules.render;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.BreakManager;
import net.payload.utils.render.Render3D;

import java.text.DecimalFormat;

public class BreakESP extends Module implements Render3DListener {

	private final BooleanSetting booleanbox = BooleanSetting.builder()
			.id("breakesp_boolbox")
			.displayName("Render Box")
			.defaultValue(true)
			.build();

	private final ColorSetting box = ColorSetting.builder()
			.id("breakesp_box")
			.displayName("Box Color")
			.description("Box outline color")
			.defaultValue(new Color(255, 255, 255, 50)) // Converting 255,255,255,255 to float values
			.build();

	private final FloatSetting breakTime = FloatSetting.builder()
			.id("breakesp_break_time")
			.displayName("Break Time")
			.description("Time to break")
			.defaultValue(2.5f)
			.minValue(0f)
			.maxValue(5f)
			.step(0.1f)
			.build();


	public BreakESP() {
		super("BreakESP");
		this.setCategory(Category.of("Render"));
		this.setDescription("Reveals where other players are attempting to mine");

		this.addSetting(booleanbox);
		this.addSetting(box);
		this.addSetting(breakTime);
	}


	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	DecimalFormat df = new DecimalFormat("0.0");

	@Override
	public void onRender(Render3DEvent event) {
		for (BreakManager.BreakData breakData : Payload.getInstance().breakManager.breakMap.values()) {
			if (breakData == null || breakData.getEntity() == null) continue;

			Box cbox = new Box(new BlockPos(breakData.pos));

			Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), cbox, box.getValue(), 1f);
			Render3D.drawText3D(breakData.getEntity().getName().getString(), breakData.pos.toCenterPos().add(0, 0.15, 0), -1);

			double breakTime = this.breakTime.getValue() * 1000;
			Render3D.drawText3D(Text.of(df.format(Math.min(1, breakData.timer.getElapsedTime() / breakTime) * 100)), breakData.pos.toCenterPos().add(0, -0.15, 0), 0, 0, 1, new java.awt.Color(255, 255, 255));

		}
	}
}