package com.grim3212.assorted.lib.core.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

/**
 * Implement on an Item to decide whether an enchantment can be applied to it at the enchanting table.
 * <p>
 * 1.21 port: enchantments are data driven now, so the enchantment is passed as a {@link Holder}.
 * On NeoForge this is hooked into {@code IItemExtension#isPrimaryItemFor}.
 */
public interface IItemEnchantmentCondition {

    default Optional<Boolean> assortedlib_canApplyAtEnchantingTable(ItemStack stack, Holder<Enchantment> enchantment) {
        return Optional.empty();
    }
}
