package com.grim3212.assorted.lib.core.conditions;

import com.google.gson.JsonObject;
import com.grim3212.assorted.lib.conditions.ForgeConditionProvider;
import com.grim3212.assorted.lib.platform.Services;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Recipe provider that can attach lib conditions to recipes by id.
 * <p>
 * 1.21 port: recipes are written through {@link RecipeOutput} now. Subclasses implement
 * {@link #registerConditions()} and {@link #buildRecipes(RecipeOutput)}; the conditions registered for a recipe id
 * are added automatically when that recipe is saved.
 */
public abstract class ConditionalRecipeProvider extends RecipeProvider {

    protected final Map<ResourceLocation, List<LibConditionProvider>> conditions;
    private final String modId;

    public ConditionalRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
        super(output, registries);
        this.modId = modId;
        this.conditions = new HashMap<>();
    }

    public LibConditionProvider and(LibConditionProvider... providers) {
        return Services.CONDITIONS.and(providers);
    }

    public LibConditionProvider or(LibConditionProvider... providers) {
        return Services.CONDITIONS.or(providers);
    }

    public LibConditionProvider not(LibConditionProvider provider) {
        return Services.CONDITIONS.not(provider);
    }

    public LibConditionProvider itemTagExists(TagKey<Item> itemTag) {
        return Services.CONDITIONS.itemTagExists(itemTag);
    }

    public LibConditionProvider modLoaded(String modId) {
        return Services.CONDITIONS.modLoaded(modId);
    }

    public LibConditionProvider partEnabled(String part) {
        return Services.CONDITIONS.partEnabled(part);
    }

    public Ingredient and(Ingredient... ingredients) {
        return Services.INGREDIENTS.and(ingredients);
    }

    public Ingredient or(Ingredient... ingredients) {
        return Services.INGREDIENTS.or(ingredients);
    }

    public Ingredient difference(Ingredient base, Ingredient subtracted) {
        return Services.INGREDIENTS.difference(base, subtracted);
    }

    public Ingredient nbt(ItemStack item) {
        return Services.INGREDIENTS.nbt(item);
    }

    public Ingredient fluid(@Nullable TagKey<Item> itemTagKey, TagKey<Fluid> fluidTagKey, long amount) {
        return Services.INGREDIENTS.fluid(itemTagKey, fluidTagKey, amount);
    }

    public Ingredient fluid(TagKey<Fluid> fluidTagKey) {
        return Services.INGREDIENTS.fluid(null, fluidTagKey, Services.FLUIDS.getBucketAmount());
    }

    protected String name(Item i) {
        return id(i).getPath();
    }

    protected String name(Block b) {
        return id(b).getPath();
    }

    protected ResourceLocation id(Item i) {
        return Services.PLATFORM.getRegistry(Registries.ITEM).getRegistryName(i);
    }

    protected ResourceLocation id(Block b) {
        return Services.PLATFORM.getRegistry(Registries.BLOCK).getRegistryName(b);
    }

    protected ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(this.modId, name);
    }

    public void addConditions(LibConditionProvider condition, ResourceLocation... recipes) {
        if (recipes.length == 0)
            return;

        for (ResourceLocation recipe : recipes) {
            this.conditions.computeIfAbsent(recipe, (r) -> new ArrayList<>()).add(condition);
        }
    }

    public void writeConditions(ResourceLocation id, JsonObject json) {
        if (this.conditions.containsKey(id))
            Services.CONDITIONS.write(json, this.conditions.get(id).toArray(new LibConditionProvider[0]));
    }

    /**
     * Used to register the conditions for recipes before the recipes are built
     * Makes sure that the conditions are available when building the recipe JSONs
     */
    public abstract void registerConditions();

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput, HolderLookup.Provider holderLookup) {
        this.conditions.clear();
        this.registerConditions();
        this.buildRecipes(new ConditionalOutput(recipeOutput));
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
    }

    /**
     * Adds the registered conditions of a recipe id to everything that gets saved.
     */
    private class ConditionalOutput implements RecipeOutput {
        private final RecipeOutput parent;

        private ConditionalOutput(RecipeOutput parent) {
            this.parent = parent;
        }

        @Override
        public Advancement.Builder advancement() {
            return this.parent.advancement();
        }

        @Override
        public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... extraConditions) {
            List<LibConditionProvider> registered = conditions.getOrDefault(id, List.of());
            ICondition[] all = Stream.concat(
                    registered.stream().map(ForgeConditionProvider::toCondition),
                    Arrays.stream(extraConditions)
            ).toArray(ICondition[]::new);
            this.parent.accept(id, recipe, advancement, all);
        }
    }
}
