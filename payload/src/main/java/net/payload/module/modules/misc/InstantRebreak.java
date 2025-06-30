
package net.payload.module.modules.misc;

import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.StartBreakingBlockEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.StartBreakingBlockListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.block.BlockUtils;
import net.payload.utils.render.Render3D;

public class InstantRebreak extends Module implements Render3DListener, TickListener, StartBreakingBlockListener {

    public FloatSetting tickDelay = FloatSetting.builder()
            .id("instantrebreak_delay")
            .displayName("Tick Delay")
            .defaultValue(0f)
            .minValue(0f)
            .maxValue(20f)
            .step(1f)
            .build();

    public BooleanSetting pickfilter = BooleanSetting.builder()
            .id("instantrebreak_pickfilter")
            .displayName("Pickaxe Only")
            .description("")
            .defaultValue(false)
            .build();

    private ColorSetting blockcolor = ColorSetting.builder()
            .id("instantrebreak_color")
            .displayName("Block Color")
            .defaultValue(new Color(255, 0, 0, 30))
            .build();

    public BooleanSetting render = BooleanSetting.builder()
            .id("instantrebreak_render")
            .displayName("Render")
            .description("shows visual")
            .defaultValue(true)
            .build();

    public FloatSetting lineThickness = FloatSetting.builder()
            .id("instantrebreak_thickness")
            .displayName("Line Thickness")
            .defaultValue(2f)
            .minValue(0f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    public final BlockPos.Mutable blockPos = new BlockPos.Mutable(0, Integer.MIN_VALUE, 0);
    private int ticks;
    private Direction direction;

    public InstantRebreak() {
        super("AutoRebreak");

        this.setCategory(Category.of("Misc"));
        this.setDescription("Automatically rebreaks blocks you tried to mine");

        this.addSetting(tickDelay);
        this.addSetting(pickfilter);
        this.addSetting(render);
        this.addSetting(blockcolor);
        this.addSetting(lineThickness);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(StartBreakingBlockListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(StartBreakingBlockListener.class, this);
        ticks = 0;
        blockPos.set(0, -1, 0);
    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onRender(Render3DEvent event) {
        if (!render.getValue() || !shouldMine()) return;

        Box box = new Box(blockPos);
        Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), box, blockcolor.getValue(), lineThickness.getValue());
    }

    public void sendPacket() {
        MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction == null ? Direction.UP : direction));
    }

    public boolean shouldMine() {
        if (MC.world.isOutOfHeightLimit(blockPos) || !BlockUtils.canBreak(blockPos)) return false;

        return !pickfilter.getValue() || MC.player.getMainHandStack().getItem() instanceof PickaxeItem;
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (ticks >= tickDelay.getValue()) {
            ticks = 0;

            if (shouldMine()) {
                //if (rotate.get()) Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos), this::sendPacket);
                //else sendPacket();
                //Fish need to work on rotations.
                sendPacket();

                MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        } else {
            ticks++;
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onBreak(StartBreakingBlockEvent even) {
        direction = even.direction;
        blockPos.set(even.blockPos);
    }
}
