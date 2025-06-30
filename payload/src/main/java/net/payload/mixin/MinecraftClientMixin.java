

package net.payload.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.interfaces.RightClickHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.payload.event.events.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.session.Session;
import net.minecraft.client.world.ClientWorld;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements RightClickHandler {

	@Unique private boolean doItemUseCalled;
	@Unique private boolean rightClick;

	@Shadow
	public int attackCooldown;

	@Shadow
	public HitResult crosshairTarget;

	@Shadow
	public ClientPlayerInteractionManager interactionManager;

	@Final
	@Shadow
	public ParticleManager particleManager;

	@Shadow
	private int itemUseCooldown;

	@Shadow
	@Final
	private Session session;

	@Shadow
	@Final
	private Mouse mouse;

	@Shadow
	public ClientWorld world;

	@Shadow
	public ClientPlayerEntity player;

	private Session payloadSession;

	@Shadow
	public abstract boolean isWindowFocused();

	@Shadow protected abstract void doItemUse();

	@Shadow
	@Final
	public GameOptions options;

	@Mixin(ClientWorld.class)
	public interface ClientWorldAccessor {
		@Accessor("pendingUpdateManager")
		PendingUpdateManager getPendingUpdateManager();
	}

	@Inject(at = @At("HEAD"), method = "onFinishedLoading(Lnet/minecraft/client/MinecraftClient$LoadingContext;)V")
	private void onfinishedloading(CallbackInfo info) {
		Payload.getInstance().loadAssets();
	}

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void onPreTick(CallbackInfo info) {
		if (this.world != null && player != null) {

			doItemUseCalled = false;

			TickEvent.Pre updateEvent = new TickEvent.Pre();
			Payload.getInstance().eventManager.Fire(updateEvent);

			if (rightClick && !doItemUseCalled && interactionManager != null) doItemUse();
			rightClick = false;
		}
	}

	@Inject(method = "doItemUse", at = @At("HEAD"))
	private void onDoItemUse(CallbackInfo info) {
		doItemUseCalled = true;
	}

	@Inject(at = @At("TAIL"), method = "tick()V")
	public void onPostTick(CallbackInfo info) {
		if (this.world != null && player != null) {
			TickEvent.Post updateEvent = new TickEvent.Post();
			Payload.getInstance().eventManager.Fire(updateEvent);
		}
	}

	@Inject(at = { @At("HEAD") }, method = { "getSession()Lnet/minecraft/client/session/Session;" }, cancellable = true)
	private void onGetSession(CallbackInfoReturnable<Session> cir) {
		if (payloadSession == null)
			return;
		cir.setReturnValue(payloadSession);
	}

	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;session:Lnet/minecraft/client/session/Session;", opcode = Opcodes.GETFIELD, ordinal = 0), method = {
			"getSession()Lnet/minecraft/client/session/Session;" })
	private Session getSessionForSessionProperties(MinecraftClient mc) {
		if (payloadSession != null)
			return payloadSession;
		return session;
	}

	@Inject(at = { @At(value = "HEAD") }, method = { "close()V" })
	private void onClose(CallbackInfo ci) {
		try {
			Payload.getInstance().endClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Inject(at = { @At(value = "HEAD") }, method = { "openGameMenu(Z)V" })
	private void onOpenPauseMenu(boolean pause, CallbackInfo ci) {
		PayloadClient payload = Payload.getInstance();
		try {
			//crash on pause
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (payload.guiManager != null) {
			Payload.getInstance().guiManager.setClickGuiOpen(false);
		}
	}

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
		if (this.attackCooldown <= 0 && this.player.isUsingItem() && Payload.getInstance().moduleManager.interactTweaks.multiTask()) {
			if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockHitResult = (BlockHitResult)this.crosshairTarget;
				BlockPos blockPos = blockHitResult.getBlockPos();
				if (!this.world.getBlockState(blockPos).isAir()) {
					Direction direction = blockHitResult.getSide();
					if (this.interactionManager.updateBlockBreakingProgress(blockPos, direction)) {
						this.particleManager.addBlockBreakingParticles(blockPos, direction);
						this.player.swingHand(Hand.MAIN_HAND);
					}
				}
			} else {
				this.interactionManager.cancelBlockBreaking();
			}
			ci.cancel();
		}
	}

	// Interface
	@Unique
	public void payload$rightClick() {
		rightClick = true;
	}
}
