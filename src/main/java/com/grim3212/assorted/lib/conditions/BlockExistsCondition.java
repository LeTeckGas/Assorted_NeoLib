package com.grim3212.assorted.lib.conditions;

import com.grim3212.assorted.lib.LibConstants;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe condition that passes when a block with the given id is registered.
 * JSON: {"type": "assortedlib:block_exists", "block": "minecraft:stone"}
 */
public record BlockExistsCondition(ResourceLocation block) implements ICondition {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(LibConstants.MOD_ID, "block_exists");
    public static final MapCodec<BlockExistsCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(BlockExistsCondition::block)
    ).apply(instance, BlockExistsCondition::new));

    @Override
    public boolean test(IContext context) {
        return BuiltInRegistries.BLOCK.containsKey(this.block);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "block_exists(\"" + this.block + "\")";
    }
}
