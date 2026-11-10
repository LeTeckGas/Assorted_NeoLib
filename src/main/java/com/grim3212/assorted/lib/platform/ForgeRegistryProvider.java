package com.grim3212.assorted.lib.platform;

import com.grim3212.assorted.lib.platform.services.IRegistryFactory;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ForgeRegistryProvider implements IRegistryFactory {

    @Override
    public <T> RegistryProvider<T> create(ResourceKey<? extends Registry<T>> resourceKey, String modId) {
        final var containerOpt = ModList.get().getModContainerById(modId);
        if (containerOpt.isEmpty())
            throw new NullPointerException("Cannot find mod container for id " + modId);
        final var modBus = containerOpt.get().getEventBus();
        if (modBus == null)
            throw new IllegalStateException("The mod " + modId + " has no mod event bus!");
        final DeferredRegister<T> register = DeferredRegister.create(resourceKey, modId);
        register.register(modBus);
        return new Provider<>(register);
    }

    private static class Provider<T> implements RegistryProvider<T> {
        private final DeferredRegister<T> registry;

        private final Set<IRegistryObject<T>> entries = new HashSet<>();
        private final Set<IRegistryObject<T>> entriesView = Collections.unmodifiableSet(entries);

        private Provider(DeferredRegister<T> registry) {
            this.registry = registry;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <I extends T> IRegistryObject<I> register(String name, Supplier<? extends I> supplier) {
            final DeferredHolder<T, I> obj = registry.register(name, supplier);
            final var ro = new IRegistryObject<I>() {

                @Override
                public ResourceKey<I> getResourceKey() {
                    return (ResourceKey<I>) (ResourceKey<?>) obj.getKey();
                }

                @Override
                public ResourceLocation getId() {
                    return obj.getId();
                }

                @Override
                public I get() {
                    return obj.get();
                }

                @Override
                public Holder<I> asHolder() {
                    return (Holder<I>) (Holder<?>) obj.getDelegate();
                }
            };
            entries.add((IRegistryObject<T>) ro);
            return ro;
        }

        @Override
        public Set<IRegistryObject<T>> getEntries() {
            return entriesView;
        }

        @Override
        public Stream<T> getValues() {
            return this.entriesView.stream().map(x -> x.get());
        }

        @Override
        public Optional<T> getValue(ResourceLocation resourceLocation) {
            Optional<T> value = entriesView.stream().filter(x -> x.getId().equals(resourceLocation)).map(x -> x.get()).findFirst();
            return value;
        }

        @Override
        public boolean contains(T entry) {
            return this.getValues().anyMatch(x -> entry.equals(x));
        }

        @Override
        public boolean containsKey(ResourceLocation resourceLocation) {
            return this.getValue(resourceLocation).isPresent();
        }

        @Override
        @SuppressWarnings("unchecked")
        public ResourceLocation getRegistryName(T entry) {
            Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(this.registry.getRegistryKey().location());
            return reg == null ? null : reg.getKey(entry);
        }
    }
}