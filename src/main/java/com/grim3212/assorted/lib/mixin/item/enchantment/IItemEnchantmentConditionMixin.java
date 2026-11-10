package com.grim3212.assorted.lib.mixin.item.enchantment;

import com.grim3212.assorted.lib.core.item.IItemEnchantmentCondition;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(IItemEnchantmentCondition.class)
public interface IItemEnchantmentConditionMixin extends IItemExtension {
    @Shadow
    Optional<Boolean> assortedlib_canApplyAtEnchantingTable(ItemStack stack, Holder<Enchantment> enchantment);

    @Override
    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return this.assortedlib_canApplyAtEnchantingTable(stack, enchantment).orElseGet(() -> IItemExtension.super.isPrimaryItemFor(stack, enchantment));
    }
}
