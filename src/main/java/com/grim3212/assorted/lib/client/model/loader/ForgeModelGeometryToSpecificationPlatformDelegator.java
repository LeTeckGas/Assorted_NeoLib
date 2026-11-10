package com.grim3212.assorted.lib.client.model.loader;

import com.grim3212.assorted.lib.client.model.loaders.IModelSpecification;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public final class ForgeModelGeometryToSpecificationPlatformDelegator<T extends IModelSpecification<T>>
        implements IUnbakedGeometry<ForgeModelGeometryToSpecificationPlatformDelegator<T>> {

    // 1.21: NeoForge no longer passes the model location into bake. Kept for the lib's IModelSpecification API.
    private static final ResourceLocation UNKNOWN_LOCATION = ResourceLocation.fromNamespaceAndPath("assortedlib", "unknown_model");

    private final T delegate;

    public ForgeModelGeometryToSpecificationPlatformDelegator(final T delegate) {
        this.delegate = delegate;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        final ForgeModelBakingContextDelegate contextDelegate = new ForgeModelBakingContextDelegate(baker::getModel, context);

        return new ForgeBakedModelDelegate(delegate.bake(contextDelegate, baker, spriteGetter, modelState, UNKNOWN_LOCATION));
    }
}
