package net.payload.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.payload.module.modules.render.ItemTags;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.payload.Payload;
import net.payload.module.modules.render.Nametags;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.payload.PayloadClient.MC;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Shadow @Final private TextRenderer textRenderer;
    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;


    /*@Inject(at = @At("HEAD"),
            method = "render",
            cancellable = true)
    public void render(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (state.displayName == null) {
            state.displayName = Text.literal("null");
        }
    }

     */

    @Inject(at = @At("HEAD"),
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            cancellable = true)
    private void onRenderLabelIfPresent(S state, Text text,
                                        MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                        CallbackInfo ci)
    {

        if (this.dispatcher.targetedEntity instanceof PlayerEntity) {
            if (Payload.getInstance().moduleManager.nametags.state.getValue() && Payload.getInstance().moduleManager.nametags.isMinecraft()) {
                namesRenderLabelIfPresent(state, text, matrices, vertexConsumers, light);
                ci.cancel();
            }
            else {
                ci.cancel();
            }
        }
        else if (this.dispatcher.targetedEntity instanceof ItemEntity) {
            if (Payload.getInstance().moduleManager.itemTags.state.getValue()) {
                namesRenderLabelIfPresent(state, text, matrices, vertexConsumers, light);
                ci.cancel();
            }
        }
    }
    protected void namesRenderLabelIfPresent(S state, Text text,
                                             MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {
        Nametags nameTags = Payload.getInstance().moduleManager.nametags;

        Vec3d attVec = state.nameLabelPos;

       if (attVec == null) {
            return;
        }

        boolean notSneaky = !state.sneaking || nameTags.state.getValue();

        int labelY = "deadmau5".equals(text.getString()) ? -10 : 0;

        matrices.push();
        matrices.multiply(dispatcher.getRotation());

        float scale = 0.025F * nameTags.getNametagScale();

        if(nameTags.state.getValue())
        {
            Vec3d entityPos = new Vec3d(state.x, state.y, state.z);
            double distance = MC.player.getPos().distanceTo(entityPos);
            double VertOffset = distance / 25;
            if (distance > 10) {
                scale *= distance / 10;
                matrices.translate(attVec.x, attVec.y + VertOffset, attVec.z);
            }

            else if (distance <= 10) {
                matrices.translate(attVec.x, attVec.y + 0.5, attVec.z);
            }
        }
        matrices.scale(scale, -scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float bgOpacity =
                MC.options.getTextBackgroundOpacity(0.25F);
        int bgColor = (int)(bgOpacity * 255F) << 24;
        TextRenderer tr = getTextRenderer();
        float labelX = -tr.getWidth(text) / 2;

        // adjust layers if using NameTags in see-through mode
        /*TextRenderer.TextLayerType bgLayer = notSneaky && !nameTags.isSeeThrough() ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;

        TextRenderer.TextLayerType textLayer = nameTags.isSeeThrough() ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;

         */
        TextRenderer.TextLayerType bgLayer = TextRenderer.TextLayerType.SEE_THROUGH;
        TextRenderer.TextLayerType textLayer = TextRenderer.TextLayerType.SEE_THROUGH;

        // draw background
        if (nameTags.getBackdrop()) {
            tr.draw(text, labelX, labelY, 0x20FFFFFF, false, matrix,
                    vertexConsumers, bgLayer, bgColor, light);
        }

        // draw text
        if (notSneaky)
            tr.draw(text, labelX, labelY, nameTags.getColorBank().getColorAsInt(), false, matrix,
                    vertexConsumers, textLayer, 0, light);

        matrices.pop();
    }

    /**
     * Disables the nametag distance limit if configured in NameTags.
     */
    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;getSquaredDistanceToCamera(Lnet/minecraft/entity/Entity;)D"),
            method = "updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V")
    private double fakeSquaredDistanceToCamera(
            EntityRenderDispatcher dispatcher, Entity entity,
            Operation<Double> original,
            @Share("actualDistanceSq") LocalDoubleRef actualDistanceSq)
    {
        actualDistanceSq.set(original.call(dispatcher, entity));

        if (Payload.getInstance().moduleManager.nametags.isUnlimitedRange())
            return 0;

        return actualDistanceSq.get();
    }


    @Inject(at = @At("TAIL"),
            method = "updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V")
    private void restoreSquaredDistanceToCamera(T entity, S state,
                                                float tickDelta, CallbackInfo ci,
                                                @Share("actualDistanceSq") LocalDoubleRef actualDistanceSq)
    {
        state.squaredDistanceToCamera = actualDistanceSq.get();
    }

    @Shadow
    public abstract TextRenderer getTextRenderer();
}
