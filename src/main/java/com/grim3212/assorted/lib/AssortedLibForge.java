package com.grim3212.assorted.lib;

import com.grim3212.assorted.lib.data.ForgeBiomeTagProvider;
import com.grim3212.assorted.lib.data.ForgeBlockTagProvider;
import com.grim3212.assorted.lib.data.ForgeItemTagProvider;
import com.grim3212.assorted.lib.data.LibCommonTagProvider;
import com.grim3212.assorted.lib.events.*;
import com.grim3212.assorted.lib.inventory.LibCapabilities;
import com.grim3212.assorted.lib.platform.ForgeConditionHelper;
import com.grim3212.assorted.lib.platform.ForgeIngredientHelper;
import com.grim3212.assorted.lib.platform.ForgeNetworkHelper;
import com.grim3212.assorted.lib.platform.ForgePlatformHelper;
import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.lib.worldgen.LibForgeWorldGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.concurrent.CompletableFuture;

@Mod(LibConstants.MOD_ID)
public class AssortedLibForge {
    // NeoForge passes the mod event bus directly into the constructor
    public AssortedLibForge(final IEventBus modBus) {
        LibConstants.LOG.info(LibConstants.MOD_NAME + " starting up...");

        modBus.addListener(this::gatherData);
        modBus.addListener(ForgeIngredientHelper::registerTypes);
        modBus.addListener(this::modifyCreativeTabs);
        modBus.addListener(ForgeNetworkHelper::registerPayloads);
        modBus.addListener(ForgeConditionHelper::registerCodecs);
        modBus.addListener(LibCapabilities::register);

        Services.EVENTS.registerEventType(UseBlockEvent.class, () -> {
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (final PlayerInteractEvent.RightClickBlock event) -> {
                final UseBlockEvent newEvent = new UseBlockEvent(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
                Services.EVENTS.handleEvents(newEvent);
                if (newEvent.isCanceled()) {
                    event.setCancellationResult(newEvent.getInteractionResult());
                    event.setCanceled(true);
                }
            });
        });

        Services.EVENTS.registerEventType(AnvilUpdatedEvent.class, () -> {
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (final AnvilUpdateEvent event) -> {
                final AnvilUpdatedEvent newEvent = new AnvilUpdatedEvent(event.getLeft(), event.getRight(), event.getName(), (int) event.getCost(), event.getPlayer());
                Services.EVENTS.handleEvents(newEvent);
                if (newEvent.isCanceled()) {
                    event.setCanceled(true);
                } else if (!newEvent.getOutput().isEmpty()) {
                    event.setOutput(newEvent.getOutput());
                    event.setCost(newEvent.getCost());
                    event.setMaterialCost(newEvent.getMaterialCost());
                }
            });
        });

        Services.EVENTS.registerEventType(EntityInteractEvent.class, () -> {
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, (final PlayerInteractEvent.EntityInteract event) -> {
                final EntityInteractEvent newEvent = new EntityInteractEvent(event.getEntity(), event.getHand(), event.getTarget());
                Services.EVENTS.handleEvents(newEvent);
                event.setCanceled(newEvent.isCanceled());
                event.setCancellationResult(newEvent.getInteractionResult());
            });
        });

        Services.EVENTS.registerEventType(LootTableModifyEvent.class, () -> {
            NeoForge.EVENT_BUS.addListener((final LootTableLoadEvent event) -> {
                final LootTableModifyEvent newEvent = new LootTableModifyEvent(event.getTable(), event.getName(), new ForgeLootTableModificationContext(event.getTable()), true);
                Services.EVENTS.handleEvents(newEvent);
            });
        });

        Services.CONDITIONS.init();

        LibForgeWorldGen.init(modBus);
    }

    private void modifyCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        for (var tab : ForgePlatformHelper.tabsToRegister.entrySet()) {
            if (event.getTabKey() == tab.getKey()) {
                event.acceptAll(tab.getValue().get());
            }
        }
    }

    private void gatherData(final GatherDataEvent event) {
        DataGenerator datagenerator = event.getGenerator();
        PackOutput packOutput = datagenerator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        ForgeBlockTagProvider blockTagProvider = new ForgeBlockTagProvider(packOutput, lookupProvider, fileHelper, LibConstants.MOD_ID, new LibCommonTagProvider.BlockTagProvider(packOutput, lookupProvider));
        datagenerator.addProvider(event.includeServer(), blockTagProvider);
        datagenerator.addProvider(event.includeServer(), new ForgeItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), fileHelper, LibConstants.MOD_ID, new LibCommonTagProvider.ItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter())));
        datagenerator.addProvider(event.includeServer(), new ForgeBiomeTagProvider(packOutput, lookupProvider, fileHelper, LibConstants.MOD_ID, new LibCommonTagProvider.BiomeTagProvider(packOutput, lookupProvider)));
    }
}
