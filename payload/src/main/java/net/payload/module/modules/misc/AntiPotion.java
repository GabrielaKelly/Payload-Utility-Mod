package net.payload.module.modules.misc;

import net.minecraft.entity.effect.StatusEffects;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class AntiPotion extends Module implements TickListener {

    private BooleanSetting antilevitate = BooleanSetting.builder()
            .id("potionspoof_levitate")
            .displayName("Anti Levitate")
            .description("removes effect")
            .defaultValue(true)
            .build();

    private BooleanSetting antislowfalling = BooleanSetting.builder()
            .id("potionspoof_slowfalling")
            .displayName("Anti Slowfalling")
            .description("removes effect")
            .defaultValue(true)
            .build();

    public AntiPotion() {
        super("AntiPotion");

        this.setCategory(Category.of("Misc"));
        this.setDescription("Prevents certain potion effects");
        this.addSetting(antilevitate);
        this.addSetting(antislowfalling);
    }

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
        if (nullCheck()) return;

        if (MC.player.hasStatusEffect(StatusEffects.LEVITATION) && antilevitate.getValue()) {
            MC.player.removeStatusEffect(StatusEffects.LEVITATION);
        }

        if (MC.player.hasStatusEffect(StatusEffects.SLOW_FALLING) && antislowfalling.getValue()) {
            MC.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
        }

    }

    @Override
    public void onTick(TickEvent.Post event) {

    }
}