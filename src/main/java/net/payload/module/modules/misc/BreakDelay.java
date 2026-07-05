
//Fish finish break delay mixin lol

package net.payload.module.modules.misc;

import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.listeners.MouseClickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.types.MouseButton;

public class BreakDelay extends Module implements MouseClickListener {

    private BooleanSetting noinstabreak = BooleanSetting.builder().id("breakdelay_noinstant").displayName("No instamine")
            .description("Add a short delay").defaultValue(false)
            .build();

    private FloatSetting cooldown = FloatSetting.builder()
            .id("breakdelay_cooldown")
            .displayName("Cooldown")
            .defaultValue(0f)
            .minValue(0f)
            .maxValue(5f)
            .step(1f)
            .build();

    public boolean isBreaking;

    public BreakDelay() {
        super("BreakDelay");

        this.setCategory(Category.of("Misc"));
        this.setDescription("Changes breaking delay, allowing for bypassing faster mining");

        this.addSetting(noinstabreak);
        this.addSetting(cooldown);
    }

    public boolean preventInstaBreak() {
        return Payload.getInstance().moduleManager.breakDelay.state.getValue() && noinstabreak.getValue();
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onMouseClick(MouseClickEvent mouseClickEvent) {
        if (mouseClickEvent.button == MouseButton.LEFT) {
            isBreaking = true;
        } else isBreaking = false;
    }

   public float cooldownVal() {
        if (this.state.getValue()) {
            return cooldown.getValue();
        }
        else return 5;
   }

   public boolean isBreaking() {
       return isBreaking;
   }
}
