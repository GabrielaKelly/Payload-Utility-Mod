package net.payload.utils.rotation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.module.modules.client.AntiCheat;
import net.payload.module.modules.client.Rotations;
import net.payload.module.modules.client.MoveFix;
import net.payload.utils.math.CacheTimer;

import static net.payload.PayloadClient.MC;

public class RotationManager implements SendMovementPacketListener, MouseMoveListener, ReceivePacketListener, MovementPacketsListener, RotateListener, SendPacketListener {

    public RotationManager() {
        Payload.getInstance().eventManager.AddListener(RotateListener.class, this);
        Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(MouseMoveListener.class, this);
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(MovementPacketsListener.class, this);
    }

	private static final MinecraftClient mc = MinecraftClient.getInstance();

	AntiCheat antiCheat = Payload.getInstance().moduleManager.antiCheat;

    Rotations rots = Payload.getInstance().moduleManager.rotations;

	public float nextYaw;
	public float nextPitch;
	public float rotationYaw = 0;
	public float rotationPitch = 0;
	public float lastYaw = 0;
	public float lastPitch = 0;
	public static final CacheTimer ROTATE_TIMER = new CacheTimer();
	public static Vec3d directionVec = null;
	public static boolean lastGround;
    private int rotateTicks;

	public void snapBack() {
		mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), rotationYaw, rotationPitch, mc.player.isOnGround(), mc.player.horizontalCollision));
	}
	public void lookAt(Vec3d directionVec) {
		rotationTo(directionVec);
		snapAt(directionVec);
	}
	public void lookAt(BlockPos pos, Direction side) {
		final Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
		lookAt(hitVec);
	}

	public void snapAt(float yaw, float pitch) {
		setRenderRotation(yaw, pitch, true);
		if (antiCheat.grimRotation.getValue()) {
			mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround(), false));
		} else {
			mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround(), false));
		}
	}

	public void snapAt(Vec3d directionVec) {
		float[] angle = getRotation(directionVec);
		if (antiCheat.noSpamRotation.getValue()) {
			if (MathHelper.angleBetween(angle[0], lastYaw) < antiCheat.fov.getValue() && Math.abs(angle[1] - lastPitch) < antiCheat.fov.getValue()) {
				return;
			}
		}
		snapAt(angle[0], angle[1]);
	}

	public float[] getRotation(Vec3d eyesPos, Vec3d vec) {
		double diffX = vec.x - eyesPos.x;
		double diffY = vec.y - eyesPos.y;
		double diffZ = vec.z - eyesPos.z;
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
		float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
		return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)};
	}
	public float[] getRotation(Vec3d vec) {
		Vec3d eyesPos = getEyesPos();
		return getRotation(eyesPos, vec);
	}

    public static Vec3d getEyesPos() {
        return mc.player.getEyePos();
    }

	public void rotationTo(Vec3d vec3d) {
		ROTATE_TIMER.reset();
		directionVec = vec3d;
	}

	public boolean inFov(Vec3d directionVec, float fov) {
		float[] angle = getRotation(new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ()), directionVec);
		return inFov(angle[0], angle[1], fov);
	}

    public Vec3d getDirVec() {
        return directionVec;
    }

	public boolean inFov(float yaw, float pitch, float fov) {
		return MathHelper.angleBetween(yaw, rotationYaw) + Math.abs(pitch - rotationPitch) <= fov;
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {

	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Post event) {
        if (Payload.getInstance().moduleManager.moveFix.state.getValue() && !(Payload.getInstance().moduleManager.moveFix.updateMode.getValue() == MoveFix.UpdateMode.Mouse)) {
                updateNext();
        }
	}

    @Override
    public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
        if (mc.player != null && Payload.getInstance().moduleManager.moveFix.state.getValue() && !(Payload.getInstance().moduleManager.moveFix.updateMode.getValue() == MoveFix.UpdateMode.Movement)) {
            updateNext();
        }
    }

    public boolean shouldMoveFix() {
        if (!Payload.getInstance().moduleManager.moveFix.state.getValue()
                || Payload.getInstance().moduleManager.scaffold.state.getValue()
                || Payload.getInstance().moduleManager.elytraBounce.state.getValue()
                || MC.player.isGliding()) return false;

        return true;
    }

    @Override
    public void onUpdate(MovementPacketsEvent event) {
        if (!Payload.getInstance().moduleManager.rotations.state.getValue()) {
            Payload.getInstance().moduleManager.rotations.toggle();
        }

        if (shouldMoveFix()) {
            event.setYaw(nextYaw);
            event.setPitch(nextPitch);
        } else {
            RotateEvent event1 = new RotateEvent(event.getYaw(), event.getPitch());
            Payload.getInstance().eventManager.Fire(event1);
            event.setYaw(event1.getYaw());
            event.setPitch(event1.getPitch());
        }
    }

    @Override
    public void onLastRotate(RotateEvent event) {
        LookAtEvent lookAtEvent = new LookAtEvent();
        Payload.getInstance().eventManager.Fire(lookAtEvent);
        if (lookAtEvent.getRotation()) {
            float[] newAngle = injectStep(new float[]{lookAtEvent.getYaw(), lookAtEvent.getPitch()}, lookAtEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (lookAtEvent.getTarget() != null) {
            float[] newAngle = injectStep(lookAtEvent.getTarget(), lookAtEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (!event.isModified() && antiCheat.look.getValue()) {
            if (directionVec != null && !ROTATE_TIMER.passed((long) (AntiCheat.INSTANCE.rotateTime.getValue() * 1000))) {
                float[] newAngle = injectStep(directionVec, rots.steps.getValue());
                event.setYaw(newAngle[0]);
                event.setPitch(newAngle[1]);
            }
        }

        if (directionVec != null && !ROTATE_TIMER.passed((long) (AntiCheat.INSTANCE.rotateTime.getValue() * 1000))) {
            if (rotateTicks < Payload.getInstance().moduleManager.moveFix.tickssetting.getValue()) {
                float[] newAngle = injectStep(directionVec, rots.steps.getValue());
                event.setYaw(newAngle[0]);
                event.setPitch(newAngle[1]);
                rotateTicks++;
            } else {
                directionVec = null;
            }
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.player == null || event.isCancelled()) return;
        if (event.GetPacket() instanceof PlayerMoveC2SPacket packet) {
            if (packet.changesLook()) {
                lastYaw = packet.getYaw(lastYaw);
                lastPitch = packet.getPitch(lastPitch);
                setRenderRotation(lastYaw, lastPitch, false);
            }
            lastGround = packet.isOnGround();
        }
    }

    private void updateNext() {
        RotateEvent rotateEvent = new RotateEvent(mc.player.getYaw(), mc.player.getPitch());
        Payload.getInstance().eventManager.Fire(rotateEvent);
        if (rotateEvent.isModified()) {
            nextYaw = rotateEvent.getYaw();
            nextPitch = rotateEvent.getPitch();
        } else {
            float[] newAngle = injectStep(new float[]{rotateEvent.getYaw(), rotateEvent.getPitch()}, rots.steps.getValue());
            nextYaw = newAngle[0];
            nextPitch = newAngle[1];
        }
        MoveFix.fixRotation = nextYaw;
        MoveFix.fixPitch = nextPitch;
    }

    public float[] injectStep(Vec3d vec, float steps) {
        float currentYaw = antiCheat.forceSync.getValue() ? lastYaw : rotationYaw;
        float currentPitch = antiCheat.forceSync.getValue() ? lastPitch : rotationPitch;

        float yawDelta = MathHelper.wrapDegrees((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z - mc.player.getZ(), (vec.x - mc.player.getX()))) - 90) - currentYaw);
        float pitchDelta = ((float) (-Math.toDegrees(Math.atan2(vec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((vec.x - mc.player.getX()), 2) + Math.pow(vec.z - mc.player.getZ(), 2))))) - currentPitch);

        float angleToRad = (float) Math.toRadians(27 * (mc.player.age % 30));
        yawDelta = (float) (yawDelta + Math.sin(angleToRad) * 3) + random(-1f, 1f);
        pitchDelta = pitchDelta + random(-0.6f, 0.6f);

        if (yawDelta > 180)
            yawDelta = yawDelta - 180;

        float yawStepVal = 180 * steps;

        float clampedYawDelta = MathHelper.clamp(MathHelper.abs(yawDelta), -yawStepVal, yawStepVal);
        float clampedPitchDelta = MathHelper.clamp(pitchDelta, -45, 45);

        float newYaw = currentYaw + (yawDelta > 0 ? clampedYawDelta : -clampedYawDelta);
        float newPitch = MathHelper.clamp(currentPitch + clampedPitchDelta, -90.0F, 90.0F);

        double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;

        return new float[]{(float) (newYaw - (newYaw - currentYaw) % gcdFix), (float) (newPitch - (newPitch - currentPitch) % gcdFix)};
    }

    public float[] injectStep(float[] angle, float steps) {
        if (steps < 0.01f) steps = 0.01f;
        if (steps > 1) steps = 1;
        if (steps < 1 && angle != null) {
            float packetYaw = antiCheat.forceSync.getValue() ? lastYaw : rotationYaw;
            float diff = MathHelper.angleBetween(angle[0], packetYaw);
            if (Math.abs(diff) > 180 * steps) {
                angle[0] = (packetYaw + (diff * ((180 * steps) / Math.abs(diff))));
            }
            float packetPitch = antiCheat.forceSync.getValue() ? lastPitch : rotationPitch;
            diff = angle[1] - packetPitch;
            if (Math.abs(diff) > 90 * steps) {
                angle[1] = (packetPitch + (diff * ((90 * steps) / Math.abs(diff))));
            }
        }
        return new float[]{angle[0], angle[1]};
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }
    public static double random(double min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }

    //Rendering

    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    private int ticksExisted;

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        if (mc.player == null) return;
        if (readPacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket packet) {

            if (packet.getClass().accessFlags().contains(PositionFlag.X_ROT)) {
                lastYaw = lastYaw + packet.change().yaw();
            } else {
                lastYaw = packet.change().yaw();
            }

            if (packet.getClass().accessFlags().contains(PositionFlag.Y_ROT)) {
                lastPitch = lastPitch + packet.change().pitch();
            } else {
                lastPitch = packet.change().pitch();
            }
            setRenderRotation(lastYaw, lastPitch, true);
        }
    }
    public void setRenderRotation(float yaw, float pitch, boolean force) {
        if (mc.player == null) return;
        if (mc.player.age == ticksExisted && !force) {
            return;
        }

        ticksExisted = mc.player.age;
        prevPitch = renderPitch;

        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = getRenderYawOffset(yaw, prevRenderYawOffset);

        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;

        renderPitch = pitch;
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        float offset;

        double xDif = mc.player.getX() - mc.player.prevX;
        double zDif = mc.player.getZ() - mc.player.prevZ;

        if (xDif * xDif + zDif * zDif > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDif, xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            if (95.0F < wrap && wrap < 265.0F) {
                result = offset - 180.0F;
            } else {
                result = offset;
            }
        }

        if (mc.player.handSwingProgress > 0.0F) {
            result = yaw;
        }

        result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f;
        offset = MathHelper.wrapDegrees(yaw - result);

        if (offset < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }

        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }

        return result;
    }
}