package com.grim3212.assorted.lib.conditions;

import com.grim3212.assorted.lib.LibConstants;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.function.Function;

/**
 * Recipe condition that passes when a tag contains at least one entry.
 * JSON: {"type": "assortedlib:item_tag_populated", "tag": "c:ingots/tin"}
 */
public abstract class TagPopulatedCondition<T> implements ICondition {

    private final TagKey<T> tag;

    public TagPopulatedCondition(ResourceKey<? extends Registry<T>> registry, ResourceLocation tag) {
        this.tag = TagKey.create(registry, tag);
    }

    public ResourceLocation tag() {
        return this.tag.location();
    }

    @Override
    public boolean test(IContext context) {
        return !context.getTag(this.tag).isEmpty();
    }

    @Override
    public String toString() {
        return this.tag.registry().location().getPath() + "_tag_populated(\"" + this.tag.location() + "\")";
    }

    private static <C extends TagPopulatedCondition<?>> MapCodec<C> createCodec(Function<ResourceLocation, C> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("tag").forGetter(TagPopulatedCondition::tag)
        ).apply(instance, factory));
    }

    public static class ItemTagPopulatedCondition extends TagPopulatedCondition<Item> {

        public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(LibConstants.MOD_ID, "item_tag_populated");
        public static final MapCodec<ItemTagPopulatedCondition> CODEC = createCodec(ItemTagPopulatedCondition::new);

        public ItemTagPopulatedCondition(ResourceLocation tag) {
            super(Registries.ITEM, tag);
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }

    public static class BlockTagPopulatedCondition extends TagPopulatedCondition<Block> {

        public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(LibConstants.MOD_ID, "block_tag_populated");
        public static final MapCodec<BlockTagPopulatedCondition> CODEC = createCodec(BlockTagPopulatedCondition::new);

        public BlockTagPopulatedCondition(ResourceLocation tag) {
            super(Registries.BLOCK, tag);
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }
}
