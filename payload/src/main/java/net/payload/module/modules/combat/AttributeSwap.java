
package net.payload.module.modules.combat;

import net.minecraft.item.Items;
import net.payload.Payload;
import net.payload.event.events.AttackEntityEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.AttackEntityListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.player.InvUtils;

public class AttributeSwap extends Module implements TickListener, AttackEntityListener {

	public FloatSetting targetSlot = FloatSetting.builder()
			.id("attributeswap_target-slot")
			.displayName("Target Slot")
			.description("The hotbar slot to swap to when attacking.")
			.defaultValue(1.0f)
			.minValue(1.0f)
			.maxValue(10.0f)
			.step(1.0f)
			.build();

	public BooleanSetting swapBack = BooleanSetting.builder()
			.id("attributeswap_swapback")
			.displayName("Swap Back")
			.description("Swap back to the original slot after a short delay.")
			.defaultValue(true)
			.build();

	public FloatSetting swingdelay = FloatSetting.builder()
			.id("attributeswap_target_slot")
			.displayName("Swap Back Delay")
			.description("Delay in ticks before swapping back to the previous slot.")
			.defaultValue(6.0f)
			.minValue(1.0f)
			.maxValue(10.0f)
			.step(1.0f)
			.build();

	public BooleanSetting bowAttribute = BooleanSetting.builder()
			.id("attributeswap_bowattribute")
			.displayName("Use Bows")
			.defaultValue(true)
			.build();

	public FloatSetting bowdelay = FloatSetting.builder()
			.id("attributeswap_target_slot")
			.displayName("Swap Back Bow Delay")
			.description("Delay in ticks before swapping back to the previous slot.")
			.defaultValue(10.0f)
			.minValue(1.0f)
			.maxValue(20.0f)
			.step(1.0f)
			.build();

	private int prevSlot = -1;
	private int dDelay = 0;
	boolean bowtrigger = false;

	public AttributeSwap() {
		super("AttributeSwap");

		this.setCategory(Category.of("Combat"));
		this.setDescription("Swaps your held tool to spoof weapons attributes");

		this.addSetting(targetSlot);
		this.addSetting(swapBack);
		this.addSetting(swingdelay);
		this.addSetting(bowAttribute);
		this.addSetting(bowdelay);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(AttackEntityListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(AttackEntityListener.class, this);
	}

	@Override
	public void onToggle() {
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (dDelay > 0) {
			dDelay--;
			if (dDelay == 0 && prevSlot != -1) {
				InvUtils.swap(prevSlot, false);
				prevSlot = -1;
			}
		}

		if (MC.player != null && MC.player.getMainHandStack().getItem() == Items.BOW) {
			if (MC.player.isUsingItem()) {
				bowtrigger = true;
			}

			if (!MC.player.isUsingItem() && bowtrigger) {
					bowtrigger = false;
					if (swapBack.get()) {
						prevSlot = MC.player.getInventory().selectedSlot;
					}
					InvUtils.swap(Math.round(targetSlot.get()-1), false);
					if (swapBack.get() && prevSlot != -1) {
						dDelay = Math.round(swingdelay.get());
					}
				}
			}
		}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	@Override
	public void onAttack(AttackEntityEvent event) {
		if (nullCheck()) return;

		if (swapBack.get()) {
			prevSlot = MC.player.getInventory().selectedSlot;
		}

		InvUtils.swap(Math.round(targetSlot.get()-1), false);
		if (swapBack.get() && prevSlot != -1) {
			dDelay = Math.round(swingdelay.get());
		}
	}
}
