package com.udu3324.poinpow.mixin;

import com.udu3324.poinpow.utils.BlockLobbyMapAds;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public class MapMixin {

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void render(
            ItemFrameEntityRenderState itemFrameEntityRenderState,
            MatrixStack matrixStack,
            OrderedRenderCommandQueue orderedRenderCommandQueue,
            CameraRenderState cameraRenderState,
            CallbackInfo ci
    ) {
        BlockLobbyMapAds.block(itemFrameEntityRenderState);
    }
}