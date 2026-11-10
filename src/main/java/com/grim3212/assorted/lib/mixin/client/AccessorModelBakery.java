package com.grim3212.assorted.lib.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 1.21 port: ModelBakery#getModel became package-private.
 */
@Mixin(ModelBakery.class)
public interface AccessorModelBakery {
    @Invoker("getModel")
    UnbakedModel assortedlib_getModel(ResourceLocation location);
}
