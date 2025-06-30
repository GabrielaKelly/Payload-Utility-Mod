

package net.payload.mixin;

import net.payload.Payload;
import net.payload.PayloadClient;
import net.payload.event.events.KeyDownEvent;
import net.payload.event.events.KeyUpEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.event.KeyEvent;

import static net.payload.PayloadClient.MC;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(at = {@At("HEAD")}, method = {"onKey(JIIII)V"}, cancellable = true)
    private void OnKeyDown(long window, int key, int scancode,
                           int action, int modifiers, CallbackInfo ci) {
        PayloadClient payload = Payload.getInstance();

        if (action == GLFW.GLFW_PRESS) {
            if (payload != null && payload.eventManager != null) {
                KeyDownEvent event = new KeyDownEvent(window, key, scancode, action, modifiers);
                
                Payload.getInstance().eventManager.Fire(event);

                if (event.isCancelled()) {
                    ci.cancel();
                }
            }

            if (MC.currentScreen == null && MC.getOverlay() == null) {
                if (key == KeyEvent.VK_PERIOD) {
                    MC.setScreen(new ChatScreen(""));
                }
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            if (payload != null && payload.eventManager != null) {
                KeyUpEvent event = new KeyUpEvent(window, key, scancode, action, modifiers);

                Payload.getInstance().eventManager.Fire(event);

                if (event.isCancelled()) {
                    ci.cancel();
                }
            }
        }
    }
}
