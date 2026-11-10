package com.grim3212.assorted.lib.crafting.ingredient;

import com.grim3212.assorted.lib.LibConstants;
import com.grim3212.assorted.lib.core.crafting.ingredient.LibFluidIngredient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Ingredient that matches any fluid container holding at least {@code amount} of a fluid from {@code fluid}.
 * Optionally the container itself must also be in the item tag {@code item}.
 * <p>
 * JSON: {"type": "assortedlib:stored_fluid_ingredient", "fluid": "minecraft:water", "item": "c:buckets", "amount": 1000}
 * <p>
 * 1.21 port: implemented as a NeoForge {@link ICustomIngredient} with codecs instead of the old JSON serializer.
 */
public record ForgeFluidIngredient(Optional<TagKey<Item>> itemTag, TagKey<Fluid> fluidTag, int amount) implements ICustomIngredient {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(LibConstants.MOD_ID, "stored_fluid_ingredient");

    public static final MapCodec<ForgeFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.ITEM).optionalFieldOf("item").forGetter(ForgeFluidIngredient::itemTag),
            TagKey.codec(Registries.FLUID).fieldOf("fluid").forGetter(ForgeFluidIngredient::fluidTag),
            Codec.INT.optionalFieldOf("amount", FluidType.BUCKET_VOLUME).forGetter(ForgeFluidIngredient::amount)
    ).apply(instance, ForgeFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeFluidIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.ITEM, rl), TagKey::location)), ForgeFluidIngredient::itemTag,
            ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location), ForgeFluidIngredient::fluidTag,
            ByteBufCodecs.VAR_INT, ForgeFluidIngredient::amount,
            ForgeFluidIngredient::new);

    public static final IngredientType<ForgeFluidIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    public static Ingredient of(@Nullable TagKey<Item> itemTag, TagKey<Fluid> fluidTag) {
        return of(itemTag, fluidTag, FluidType.BUCKET_VOLUME);
    }

    public static Ingredient of(@Nullable TagKey<Item> itemTag, TagKey<Fluid> fluidTag, int amount) {
        return new ForgeFluidIngredient(Optional.ofNullable(itemTag), fluidTag, amount).toVanilla();
    }

    private LibFluidIngredient common() {
        return new LibFluidIngredient(this.itemTag.orElse(null), this.fluidTag, this.amount);
    }

    @Override
    public boolean test(ItemStack input) {
        return common().test(input);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return common().getMatchingStacks().stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
