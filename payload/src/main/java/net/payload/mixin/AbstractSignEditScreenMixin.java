

package net.payload.mixin;

import net.payload.module.modules.exploit.ServerNuker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.payload.Payload;
import net.payload.cmd.CommandManager;
import net.payload.module.modules.world.AutoSign;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.Text;

import java.util.Arrays;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {
	@Shadow
	@Final
	private String[] messages;

	protected AbstractSignEditScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = { @At("HEAD") }, method = { "init()V" })
	private void onInit(CallbackInfo ci) {
		AutoSign mod = (AutoSign) Payload.getInstance().moduleManager.autosign;
		String[] newText = mod.getText();
		if (newText != null) {
			System.arraycopy(newText, 0, messages, 0, 4);
			finishEditing();
		}
	}

	@Inject(at = { @At("HEAD") }, method = "finishEditing()V")
	private void onEditorClose(CallbackInfo ci) {
		AutoSign mod = (AutoSign) Payload.getInstance().moduleManager.autosign;
		if (mod.state.getValue()) {
			if (mod.getText() == null) {
				mod.setText(messages);
				CommandManager.sendChatMessage("Sign text set to " + toString(mod.getText()));
			}
		}
	}

	@Unique
	public String toString(String[] arr) {

		String val = "";

		if (arr != null) {
			for (String element : arr) {
				val = val + " " + element;
			}
		}

		return val;
	}

	@Shadow
	private void finishEditing() {

	}
}
