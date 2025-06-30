
package net.payload.module.modules.combat;

import net.minecraft.util.math.BlockPos;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.module.modules.misc.PacketMine;
import net.payload.settings.types.BooleanSetting;
import net.payload.utils.block.BlockPosX;
import net.payload.utils.block.OtherBlockUtils;

public class AntiCrawl extends Module implements TickListener {

    public BooleanSetting anticipation = BooleanSetting.builder()
            .id("anticrawl_predict")
            .displayName("Prediction")
            .description("Predicts when your about to crawl")
            .defaultValue(true)
            .build();

    public AntiCrawl() {
        super("AntiCrawl");
        this.setCategory(Category.of("Combat"));
        this.setDescription("Attempts to prevent crawling for CPVP");

        this.addSetting(anticipation);
    }

    public boolean work = false;
    double[] xzOffset = new double[]{0, 0.3, -0.3};

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);

    }

    @Override
    public void onToggle() {

    }

    @Override
    public void onTick(TickEvent.Pre event) {
        work = false;
        if (MC.player.isCrawling() || anticipation.getValue() && Payload.getInstance().breakManager.isMining(MC.player.getBlockPos())) {
            for (double offset : xzOffset) {
                for (double offset2 : xzOffset) {
                    BlockPos pos = new BlockPosX(MC.player.getX() + offset, MC.player.getY() + 1.2, MC.player.getZ() + offset2);
                    if (canBreak(pos)) {
                        PacketMine.INSTANCE.mine(pos);
                        work = true;
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    private boolean canBreak(BlockPos pos) {
        return (OtherBlockUtils.getClickSideStrict(pos) != null || PacketMine.getBreakPos().equals(pos)) && !PacketMine.godBlocks.contains(MC.world.getBlockState(pos).getBlock()) && !MC.world.isAir(pos);
    }
}