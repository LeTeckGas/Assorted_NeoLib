package com.grim3212.assorted.lib.platform;

import com.grim3212.assorted.lib.dist.Dist;
import com.grim3212.assorted.lib.platform.services.IPlatformHelper;
import com.grim3212.assorted.lib.registry.ILoaderRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public String getCommonTagPrefix() {
        return "c"; // 1.21: NeoForge and Fabric share the "c" namespace for common tags
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraDataWriter) {
        player.openMenu(provider, extraDataWriter::accept);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }


    @Override
    public boolean isProduction() {
        return FMLLoader.isProduction();
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public double getPlayerReachDistance(Player player) {
        return player.blockInteractionRange();
    }

    @Override
    public Dist getCurrentDistribution() {
        return switch (FMLEnvironment.dist) {
            case CLIENT -> Dist.CLIENT;
            case DEDICATED_SERVER -> Dist.DEDICATED_SERVER;
        };
    }

    @Override
    public boolean isPhysicalClient() {
        return this.getCurrentDistribution() == Dist.CLIENT;
    }


    @Override
    public <T> ILoaderRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return ForgeRegistryWrapper.getRegistry(key);
    }

    public static Map<ResourceKey<CreativeModeTab>, Supplier<List<ItemStack>>> tabsToRegister = new HashMap<>();

    @Override
    public void modifyCreativeTab(ResourceKey<CreativeModeTab> key, Supplier<List<ItemStack>> displayStacks) {
        tabsToRegister.put(key, displayStacks);
    }

    @Override
    public void addReloadListener(ResourceLocation identifier, PreparableReloadListener reloadListener) {
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(reloadListener));
    }


    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> builder, Block... blocks) {
        return BlockEntityType.Builder.of(builder::apply, blocks).build(null);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        return IMenuTypeExtension.create(factory::create);
    }

    @Override
    public int getFuelTime(ItemStack stack) {
        return stack.getBurnTime(null);
    }

    @Override
    public boolean isTieredTool(ItemStack stack, Tiers minTier, ToolType toolType) {
        if (stack.getItem() instanceof TieredItem itemTier) {
            // 1.21: TierSortingRegistry is gone, tiers are compared through their "incorrect for drops" tags
            if (stack.canPerformAction(getItemAbilityForType(toolType))) {
                return IPlatformHelper.getTierLevel(itemTier.getTier()) >= IPlatformHelper.getTierLevel(minTier);
            }
        }
        return false;
    }

    private ItemAbility getItemAbilityForType(ToolType type) {
        switch (type) {
            case PICKAXE -> {
                return ItemAbilities.PICKAXE_DIG;
            }
            case SHOVEL -> {
                return ItemAbilities.SHOVEL_DIG;
            }
            case AXE -> {
                return ItemAbilities.AXE_DIG;
            }
            case HOE -> {
                return ItemAbilities.HOE_DIG;
            }
        }
        return null;
    }
}
