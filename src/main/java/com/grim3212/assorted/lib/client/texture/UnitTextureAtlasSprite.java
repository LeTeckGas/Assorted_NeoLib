package com.grim3212.assorted.lib.client.texture;

import com.grim3212.assorted.lib.LibConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;

/**
 * A helper sprite with UVs spanning the entire texture.
 * <p>
 * Useful for baking quads that won't be used with an atlas.
 */
public class UnitTextureAtlasSprite extends TextureAtlasSprite {
    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(LibConstants.MOD_ID, "unit");
    public static final UnitTextureAtlasSprite INSTANCE = new UnitTextureAtlasSprite();

    private UnitTextureAtlasSprite() {
        super(LOCATION, new SpriteContents(LOCATION, new FrameSize(1, 1), new NativeImage(1, 1, false), ResourceMetadata.EMPTY), 1, 1, 0, 0);
    }

    @Override
    public float getU(float u) {
        return u;
    }

    @Override
    public float getV(float v) {
        return v;
    }
}
