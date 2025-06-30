

package net.payload.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.payload.Payload;
import net.payload.cmd.CommandManager;
import net.payload.cmd.GlobalChat;
import net.payload.cmd.GlobalChat.ChatType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends ScreenMixin {
	@Shadow
	protected TextFieldWidget chatField;

	// protected ButtonComponent serverChatButton;
	// protected ButtonComponent globalChatButton;

	@Inject(at = { @At("TAIL") }, method = { "init()V" }, cancellable = true)
	public void onInit(CallbackInfo ci) {
		/*
		 * MinecraftClient mc = MinecraftClient.getInstance(); int guiScale =
		 * mc.getWindow().calculateScaleFactor(mc.options.getGuiScale().getValue(),
		 * mc.forcesUnicodeFont());
		 *
		 *
		 * // Create server chat button. serverChatButton = new ButtonComponent(null,
		 * "Server Chat", new Runnable() {
		 * 
		 * @Override public void run() { GlobalChat.chatType =
		 * GlobalChat.ChatType.Minecraft; serverChatButton.setBackgroundColor(new
		 * Color(56, 56, 56)); globalChatButton.setBackgroundColor(new Color(128, 128,
		 * 128)); } }, new Color(192, 192, 192), new Color(56, 56, 56));
		 * serverChatButton.setSize(new Rectangle((float)(chatField.getX() * guiScale),
		 * (float)((chatField.getY() - chatField.getHeight() - 10) * guiScale), 140f,
		 * 30f));
		 * 
		 * // Create global chat button globalChatButton = new ButtonComponent(null,
		 * "Global Chat", new Runnable() {
		 * 
		 * @Override public void run() { GlobalChat.chatType =
		 * GlobalChat.ChatType.Global; globalChatButton.setBackgroundColor(new Color(56,
		 * 56, 56)); serverChatButton.setBackgroundColor(new Color(128, 128, 128)); } },
		 * new Color(192, 192, 192), new Color(128, 128, 128));
		 * serverChatButton.setSize(new Rectangle((float)((chatField.getX() + 80 *
		 * guiScale)), (float)((chatField.getY() - chatField.getHeight() - 10) *
		 * guiScale), 140f, 30f));
		 * 
		 * serverChatButton.setVisible(true); globalChatButton.setVisible(true);
		 */
	}

	@Override
	protected void onClose(CallbackInfo ci) {
		/*
		 * serverChatButton.setVisible(false); globalChatButton.setVisible(false);
		 */
	}

	@Override
	protected void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		super.onRender(context, mouseX, mouseY, delta, ci);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		MinecraftClient mc = MinecraftClient.getInstance();
		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();

		int guiScale = mc.getWindow().calculateScaleFactor(mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont());
		matrixStack.scale(1.0f / guiScale, 1.0f / guiScale, 1.0f);
		/*
		 * serverChatButton.draw(context, delta); globalChatButton.draw(context, delta);
		 */
		matrixStack.pop();
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	@Inject(at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addToMessageHistory(Ljava/lang/String;)V", shift = At.Shift.AFTER) }, method = "sendMessage(Ljava/lang/String;Z)V", cancellable = true)
	public void onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
		if (message.startsWith(CommandManager.PREFIX.getValue())) {
			Payload.getInstance().commandManager.command(message.split(" "));
			ci.cancel();
		} else if (GlobalChat.chatType == ChatType.Global) {
			Payload.getInstance().globalChat.SendMessage(message);
			ci.cancel();
		}
	}
}
