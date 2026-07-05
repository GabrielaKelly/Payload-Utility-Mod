

package net.payload.mixin;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;

import net.payload.Payload;
import net.payload.event.events.BossBarEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(DrawContext context, CallbackInfo ci) {
        // This is done in a super funky way to be compatible with other clients.
        Map<UUID, ClientBossBar> bossBars = ((BossBarHudAccessor) this).getBossBars();

        for (ClientBossBar bar : bossBars.values()) {
            BossBarEvent bbe = new BossBarEvent(bar.getName());
            Payload.getInstance().eventManager.Fire(bbe);
        }
    }

    @Mixin(BossBarHud.class)
    public interface BossBarHudAccessor {
        @Accessor("bossBars")
        Map<UUID, ClientBossBar> getBossBars();
    }
}
/*

   Works but incompatible with meteor

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
	public Text onAsFormattedString(ClientBossBar clientBossBar) {
		BossBarEvent bbe = new BossBarEvent(clientBossBar.getName());
		Payload.getInstance().eventManager.Fire(bbe);
		return clientBossBar.getName();
	}

 */
