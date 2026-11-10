package com.grim3212.assorted.lib.platform;

import com.google.common.collect.Maps;
import com.grim3212.assorted.lib.client.events.ClientTickHandler;
import com.grim3212.assorted.lib.client.model.loader.ForgePlatformModelLoaderPlatformDelegate;
import com.grim3212.assorted.lib.client.model.loaders.IModelSpecificationLoader;
import com.grim3212.assorted.lib.client.render.IBEWLR;
import com.grim3212.assorted.lib.client.screen.LibScreenFactory;
import com.grim3212.assorted.lib.platform.services.IClientHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class ForgeClientHelper implements IClientHelper {

    private static final Map<String, Registrations> registrationsMap = Maps.newConcurrentMap();
    private static final Map<Item, BlockEntityWithoutLevelRenderer> bewlrs = Maps.newConcurrentMap();

    @Override
    public <T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>> void registerScreen(Supplier<MenuType<? extends T>> menuType, LibScreenFactory<T, S> factory) {
        getRegistration().menuTypes.put(menuType::get, factory);
    }

    @Override
    public void registerAdditionalModel(List<ResourceLocation> modelLocations) {
        getRegistration().extraModels.addAll(modelLocations);
    }

    @Override
    public void addReloadListener(ResourceLocation identifier, PreparableReloadListener reloadListener) {
        getRegistration().clientReloadListeners.add(reloadListener);
    }

    @Override
    public void registerBEWLR(final Consumer<IBEWLR> register) {
        getRegistration().blockEntityWithoutLevelInitializers.add(register);
    }

    @Override
    public <E extends BlockEntity> void registerBlockEntityRenderer(Supplier<? extends BlockEntityType<? extends E>> entityType, BlockEntityRendererProvider<E> entityRendererFactory) {
        getRegistration().blockEntityRenderers.put((Supplier<BlockEntityType<?>>) (Supplier<? extends BlockEntityType<?>>) entityType, entityRendererFactory);
    }

    @Override
    public <E extends Entity> void registerEntityRenderer(Supplier<? extends EntityType<? extends E>> entityType, EntityRendererProvider<E> entityRendererFactory) {
        getRegistration().entityRenderers.put((Supplier<EntityType<?>>) (Supplier<? extends EntityType<?>>) entityType, entityRendererFactory);
    }

    @Override
    public void registerEntityLayer(ModelLayerLocation modelLayerLocation, Supplier<LayerDefinition> layerDefinition) {
        getRegistration().entityLayers.put(modelLayerLocation, layerDefinition);
    }

    @Override
    public void registerBlockColor(BlockColor color, Supplier<List<Block>> blocks) {
        getRegistration().blockColors.put(color, blocks);
    }

    @Override
    public void registerItemColor(ItemColor color, Supplier<List<Item>> items) {
        getRegistration().itemColors.put(color, items);
    }

    @Override
    public BlockColors getBlockColors() {
        return Minecraft.getInstance().getBlockColors();
    }

    @Override
    public ItemColors getItemColors() {
        return Minecraft.getInstance().getItemColors();
    }

    @Override
    public void registerModelLoader(ResourceLocation name, IModelSpecificationLoader<?> modelLoader) {
        getRegistration().modelLoaders.put(name, modelLoader);
    }

    @Override
    public void registerItemProperty(Supplier<Item> item, ResourceLocation location, ClampedItemPropertyFunction itemPropertyFunction) {
        getRegistration().itemProperties.put(item, Pair.of(location, itemPropertyFunction));
    }

    @Override
    public void registerRenderType(Supplier<Block> block, RenderType renderType) {
        getRegistration().renderTypes.put(block, renderType);
    }

    @Override
    public void registerKeyMapping(KeyMapping keyMapping) {
        getRegistration().keyMappings.add(keyMapping);
    }

    @Override
    public void registerClientTickStart(ClientTickHandler handler) {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> handler.handle(Minecraft.getInstance()));
    }

    @Override
    public void registerClientTickEnd(ClientTickHandler handler) {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> handler.handle(Minecraft.getInstance()));
    }

    @Override
    public <T extends ParticleOptions> void registerParticle(Supplier<ParticleType<T>> type, Function<SpriteSet, ParticleProvider<T>> particleFactory) {
        getRegistration().particleProviders.put((Supplier<ParticleType<?>>) (Supplier<? extends ParticleType<?>>) type, particleFactory::apply);
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static Registrations getRegistration() {
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        if (registrationsMap.containsKey(modId)) {
            return registrationsMap.get(modId);
        } else {
            Registrations newRegistration = new Registrations();
            registrationsMap.put(modId, newRegistration);
            ModLoadingContext.get().getActiveContainer().getEventBus().register(newRegistration);
            return newRegistration;
        }
    }

    public static Optional<BlockEntityWithoutLevelRenderer> getRenderer(Item item) {
        return Optional.ofNullable(bewlrs.get(item));
    }

    public static class Registrations {
        private final Map<Supplier<BlockEntityType<?>>, BlockEntityRendererProvider<?>> blockEntityRenderers = new HashMap<>();
        private final Map<Supplier<EntityType<?>>, EntityRendererProvider<?>> entityRenderers = new HashMap<>();
        private final Map<ModelLayerLocation, Supplier<LayerDefinition>> entityLayers = new HashMap<>();
        private final Map<BlockColor, Supplier<List<Block>>> blockColors = new HashMap<>();
        private final Map<ItemColor, Supplier<List<Item>>> itemColors = new HashMap<>();
        private final Map<Supplier<Item>, Pair<ResourceLocation, ClampedItemPropertyFunction>> itemProperties = new HashMap<>();
        private final Map<Supplier<Block>, RenderType> renderTypes = new HashMap<>();
        private final List<KeyMapping> keyMappings = new ArrayList<>();
        private final List<ResourceLocation> extraModels = new ArrayList<>();
        private final List<Consumer<IBEWLR>> blockEntityWithoutLevelInitializers = Collections.synchronizedList(new ArrayList<>());
        private final List<PreparableReloadListener> clientReloadListeners = new ArrayList<>();
        private final Map<ResourceLocation, IModelSpecificationLoader<?>> modelLoaders = new HashMap<>();
        private final Map<Supplier<ParticleType<?>>, Function<SpriteSet, ParticleProvider<?>>> particleProviders = new HashMap<>();
        private final Map<Supplier<MenuType<?>>, LibScreenFactory<?, ?>> menuTypes = new HashMap<>();

        @SubscribeEvent
        public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            for (Map.Entry<Supplier<BlockEntityType<?>>, BlockEntityRendererProvider<?>> entry : blockEntityRenderers.entrySet()) {
                event.registerBlockEntityRenderer(entry.getKey().get(), (BlockEntityRendererProvider<BlockEntity>) entry.getValue());
            }

            for (Map.Entry<Supplier<EntityType<?>>, EntityRendererProvider<?>> entry : entityRenderers.entrySet()) {
                event.registerEntityRenderer(entry.getKey().get(), (EntityRendererProvider<Entity>) entry.getValue());
            }
        }

        @SubscribeEvent
        public void registerEntityLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
            for (Map.Entry<ModelLayerLocation, Supplier<LayerDefinition>> entry : entityLayers.entrySet()) {
                event.registerLayerDefinition(entry.getKey(), entry.getValue());
            }
        }

        @SubscribeEvent
        public void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
            for (Map.Entry<BlockColor, Supplier<List<Block>>> entry : blockColors.entrySet()) {
                event.register(entry.getKey(), entry.getValue().get().toArray(Block[]::new));
            }
        }

        @SubscribeEvent
        public void registerItemColors(final RegisterColorHandlersEvent.Item event) {
            for (Map.Entry<ItemColor, Supplier<List<Item>>> entry : itemColors.entrySet()) {
                event.register(entry.getKey(), entry.getValue().get().toArray(ItemLike[]::new));
            }
        }

        @SubscribeEvent
        @SuppressWarnings("removal")
        public void clientSetup(final FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                for (Map.Entry<Supplier<Item>, Pair<ResourceLocation, ClampedItemPropertyFunction>> entry : itemProperties.entrySet()) {
                    ItemProperties.register(entry.getKey().get(), entry.getValue().getFirst(), entry.getValue().getSecond());
                }
            });

            for (Map.Entry<Supplier<Block>, RenderType> entry : renderTypes.entrySet()) {
                ItemBlockRenderTypes.setRenderLayer(entry.getKey().get(), entry.getValue());
            }

            blockEntityWithoutLevelInitializers.forEach(callback -> callback.accept((item, renderer) -> bewlrs.put(item, renderer)));
        }

        // 1.21: menu screens have their own registration event
        @SubscribeEvent
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void registerMenuScreens(final RegisterMenuScreensEvent event) {
            for (Map.Entry<Supplier<MenuType<?>>, LibScreenFactory<?, ?>> entry : menuTypes.entrySet()) {
                this.registerMenu(event, (MenuType) entry.getKey().get(), (LibScreenFactory) entry.getValue());
            }
        }

        private <T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>> void registerMenu(RegisterMenuScreensEvent event, MenuType<T> menuType, LibScreenFactory<T, S> factory) {
            event.register(menuType, factory::create);
        }

        @SubscribeEvent
        public void registerKeyMapping(final RegisterKeyMappingsEvent event) {
            for (KeyMapping key : keyMappings) {
                event.register(key);
            }
        }

        @SubscribeEvent
        public void registerAdditionalModels(final ModelEvent.RegisterAdditional event) {
            for (ResourceLocation location : extraModels) {
                event.register(ModelResourceLocation.standalone(location));
            }
        }

        @SubscribeEvent
        public void registerAdditionalModels(final RegisterClientReloadListenersEvent event) {
            for (PreparableReloadListener reloadListener : clientReloadListeners) {
                event.registerReloadListener(reloadListener);
            }
        }

        @SubscribeEvent
        public void registerModelLoaders(final ModelEvent.RegisterGeometryLoaders event) {
            for (Map.Entry<ResourceLocation, IModelSpecificationLoader<?>> entry : modelLoaders.entrySet()) {
                event.register(entry.getKey(), new ForgePlatformModelLoaderPlatformDelegate<>(entry.getValue()));
            }
        }

        @SubscribeEvent
        public void registerParticles(final RegisterParticleProvidersEvent event) {
            for (Map.Entry<Supplier<ParticleType<?>>, Function<SpriteSet, ParticleProvider<?>>> entry : particleProviders.entrySet()) {
                event.registerSpriteSet((ParticleType<ParticleOptions>) entry.getKey().get(), sprites -> (ParticleProvider<ParticleOptions>) entry.getValue().apply(sprites));
            }
        }
    }
}
