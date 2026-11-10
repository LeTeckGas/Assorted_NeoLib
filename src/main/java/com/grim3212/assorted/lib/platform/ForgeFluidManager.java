package com.grim3212.assorted.lib.platform;

import com.grim3212.assorted.lib.core.fluid.FluidInformation;
import com.grim3212.assorted.lib.core.fluid.IFluidVariantHandler;
import com.grim3212.assorted.lib.fluid.ForgeFluidVariantHandlerDelegate;
import com.grim3212.assorted.lib.platform.services.IFluidManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ForgeFluidManager implements IFluidManager {

    // 1.21: item capabilities are nullable instead of LazyOptional
    private static Optional<IFluidHandlerItem> handler(final ItemStack stack) {
        return Optional.ofNullable(stack.getCapability(Capabilities.FluidHandler.ITEM));
    }

    @Override
    public Optional<FluidInformation> get(final ItemStack stack) {
        return handler(stack)
                .map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE))
                .map(ForgeFluidManager::buildFluidInformation);
    }

    @Override
    public ItemStack extractFrom(final ItemStack stack, final long amount) {
        // 1.21: the container may be replaced (e.g. bucket -> empty bucket), so return the handler's container
        return handler(stack).map(handler -> {
            handler.drain((int) amount, IFluidHandler.FluidAction.EXECUTE);
            return handler.getContainer();
        }).orElse(stack);
    }

    @Override
    public long simulateExtract(ItemStack stack, long amount) {
        return handler(stack).map(handler -> handler.drain((int) amount, IFluidHandler.FluidAction.SIMULATE).getAmount()).orElse(0);
    }

    @Override
    public ItemStack insertInto(final ItemStack stack, final FluidInformation fluidInformation) {
        return handler(stack).map(handler -> {
            handler.fill(buildFluidStack(fluidInformation), IFluidHandler.FluidAction.EXECUTE);
            return handler.getContainer();
        }).orElse(stack);
    }

    @Override
    public long simulateInsert(ItemStack stack, FluidInformation fluidInformation) {
        return handler(stack).map(handler -> handler.fill(buildFluidStack(fluidInformation), IFluidHandler.FluidAction.SIMULATE)).orElse(0);
    }

    @Override
    public Component getDisplayName(final Fluid fluid) {
        return fluid.getFluidType().getDescription(buildFluidStack(new FluidInformation(fluid)));
    }

    /**
     * 1.21: fluid containers store their content in a data component instead of an NBT key.
     * Kept for API compatibility, returns the old key name.
     */
    @Override
    @Deprecated
    public String fluidStackTag() {
        return "Fluid";
    }

    @Override
    public Optional<IFluidVariantHandler> getVariantHandlerFor(Fluid fluid) {
        return Optional.of(new ForgeFluidVariantHandlerDelegate(fluid.getFluidType()));
    }

    /**
     * 1.21: FluidStacks carry data components instead of NBT. The {@link FluidInformation#data()} tag is mapped to
     * the {@code minecraft:custom_data} component of the FluidStack.
     */
    @NotNull
    public static FluidInformation buildFluidInformation(final FluidStack fluid) {
        CustomData data = fluid.get(DataComponents.CUSTOM_DATA);
        return new FluidInformation(fluid.getFluid(), fluid.getAmount(), data == null ? new CompoundTag() : data.copyTag());
    }

    @NotNull
    public static FluidStack buildFluidStack(final FluidInformation fluid) {
        FluidStack stack = new FluidStack(fluid.fluid(), (int) fluid.amount());
        if (fluid.data() != null && !fluid.data().isEmpty() && !stack.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(fluid.data()));
        }
        return stack;
    }
}
