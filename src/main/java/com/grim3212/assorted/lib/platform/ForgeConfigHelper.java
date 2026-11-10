package com.grim3212.assorted.lib.platform;

import com.grim3212.assorted.lib.LibConstants;
import com.grim3212.assorted.lib.config.ConfigurationType;
import com.grim3212.assorted.lib.config.ForgeDelegateConfigurationBuilder;
import com.grim3212.assorted.lib.config.IConfigurationBuilder;
import com.grim3212.assorted.lib.platform.services.IConfigHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;

public class ForgeConfigHelper implements IConfigHelper {

    @Override
    public IConfigurationBuilder createBuilder(final ConfigurationType type, final String name) {
        return new ForgeDelegateConfigurationBuilder(forgeConfigSpec -> {
            final String configName = String.format("%s.toml", name);
            final ModContainer container = ModLoadingContext.get().getActiveContainer();
            LibConstants.LOG.info(String.format("Building config '%s' for %s", configName, container.getModId()));
            container.registerConfig(remapType(type), forgeConfigSpec, configName);
        });
    }

    private static ModConfig.Type remapType(final ConfigurationType type) {
        return switch (type) {
            case CLIENT_ONLY -> ModConfig.Type.CLIENT;
            case NOT_SYNCED -> ModConfig.Type.COMMON;
            case SYNCED -> ModConfig.Type.SERVER;
        };
    }
}
