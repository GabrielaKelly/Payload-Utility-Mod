
package net.payload.module.modules.world;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.payload.Payload;
import net.payload.event.events.BossBarEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.BossBarListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.player.InvUtils;
import net.payload.utils.player.PlayerUtils;

public class AutoOminous extends Module implements BossBarListener, TickListener {

    public enum OminousMode {
        NoBossBar, ScanBossBar
    }

    private boolean hasRaidBar = false;
    private boolean isDrinking = false;
    private int slot, prevSlot;
    private int ticksSinceLastRaidBar = 0;
    private boolean sendMessage = true;

    private final EnumSetting<OminousMode> omniScanningMode = EnumSetting.<OminousMode>builder()
            .id("ominous_scanning")
            .displayName("Scanning Mode")
            .defaultValue(OminousMode.ScanBossBar)
            .build();

    private final FloatSetting delay = FloatSetting.builder()
            .id("ominous_tickdelay")
            .displayName("Tick Delay")
            .defaultValue(10f)
            .minValue(0f)
            .maxValue(30f)
            .step(5f)
            .build();

    public AutoOminous() {
        super("AutoOminous");
        this.setCategory(Category.of("World"));
        this.setDescription("Automatically drinks ominous potions for raid farms");

        this.addSetting(omniScanningMode);
        this.addSetting(delay);
    }

    @Override
    public void onDisable() {
        if (isDrinking) {
            stopDrinking();
        }

        Payload.getInstance().eventManager.RemoveListener(BossBarListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        hasRaidBar = false;
        isDrinking = false;
        sendMessage = true;

        Payload.getInstance().eventManager.AddListener(BossBarListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onRender(BossBarEvent renderEvent) {
        if (renderEvent.name.contains("Raid") && omniScanningMode.getValue() == OminousMode.ScanBossBar) {
            hasRaidBar = true;
            ticksSinceLastRaidBar = 0;
            return;
        }

        if (omniScanningMode.getValue() == OminousMode.NoBossBar) {
            hasRaidBar = true;
            ticksSinceLastRaidBar = 0;
        }
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (MC.player == null || MC.world == null) return;

        ticksSinceLastRaidBar++;

        if (ticksSinceLastRaidBar > delay.getValue()) {
            hasRaidBar = false;
        }

        if (MC.player.hasStatusEffect(StatusEffects.BAD_OMEN) || MC.player.hasStatusEffect(StatusEffects.RAID_OMEN)) {
            if (isDrinking) {
                stopDrinking();
            }
            return;
        }

        if (hasRaidBar) {
            if (isDrinking) {
                stopDrinking();
            }
            return;
        }

        if (Payload.getInstance().moduleManager.autoeat.isEating()) {
            return;
        }

        if (!isDrinking) {
            startDrinking();
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    private void startDrinking() {
        slot = InvUtils.findInHotbar(Items.OMINOUS_BOTTLE).slot();
        if (slot == -1) {
            if (sendMessage) {
                sendErrorMessage("No Ominous Bottles found.");
            }
            sendMessage = false;
            return;
        }

        sendMessage = true;
        prevSlot = MC.player.getInventory().selectedSlot;
        InvUtils.swap(slot, false);
        setUseKey(true);
        if (!MC.player.isUsingItem()) PlayerUtils.rightClick();
        isDrinking = true;
    }

    private void stopDrinking() {
        setUseKey(false);
        InvUtils.swap(prevSlot, false);
        isDrinking = false;
    }

    private void setUseKey(boolean pressed) {
        MC.options.useKey.setPressed(pressed);
    }
}