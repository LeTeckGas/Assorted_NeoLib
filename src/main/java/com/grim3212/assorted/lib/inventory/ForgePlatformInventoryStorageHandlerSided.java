package com.grim3212.assorted.lib.inventory;

import com.grim3212.assorted.lib.core.inventory.IItemStorageHandler;
import com.grim3212.assorted.lib.core.inventory.IPlatformInventoryStorageHandler;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 1.21 port: caches one {@link IItemHandler} per side instead of a LazyOptional per side.
 */
public class ForgePlatformInventoryStorageHandlerSided implements IPlatformInventoryStorageHandler {

    private final Function<Direction, IItemStorageHandler> handler;
    private final Map<Direction, Optional<IItemHandler>> itemHandlers;

    public ForgePlatformInventoryStorageHandlerSided(Function<Direction, IItemStorageHandler> handler) {
        this.handler = handler;
        this.itemHandlers = new HashMap<>();
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction direction) {
        return this.itemHandlers.computeIfAbsent(direction, dir -> {
            IItemStorageHandler newHandler = this.handler.apply(dir);
            return newHandler == null ? Optional.empty() : Optional.of(new ForgeItemStorageHandler(newHandler));
        }).orElse(null);
    }

    @Override
    public void invalidate() {
        this.itemHandlers.clear();
    }

    @Override
    public IItemStorageHandler getItemStorageHandler(@Nullable Direction direction) {
        return this.handler.apply(direction);
    }
}
