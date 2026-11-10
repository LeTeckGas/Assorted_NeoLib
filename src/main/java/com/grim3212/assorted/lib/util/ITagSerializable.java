package com.grim3212.assorted.lib.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/**
 * 1.21 port: saving ItemStacks needs registry access, so both methods take a {@link HolderLookup.Provider}
 * (same as NeoForge's INBTSerializable). Block entities get one in saveAdditional/loadAdditional.
 */
public interface ITagSerializable<T extends Tag> {
    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
