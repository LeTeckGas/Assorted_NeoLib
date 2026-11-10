package com.grim3212.assorted.lib.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.grim3212.assorted.lib.LibConstants;
import com.grim3212.assorted.lib.conditions.*;
import com.grim3212.assorted.lib.core.conditions.LibCondition;
import com.grim3212.assorted.lib.core.conditions.LibConditionProvider;
import com.grim3212.assorted.lib.platform.services.IConditionHelper;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.*;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * NeoForge 1.21.1 implementation of {@link IConditionHelper}.
 * <p>
 * Conditions are codec based now. All condition codecs (the lib's own ones and the ones mods register through
 * {@link #register(ResourceLocation, LibCondition)}) are registered in {@link #registerCodecs(RegisterEvent)}.
 */
public class ForgeConditionHelper implements IConditionHelper {
    private static final Map<ResourceLocation, LibCondition> CUSTOM_CONDITIONS = new ConcurrentHashMap<>();
    public static final Map<String, Supplier<Boolean>> REGISTERED_PARTS = new HashMap<>();

    @Override
    public void init() {
        // Nothing to do here anymore, registration happens in registerCodecs
    }

    /**
     * Called from the mod event bus by {@link com.grim3212.assorted.lib.AssortedLibForge}.
     */
    public static void registerCodecs(final RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, helper -> {
            helper.register(BlockExistsCondition.NAME, BlockExistsCondition.CODEC);
            helper.register(PartEnabledCondition.NAME, PartEnabledCondition.CODEC);
            helper.register(TagPopulatedCondition.ItemTagPopulatedCondition.NAME, TagPopulatedCondition.ItemTagPopulatedCondition.CODEC);
            helper.register(TagPopulatedCondition.BlockTagPopulatedCondition.NAME, TagPopulatedCondition.BlockTagPopulatedCondition.CODEC);
            CUSTOM_CONDITIONS.forEach((name, condition) -> helper.register(name, RecipeConditionWrapper.createCodec(name, condition)));
        });
    }

    @Override
    public void write(JsonObject conditionalObject, LibConditionProvider... conditions) {
        if (conditions.length == 0)
            return;

        if (conditionalObject.has(getConditionsKey()))
            throw new IllegalArgumentException("Object already has a condition entry: " + conditionalObject);

        JsonArray conditionsJson = new JsonArray();
        for (LibConditionProvider condition : conditions) {
            conditionsJson.add(ICondition.CODEC.encodeStart(JsonOps.INSTANCE, ForgeConditionProvider.toCondition(condition)).getOrThrow());
        }
        conditionalObject.add(getConditionsKey(), conditionsJson);
    }

    @Override
    public void register(ResourceLocation name, LibCondition condition) {
        if (CUSTOM_CONDITIONS.putIfAbsent(name, condition) != null)
            LibConstants.LOG.warn("Duplicate condition with id: " + name);
    }

    @Override
    public boolean test(JsonObject json) {
        return ICondition.conditionsMatched(JsonOps.INSTANCE, json);
    }

    @Override
    public String getConditionsKey() {
        // 1.21: NeoForge uses "neoforge:conditions" instead of "conditions"
        return ConditionalOps.DEFAULT_CONDITIONS_KEY;
    }

    @Override
    public LibConditionProvider and(LibConditionProvider... values) {
        return wrap(new AndCondition(Arrays.stream(values).map(ForgeConditionProvider::toCondition).toList()));
    }

    @Override
    public LibConditionProvider not(LibConditionProvider value) {
        return wrap(new NotCondition(ForgeConditionProvider.toCondition(value)));
    }

    @Override
    public LibConditionProvider or(LibConditionProvider... values) {
        return wrap(new OrCondition(Arrays.stream(values).map(ForgeConditionProvider::toCondition).toList()));
    }

    @Override
    public LibConditionProvider blockExists(ResourceLocation block) {
        return wrap(new BlockExistsCondition(block));
    }

    @Override
    public LibConditionProvider itemExists(ResourceLocation item) {
        return wrap(new ItemExistsCondition(item));
    }

    @Override
    public LibConditionProvider blockTagExists(TagKey<Block> tag) {
        return wrap(new TagPopulatedCondition.BlockTagPopulatedCondition(tag.location()));
    }

    @Override
    public LibConditionProvider itemTagExists(TagKey<Item> tag) {
        return wrap(new TagPopulatedCondition.ItemTagPopulatedCondition(tag.location()));
    }

    @Override
    public LibConditionProvider modLoaded(String modId) {
        return wrap(new ModLoadedCondition(modId));
    }

    @Override
    public LibConditionProvider partEnabled(String partId) {
        return wrap(new PartEnabledCondition(partId));
    }

    @Override
    public void registerPartCondition(String part, Supplier<Boolean> check) {
        if (REGISTERED_PARTS.containsKey(part)) {
            throw new IllegalArgumentException("Can't have registered part with the same name as another");
        }
        REGISTERED_PARTS.put(part, check);
    }

    public static ForgeConditionProvider wrap(ICondition condition) {
        return new ForgeConditionProvider(condition);
    }
}
