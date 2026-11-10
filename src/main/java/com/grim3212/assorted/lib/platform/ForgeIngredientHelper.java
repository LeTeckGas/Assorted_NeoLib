package com.grim3212.assorted.lib.platform;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import com.grim3212.assorted.lib.crafting.ingredient.ForgeFluidIngredient;
import com.grim3212.assorted.lib.platform.services.IIngredientHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.*;
import org.jetbrains.annotations.Nullable;

public class ForgeIngredientHelper implements IIngredientHelper {
    @Override
    public void register() {
        // 1.21: ingredient types are registered in registerTypes during RegisterEvent
    }

    /**
     * Called from the mod event bus by {@link com.grim3212.assorted.lib.AssortedLibForge}.
     */
    public static void registerTypes(final RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.INGREDIENT_TYPES, helper -> helper.register(ForgeFluidIngredient.NAME, ForgeFluidIngredient.TYPE));
    }

    @Override
    public Ingredient and(Ingredient... ingredients) {
        return ingredients.length == 0 ? Ingredient.EMPTY : ingredients.length == 1 ? ingredients[0] : IntersectionIngredient.of(ingredients);
    }

    @Override
    public Ingredient or(Ingredient... ingredients) {
        return ingredients.length == 0 ? Ingredient.EMPTY : CompoundIngredient.of(ingredients);
    }

    @Override
    public Ingredient difference(Ingredient base, Ingredient subtracted) {
        return DifferenceIngredient.of(base, subtracted);
    }

    @Override
    public Ingredient nbt(ItemStack item) {
        return DataComponentIngredient.of(true, item.copy());
    }

    @Override
    public Ingredient fluid(@Nullable TagKey<Item> itemTagKey, TagKey<Fluid> fluidTagKey, long amount) {
        return ForgeFluidIngredient.of(itemTagKey, fluidTagKey, (int) amount);
    }
}
