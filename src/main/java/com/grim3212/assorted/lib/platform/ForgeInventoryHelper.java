package com.grim3212.assorted.lib.platform;

import com.grim3212.assorted.lib.core.inventory.IInventoryBlockEntity;
import com.grim3212.assorted.lib.core.inventory.IInventoryItem;
import com.grim3212.assorted.lib.core.inventory.IItemStorageHandler;
import com.grim3212.assorted.lib.core.inventory.IPlatformInventoryStorageHandler;
import com.grim3212.assorted.lib.inventory.ForgeItemStorageHandler;
import com.grim3212.assorted.lib.inventory.ForgePlatformInventoryStorageHandlerSided;
import com.grim3212.assorted.lib.inventory.ForgePlatformInventoryStorageHandlerUnsided;
import com.grim3212.assorted.lib.inventory.ForgeWrappedItemHandler;
import com.grim3212.assorted.lib.platform.services.IInventoryHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class ForgeInventoryHelper implements IInventoryHelper {
    @Override
    public boolean canItemStacksStack(@NotNull ItemStack a, @NotNull ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    @Override
    public ItemStack copyStackWithSize(@NotNull ItemStack itemStack, int size) {
        return itemStack.copyWithCount(size);
    }

    @Override
    public Optional<IItemStorageHandler> getItemStorageHandler(ItemStack stack) {
        IItemHandler itemHandler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (itemHandler != null) {
            if (itemHandler instanceof ForgeItemStorageHandler forgeItemStorageHandler) {
                return Optional.of(forgeItemStorageHandler.getStorage());
            } else {
                return Optional.of(new ForgeWrappedItemHandler(null, itemHandler));
            }
        }

        // If we somehow fail to get it from capability try to see if its one of our own
        if (stack.getItem() instanceof IInventoryItem itemStackStorage) {
            IPlatformInventoryStorageHandler storageHandler = itemStackStorage.getStorageHandler(stack);
            if (storageHandler != null) {
                return Optional.of(storageHandler.getItemStorageHandler(null));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<IItemStorageHandler> getItemStorageHandler(BlockEntity blockEntity, @Nullable Direction direction) {
        // 1.21: block capabilities are queried through the level
        IItemHandler itemHandler = blockEntity.getLevel() == null ? null
                : blockEntity.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, direction);
        if (itemHandler != null) {
            if (itemHandler instanceof ForgeItemStorageHandler forgeItemStorageHandler) {
                return Optional.of(forgeItemStorageHandler.getStorage());
            } else {
                return Optional.of(new ForgeWrappedItemHandler(blockEntity, itemHandler));
            }
        }

        // If we somehow fail to get it from capability try to see if its one of our own
        if (blockEntity instanceof IInventoryBlockEntity inventoryBlockEntity) {
            IPlatformInventoryStorageHandler storageHandler = inventoryBlockEntity.getStorageHandler();
            if (storageHandler != null) {
                return Optional.of(storageHandler.getItemStorageHandler(direction));
            }
        }

        return Optional.empty();
    }

    @Override
    public IPlatformInventoryStorageHandler createStorageInventoryHandler(IItemStorageHandler handler) {
        return new ForgePlatformInventoryStorageHandlerUnsided(handler);
    }

    @Override
    public IPlatformInventoryStorageHandler createSidedStorageInventoryHandler(Function<Direction, IItemStorageHandler> handler) {
        return new ForgePlatformInventoryStorageHandlerSided(handler);
    }

    private static Container emptyInventory = new SimpleContainer(0);

    @Override
    public Container wrapStorageHandler(IItemStorageHandler handler) {
        // Forge should work without needing any Vanilla containers
        return emptyInventory;
    }
}
