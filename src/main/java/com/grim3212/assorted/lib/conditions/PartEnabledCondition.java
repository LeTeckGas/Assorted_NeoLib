package com.grim3212.assorted.lib.conditions;

import com.grim3212.assorted.lib.LibConstants;
import com.grim3212.assorted.lib.platform.ForgeConditionHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe condition that passes when a config "part" registered through
 * {@link com.grim3212.assorted.lib.platform.services.IConditionHelper#registerPartCondition} is enabled.
 * JSON: {"type": "assortedlib:part_enabled", "part": "some_part"}
 */
public record PartEnabledCondition(String part) implements ICondition {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(LibConstants.MOD_ID, "part_enabled");
    public static final MapCodec<PartEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("part").forGetter(PartEnabledCondition::part)
    ).apply(instance, PartEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        if (!ForgeConditionHelper.REGISTERED_PARTS.containsKey(this.part)) {
            throw new IllegalArgumentException("Can't check part that doesn't exist: " + this.part);
        }

        return ForgeConditionHelper.REGISTERED_PARTS.get(this.part).get();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "part_enabled(\"" + this.part + "\")";
    }
}
