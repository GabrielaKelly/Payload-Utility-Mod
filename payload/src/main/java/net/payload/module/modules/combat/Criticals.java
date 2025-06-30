
package net.payload.module.modules.combat;

import io.netty.buffer.Unpooled;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.payload.Payload;
import net.payload.event.events.SendPacketEvent;
import net.payload.event.listeners.SendPacketListener;
import net.payload.mixin.interfaces.IPlayerInteractEntityC2SPacket;
import net.payload.mixin.interfaces.IPlayerMoveC2SPacket;
import net.payload.module.Category;
import net.payload.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.payload.settings.types.EnumSetting;

public class Criticals extends Module implements SendPacketListener {

	public enum InteractType {
		INTERACT, ATTACK, INTERACT_AT
	}

	public enum Mode {
		Legit, Packet, Bypass
	}

	private final EnumSetting<Criticals.Mode> mode = EnumSetting.<Criticals.Mode>builder()
			.id("crits_mode")
			.displayName("Mode")
			.description("Crits mode")
			.defaultValue(Mode.Packet)
			.build();


	public Criticals() {
		super("Criticals");

		this.setCategory(Category.of("Combat"));
		this.setDescription("Forces attacks into critical strikes");

		this.addSetting(mode);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	private void sendPacket(double height) {
		double x = MC.player.getX();
		double y = MC.player.getY();
		double z = MC.player.getZ();

		PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(x, y + height, z, false,false);

		MC.player.networkHandler.sendPacket(packet);
	}

	@Override
	public void onSendPacket(SendPacketEvent event) {
		if (nullCheck()) return;

		Packet<?> packet = event.GetPacket();
		if (packet instanceof PlayerInteractEntityC2SPacket) {
			PlayerInteractEntityC2SPacket playerInteractPacket = (PlayerInteractEntityC2SPacket) packet;
			IPlayerInteractEntityC2SPacket packetAccessor = (IPlayerInteractEntityC2SPacket) playerInteractPacket;

			PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
			packetAccessor.invokeWrite(packetBuf);
			packetBuf.readVarInt();
			InteractType type = packetBuf.readEnumConstant(InteractType.class);

			if (type == InteractType.ATTACK) {
				MinecraftClient mc = MinecraftClient.getInstance();
				ClientPlayerEntity player = mc.player;
				if (player.isOnGround() && !player.isInLava() && !player.isSubmergedInWater()) {

					switch (mode.getValue()) {
						case Mode.Packet -> {
							sendPacket(0.0625);
							sendPacket(0);
						}

						case Mode.Legit -> {
							player.jump();
						}

						case Mode.Bypass -> {
							sendPacket(0.11);
							sendPacket(0.1100013579);
							sendPacket(0.0000013579);
						}
					}
				}
			}
		}
	}
}
