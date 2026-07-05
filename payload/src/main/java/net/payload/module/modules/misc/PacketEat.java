
package net.payload.module.modules.misc;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.payload.Payload;
import net.payload.event.events.SendPacketEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.SendPacketListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class PacketEat extends Module implements TickListener, SendPacketListener {

	int i;

	private BooleanSetting Desync = BooleanSetting.builder()
			.id("packeteat_desync")
			.displayName("Desync")
			.description("")
			.defaultValue(true)
			.build();

	private BooleanSetting noUse = BooleanSetting.builder()
			.id("packeteat_nouse")
			.displayName("Cancel Usage")
			.description("")
			.defaultValue(true)
			.build();

	public PacketEat() {
		super("PacketEat");

		this.setCategory(Category.of("Misc"));
		this.setDescription("Sends the eating packet");
		this.addSetting(Desync);
		this.addSetting(noUse);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
	}

	@Override
	public void onToggle() {
	}

	@Override
	public void onSendPacket(SendPacketEvent event) {
		if (nullCheck()) return;

		if (event.GetPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && isFoodItem(MC.player.getActiveItem().getItem().getDefaultStack())) {
			i = packet.getSequence();
			event.cancel();
		}
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (nullCheck()) return;

		if (Desync.getValue() && MC.player.isUsingItem() && isFoodItem(MC.player.getActiveItem().getItem().getDefaultStack())) {
			MC.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, i, MC.player.getYaw(), MC.player.getPitch()));
		}

		if (noUse.getValue()) {
			MC.player.stopUsingItem();
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	public boolean Peat() {
        return noUse.getValue();
	}

	public static boolean isFoodItem(ItemStack stack) {
		FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
		return foodComponent != null;
	}

}