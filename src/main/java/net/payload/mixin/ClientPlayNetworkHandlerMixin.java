package net.payload.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.EntityVelocityUpdateEvent;
import net.payload.gui.GuiManager;
import net.payload.utils.rotation.RotationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.payload.cmd.CommandManager;
import net.payload.event.events.GameLeftEvent;
import net.payload.event.events.SendMessageEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;

import static net.payload.PayloadClient.MC;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {
	@Shadow
	private ClientWorld world;

	@Shadow
	public abstract void sendChatMessage(String content);

	@Unique
	private boolean ignoreChatMessage;

	@Unique
	private boolean worldNotNull;

	protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection,
			ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "onGameJoin", at = @At("TAIL"))
	private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
		if (worldNotNull) {
			GameLeftEvent gameLeftEvent = new GameLeftEvent();

			GuiManager hudManager = Payload.getInstance().guiManager;

			if (hudManager.isClickGuiOpen()) {
				hudManager.setClickGuiOpen(false);
			}

			Payload.getInstance().eventManager.Fire(gameLeftEvent);
		}

		// At some point fire a game joined event here
	}

	@Inject(method = "onEnterReconfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
	private void onEnterReconfiguration(EnterReconfigurationS2CPacket packet, CallbackInfo info) {
		GameLeftEvent gameLeftEvent = new GameLeftEvent();

		GuiManager hudManager = Payload.getInstance().guiManager;

		if (hudManager.isClickGuiOpen()) {
			hudManager.setClickGuiOpen(false);
		}

		Payload.getInstance().eventManager.Fire(gameLeftEvent);
	}

	@Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
	public void test(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
		NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
		Entity entity = this.world.getEntityById(packet.getEntityId());

		if (entity != null && MC.player != null && entity == MC.player) {
			EntityVelocityUpdateEvent event = new EntityVelocityUpdateEvent();
			Payload.getInstance().eventManager.Fire(event);

			if (Payload.getInstance().moduleManager.fireworkPlus.state.getValue()) {
				if (event.isCancelled()) {
					entity.setVelocityClient((double) packet.getVelocityX() / 8000.0, (double) packet.getVelocityY() / 8000.0, (double) packet.getVelocityZ() / 8000.0);
				} else {
					entity.setVelocityClient((double) packet.getVelocityX(), (double) packet.getVelocityY(), (double) packet.getVelocityZ());
				}
				ci.cancel();
			}

			if (event.isCancelled()) {
				event.cancel();
			}
		}
	}

	@Inject(method = "onPlayerPositionLook", at = @At("HEAD"), cancellable = true)
	public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
		ci.cancel();
		RotationManager ra = Payload.getInstance().rotationManager;
		NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
		PlayerEntity playerEntity = this.client.player;
		Vec3d vec3d = playerEntity.getVelocity();
		boolean bl = packet.getClass().accessFlags().contains(PositionFlag.X);
		boolean bl2 = packet.getClass().accessFlags().contains(PositionFlag.Y);
		boolean bl3 = packet.getClass().accessFlags().contains(PositionFlag.Z);
		double d;
		double e;
		if (bl) {
			d = vec3d.getX();
			e = playerEntity.getX() + packet.change().position().getX();
			playerEntity.lastRenderX += packet.change().position().getX();
			playerEntity.prevX += packet.change().position().getX();
		} else {
			d = 0.0;
			e = packet.change().position().getX();
			playerEntity.lastRenderX = e;
			playerEntity.prevX = e;
		}

		double f;
		double g;
		if (bl2) {
			f = vec3d.getY();
			g = playerEntity.getY() + packet.change().position().getY();
			playerEntity.lastRenderY += packet.change().position().getY();
			playerEntity.prevY += packet.change().position().getY();
		} else {
			f = 0.0;
			g = packet.change().position().getY();
			playerEntity.lastRenderY = g;
			playerEntity.prevY = g;
		}

		double h;
		double i;
		if (bl3) {
			h = vec3d.getZ();
			i = playerEntity.getZ() + packet.change().position().getZ();
			playerEntity.lastRenderZ += packet.change().position().getZ();
			playerEntity.prevZ += packet.change().position().getZ();
		} else {
			h = 0.0;
			i = packet.change().position().getZ();
			playerEntity.lastRenderZ = i;
			playerEntity.prevZ = i;
		}

		playerEntity.setPosition(e, g, i);
		playerEntity.setVelocity(d, f, h);
		if (Payload.getInstance().moduleManager.rotations.serverApply.getValue()) {
			float j = packet.change().yaw();
			float k = packet.change().pitch();
			if (packet.getClass().accessFlags().contains(PositionFlag.X_ROT)) {
				playerEntity.setPitch(playerEntity.getPitch() + k);
				playerEntity.prevPitch += k;
			} else {
				playerEntity.setPitch(k);
				playerEntity.prevPitch = k;
			}

			if (packet.getClass().accessFlags().contains(PositionFlag.Y_ROT)) {
				playerEntity.setYaw(playerEntity.getYaw() + j);
				playerEntity.prevYaw += j;
			} else {
				playerEntity.setYaw(j);
				playerEntity.prevYaw = j;
			}

			this.connection.send(new TeleportConfirmC2SPacket(packet.teleportId()));
			this.connection
					.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch(), false, false));
		} else {
			if (Payload.getInstance().moduleManager.rotations.serverYaw.getValue()) {
				float j = packet.change().yaw();
				float k = packet.change().pitch();
				if (packet.getClass().accessFlags().contains(PositionFlag.X_ROT)) {
					k = (ra.lastYaw + k);
				}

				if (packet.getClass().accessFlags().contains(PositionFlag.Y_ROT)) {
					j = (ra.lastPitch + j);
				}
				this.connection.send(new TeleportConfirmC2SPacket(packet.teleportId()));
				this.connection
						.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), j, k, false, false));
			} else {
				this.connection.send(new TeleportConfirmC2SPacket(packet.teleportId()));
				this.connection
						.send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), ra.rotationYaw, ra.rotationPitch, false, false));
			}
		}
	}

	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	private void onSendChatMessage(String message, CallbackInfo ci) {
		if (ignoreChatMessage)
			return;

		if (!message.startsWith(CommandManager.PREFIX.getValue())) {
			SendMessageEvent sendMessageEvent = new SendMessageEvent(message);
			Payload.getInstance().eventManager.Fire(sendMessageEvent);

			if (!sendMessageEvent.isCancelled()) {
				ignoreChatMessage = true;
				sendChatMessage(sendMessageEvent.getMessage());
				ignoreChatMessage = false;
			}
			ci.cancel();
			return;
		}
	}
}