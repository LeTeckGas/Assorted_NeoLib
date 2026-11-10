package com.grim3212.assorted.lib.worldgen;

import com.grim3212.assorted.lib.LibConstants;
import com.grim3212.assorted.lib.platform.ForgeWorldGenHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

public class LibForgeWorldGen {
    @Nullable
    private static MapCodec<ForgeBiomeModifier> libBiomeModifierCodec = null;

    public static void init(IEventBus modEventBus) {
        modEventBus.<RegisterEvent>addListener(event -> {
            event.register(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, registry -> {
                registry.register(ResourceLocation.fromNamespaceAndPath(LibConstants.MOD_ID, "lib_biome_modifier"), libBiomeModifierCodec = MapCodec.unit(ForgeBiomeModifier.INSTANCE));
            });
        });
    }

    private static class ForgeBiomeModifier implements BiomeModifier {

        private static final ForgeBiomeModifier INSTANCE = new ForgeBiomeModifier();

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            ForgeWorldGenHelper.modifyBiome(biome, phase, builder);
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            if (libBiomeModifierCodec != null) {
                return libBiomeModifierCodec;
            } else {
                return MapCodec.unit(INSTANCE);
            }
        }

    }
}
