package com.grim3212.assorted.lib.platform;

import com.grim3212.assorted.lib.registry.ILoaderRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 1.21 port: NeoForge uses the vanilla registries directly, so this just wraps a {@link Registry}.
 */
public class ForgeRegistryWrapper<T> implements ILoaderRegistry<T> {

    private final Registry<T> registry;

    private ForgeRegistryWrapper(Registry<T> registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public static <T> ILoaderRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
        if (registry == null) {
            throw new IllegalArgumentException("No built-in registry found for " + key.location());
        }
        return new ForgeRegistryWrapper<>(registry);
    }

    @Override
    public Stream<T> getValues() {
        return this.registry.stream();
    }

    @Override
    public Optional<T> getValue(ResourceLocation resourceLocation) {
        return this.registry.getOptional(resourceLocation);
    }

    @Override
    public boolean containsKey(ResourceLocation resourceLocation) {
        return this.registry.containsKey(resourceLocation);
    }

    @Override
    public boolean contains(T entry) {
        return this.registry.getResourceKey(entry).isPresent();
    }

    @Override
    public ResourceLocation getRegistryName(T entry) {
        ResourceLocation key = this.registry.getKey(entry);
        if (key == null && this.registry instanceof DefaultedRegistry<T> defaulted) {
            return defaulted.getDefaultKey();
        }
        return key;
    }
}
