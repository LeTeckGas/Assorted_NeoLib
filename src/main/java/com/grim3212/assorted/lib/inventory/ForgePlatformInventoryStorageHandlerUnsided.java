package com.grim3212.assorted.lib.inventory;

import com.grim3212.assorted.lib.core.inventory.IItemStorageHandler;
import com.grim3212.assorted.lib.core.inventory.IPlatformInventoryStorageHandler;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21 port: NeoForge capabilities no longer use LazyOptional. The item handler is created once and handed out by
 * {@link LibCapabilities} when NeoForge asks for {@code Capabilities.ItemHandler}.
 */
public class ForgePlatformInventoryStorageHandlerUnsided implements IPlatformInventoryStorageHandler {

    private final IItemStorageHandler handler;
    private final IItemHandler itemHandler;

    public ForgePlatformInventoryStorageHandlerUnsided(IItemStorageHandler handler) {
        this.handler = handler;
        this.itemHandler = new ForgeItemStorageHandler(handler);
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    @Override
    public void invalidate() {
        // Nothing cached that could become invalid. Block entities should call
        // Level#invalidateCapabilities(BlockPos) themselves when their handler changes.
    }

    @Override
    public IItemStorageHandler getItemStorageHandler(@Nullable Direction direction) {
        return this.handler;
    }
}
