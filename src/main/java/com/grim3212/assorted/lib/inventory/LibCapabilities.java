package com.grim3212.assorted.lib.inventory;

import com.grim3212.assorted.lib.core.inventory.IInventoryBlockEntity;
import com.grim3212.assorted.lib.core.inventory.IInventoryItem;
import com.grim3212.assorted.lib.core.inventory.IItemStorageHandler;
import com.grim3212.assorted.lib.core.inventory.IPlatformInventoryStorageHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21 port: NeoForge capabilities have to be registered up front in {@link RegisterCapabilitiesEvent}.
 * <p>
 * Every block entity that implements {@link IInventoryBlockEntity} and every item that implements
 * {@link IInventoryItem} automatically exposes an item handler, so mods don't have to register anything themselves.
 */
public final class LibCapabilities {

    private LibCapabilities() {
    }

    public static void register(final RegisterCapabilitiesEvent event) {
        // We can't know which block entity class a type creates, so check the instance when asked
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            registerBlockEntity(event, type);
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IInventoryItem inventoryItem) {
                event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> toItemHandler(inventoryItem.getStorageHandler(stack), null), item);
            }
        }
    }

    private static <T extends BlockEntity> void registerBlockEntity(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (blockEntity, side) -> {
            if (blockEntity instanceof IInventoryBlockEntity inventoryBlockEntity) {
                return toItemHandler(inventoryBlockEntity.getStorageHandler(), side);
            }
            return null;
        });
    }

    @Nullable
    public static IItemHandler toItemHandler(@Nullable IPlatformInventoryStorageHandler handler, @Nullable Direction side) {
        if (handler instanceof ForgePlatformInventoryStorageHandlerSided sided) {
            return sided.getItemHandler(side);
        }
        if (handler instanceof ForgePlatformInventoryStorageHandlerUnsided unsided) {
            return unsided.getItemHandler();
        }
        if (handler != null) {
            IItemStorageHandler storage = handler.getItemStorageHandler(side);
            return storage == null ? null : new ForgeItemStorageHandler(storage);
        }
        return null;
    }
}
