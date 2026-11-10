package com.grim3212.assorted.lib.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grim3212.assorted.lib.core.conditions.LibConditionProvider;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Map;

/**
 * Wraps a NeoForge {@link ICondition} so common code can pass it around as a {@link LibConditionProvider}.
 */
public class ForgeConditionProvider implements LibConditionProvider {

    public final ICondition condition;

    public ForgeConditionProvider(ICondition condition) {
        this.condition = condition;
    }

    @Override
    public void write(JsonObject json) {
        JsonElement encoded = ICondition.CODEC.encodeStart(JsonOps.INSTANCE, this.condition).getOrThrow();
        for (Map.Entry<String, JsonElement> entry : encoded.getAsJsonObject().entrySet()) {
            if (!entry.getKey().equals("type")) {
                json.add(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public ResourceLocation getName() {
        return NeoForgeRegistries.CONDITION_SERIALIZERS.getKey(this.condition.codec());
    }

    /**
     * Turns any {@link LibConditionProvider} into a NeoForge condition.
     */
    public static ICondition toCondition(LibConditionProvider provider) {
        if (provider instanceof ForgeConditionProvider forge) {
            return forge.condition;
        }
        return new RecipeConditionWrapper.Provided(provider);
    }
}
