package net.payload.module.modules.client;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.PlayerDeathEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.PlayerDeathListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.EnumSetting;

public class Suicide extends Module implements TickListener, PlayerDeathListener {

    public enum Mode {
        Health, Fire, Packet
    }

    private final EnumSetting<Suicide.Mode> mode = EnumSetting.<Suicide.Mode>builder()
            .id("Mode Choice")
            .displayName("Mode")
            .description("Method of suicide")
            .defaultValue(Mode.Packet)
            .build();

    public Suicide() {
        super("Suicide");

        this.setCategory(Category.of("Client"));
        this.setDescription("Kills you, best used when trapped");

        this.addSetting(mode);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(PlayerDeathListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(PlayerDeathListener.class, this);

        if (nullCheck()) {
            toggle();
            return;
        }

        if (mode.getValue() == Mode.Health) {
            MC.player.setHealth(0);
        }
    }

    @Override
    public void onToggle() {
        // No need to do anything here
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        switch (mode.getValue()) {
            case Fire -> {
                MC.player.setOnFireFor(5); // Fire
            }

            case Packet -> {
                int packetsRequired = Math.round((float) Math.ceil(Math.abs(150 / 10.0f)));
                for (int i = 0; i < packetsRequired; i++) {
                    MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
                }

                Vec3d newPos = MC.player.getPos().add(0, 150, 0);
                MC.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, false, false));
            }
        }
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.state.getValue()) {
            this.toggle();
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {
        // Unused
    }
}
