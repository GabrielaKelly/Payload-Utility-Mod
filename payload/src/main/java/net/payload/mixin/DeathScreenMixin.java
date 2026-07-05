

package net.payload.mixin;

import net.payload.Payload;
import net.payload.event.events.PlayerDeathEvent;
import net.payload.gui.GuiManager;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {

    @Inject(at = {@At("HEAD")}, method = "init()V", cancellable = true)
    private void onInit(CallbackInfo ci) {
        GuiManager hudManager = Payload.getInstance().guiManager;
        if (hudManager.isClickGuiOpen()) {
            hudManager.setClickGuiOpen(false);
        }

        PlayerDeathEvent event = new PlayerDeathEvent();
        Payload.getInstance().eventManager.Fire(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
