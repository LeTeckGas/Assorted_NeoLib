package com.grim3212.assorted.lib.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.grim3212.assorted.lib.core.conditions.LibCondition;
import com.grim3212.assorted.lib.core.conditions.LibConditionProvider;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Bridges the loader independent {@link LibCondition} (which works on raw JSON) into NeoForge's codec based
 * condition system.
 * <p>
 * Reading: the JSON of the condition is handed to {@link LibCondition#test(JsonObject)} and the result is stored.
 * Writing (datagen): a {@link LibConditionProvider} with the same name writes its JSON through {@link Provided}.
 */
public record RecipeConditionWrapper(ResourceLocation name, String stringName, boolean result) implements ICondition {

    private static final Map<ResourceLocation, MapCodec<ICondition>> CODECS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public boolean test(IContext context) {
        return this.result;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODECS.get(this.name);
    }

    @Override
    public String toString() {
        return stringName;
    }

    /**
     * Datagen side: a condition that only knows how to write itself as JSON.
     */
    public record Provided(LibConditionProvider provider) implements ICondition {
        @Override
        public boolean test(IContext context) {
            throw new UnsupportedOperationException("Provided conditions are only used for writing data");
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            MapCodec<ICondition> codec = CODECS.get(provider.getName());
            if (codec == null) {
                throw new IllegalStateException("No condition registered with name " + provider.getName());
            }
            return codec;
        }
    }

    public static MapCodec<ICondition> createCodec(ResourceLocation name, LibCondition condition) {
        MapCodec<ICondition> codec = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public <T> DataResult<ICondition> decode(DynamicOps<T> ops, MapLike<T> input) {
                JsonObject json = new JsonObject();
                input.entries().forEach(pair -> ops.getStringValue(pair.getFirst()).result()
                        .ifPresent(key -> json.add(key, ops.convertTo(JsonOps.INSTANCE, pair.getSecond()))));
                try {
                    return DataResult.success(new RecipeConditionWrapper(name, condition.toString(), condition.test(json)));
                } catch (JsonParseException e) {
                    return DataResult.error(() -> "Failed to read condition " + name + ": " + e.getMessage());
                }
            }

            @Override
            public <T> RecordBuilder<T> encode(ICondition input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                if (input instanceof Provided provided) {
                    JsonObject json = new JsonObject();
                    provided.provider().write(json);
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        if (!entry.getKey().equals("type")) {
                            prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
                        }
                    }
                }
                return prefix;
            }
        };
        CODECS.put(name, codec);
        return codec;
    }
}
