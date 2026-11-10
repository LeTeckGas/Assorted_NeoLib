package com.grim3212.assorted.lib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/**
 * Helper for reading/writing simple values on ItemStacks and CompoundTags.
 * <p>
 * 1.21 port: ItemStacks no longer carry a raw NBT tag. All ItemStack methods now read from and write to the
 * {@code minecraft:custom_data} data component. Important differences to the 1.20.1 version:
 * <ul>
 *     <li>Getters no longer write a default value into the stack, they just return the default.</li>
 *     <li>{@link #getTag(ItemStack, String)} returns a <b>copy</b>. Changes to it are not saved unless you call
 *     {@link #putTag(ItemStack, String, CompoundTag)} afterwards.</li>
 * </ul>
 * New code should prefer dedicated data components over this helper.
 */
public class NBTHelper {

    // =============== INTERNAL ===============
    private static CompoundTag read(ItemStack stack) {
        // copyTag() is a copy, so callers can never mutate the stored component by accident
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void write(ItemStack stack, Consumer<CompoundTag> modifier) {
        if (stack.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, modifier);
    }

    private static boolean has(ItemStack stack, String keyName) {
        return !stack.isEmpty() && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(keyName);
    }

    private static int[] toArray(BlockPos pos) {
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }

    private static BlockPos fromArray(int[] pos) {
        return pos.length >= 3 ? new BlockPos(pos[0], pos[1], pos[2]) : BlockPos.ZERO;
    }

    // =============== GENERAL ===============
    public static boolean hasTag(ItemStack itemStack, String keyName) {
        return has(itemStack, keyName);
    }

    public static void removeTag(ItemStack itemStack, String keyName) {
        if (has(itemStack, keyName)) {
            write(itemStack, tag -> tag.remove(keyName));
        }
    }

    public static CompoundTag getTag(CompoundTag compound, String keyName) {
        if (compound == null || !compound.contains(keyName)) {
            return new CompoundTag();
        }

        return compound.getCompound(keyName);
    }

    /**
     * @return a copy of the sub compound. Use {@link #putTag(ItemStack, String, CompoundTag)} to save changes.
     */
    public static CompoundTag getTag(ItemStack stack, String keyName) {
        return read(stack).getCompound(keyName);
    }

    public static void putTag(ItemStack stack, String keyName, CompoundTag compound) {
        write(stack, tag -> tag.put(keyName, compound));
    }

    // =============== STRING ===============
    public static String getString(CompoundTag compound, String keyName) {
        return compound == null ? "" : compound.getString(keyName);
    }

    public static void putString(CompoundTag compound, String keyName, String keyValue) {
        compound.putString(keyName, keyValue);
    }

    public static String getString(ItemStack itemStack, String keyName) {
        return read(itemStack).getString(keyName);
    }

    public static void putString(ItemStack itemStack, String keyName, String keyValue) {
        write(itemStack, tag -> tag.putString(keyName, keyValue));
    }

    public static ItemStack putStringItemStack(ItemStack itemStack, String keyName, String keyValue) {
        putString(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== BOOLEAN ===============
    public static boolean getBoolean(ItemStack itemStack, String keyName) {
        return read(itemStack).getBoolean(keyName);
    }

    public static void putBoolean(ItemStack itemStack, String keyName, boolean keyValue) {
        write(itemStack, tag -> tag.putBoolean(keyName, keyValue));
    }

    public static ItemStack putBooleanItemStack(ItemStack itemStack, String keyName, boolean keyValue) {
        putBoolean(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== BYTE ===============
    public static byte getByte(ItemStack itemStack, String keyName) {
        return read(itemStack).getByte(keyName);
    }

    public static void putByte(ItemStack itemStack, String keyName, byte keyValue) {
        write(itemStack, tag -> tag.putByte(keyName, keyValue));
    }

    public static ItemStack putByteItemStack(ItemStack itemStack, String keyName, byte keyValue) {
        putByte(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== SHORT ===============
    public static short getShort(ItemStack itemStack, String keyName) {
        return read(itemStack).getShort(keyName);
    }

    public static void putShort(ItemStack itemStack, String keyName, short keyValue) {
        write(itemStack, tag -> tag.putShort(keyName, keyValue));
    }

    public static ItemStack putShortItemStack(ItemStack itemStack, String keyName, short keyValue) {
        putShort(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== INTEGER ===============
    public static int getInt(CompoundTag compound, String keyName) {
        return compound == null ? 0 : compound.getInt(keyName);
    }

    public static void putInt(CompoundTag compound, String keyName, int keyValue) {
        compound.putInt(keyName, keyValue);
    }

    public static int getInt(ItemStack itemStack, String keyName) {
        return read(itemStack).getInt(keyName);
    }

    public static int getInt(ItemStack itemStack, String keyName, int fallback) {
        CompoundTag tag = read(itemStack);
        return tag.contains(keyName) ? tag.getInt(keyName) : fallback;
    }

    public static void putInt(ItemStack itemStack, String keyName, int keyValue) {
        write(itemStack, tag -> tag.putInt(keyName, keyValue));
    }

    public static ItemStack putIntItemStack(ItemStack itemStack, String keyName, int keyValue) {
        putInt(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== LONG ===============
    public static long getLong(CompoundTag compound, String keyName) {
        return compound == null ? 0L : compound.getLong(keyName);
    }

    public static void putLong(CompoundTag compound, String keyName, long keyValue) {
        compound.putLong(keyName, keyValue);
    }

    public static long getLong(ItemStack itemStack, String keyName) {
        return read(itemStack).getLong(keyName);
    }

    public static void putLong(ItemStack itemStack, String keyName, long keyValue) {
        write(itemStack, tag -> tag.putLong(keyName, keyValue));
    }

    public static ItemStack putLongItemStack(ItemStack itemStack, String keyName, long keyValue) {
        putLong(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== FLOAT ===============
    public static float getFloat(ItemStack itemStack, String keyName) {
        return read(itemStack).getFloat(keyName);
    }

    public static void putFloat(ItemStack itemStack, String keyName, float keyValue) {
        write(itemStack, tag -> tag.putFloat(keyName, keyValue));
    }

    public static ItemStack putFloatItemStack(ItemStack itemStack, String keyName, float keyValue) {
        putFloat(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== DOUBLE ===============
    public static double getDouble(ItemStack itemStack, String keyName) {
        return read(itemStack).getDouble(keyName);
    }

    public static void putDouble(ItemStack itemStack, String keyName, double keyValue) {
        write(itemStack, tag -> tag.putDouble(keyName, keyValue));
    }

    public static ItemStack putDoubleItemStack(ItemStack itemStack, String keyName, double keyValue) {
        putDouble(itemStack, keyName, keyValue);
        return itemStack;
    }

    // =============== BLOCK POS ===============
    public static BlockPos getBlockPos(ItemStack itemStack, String keyName) {
        return fromArray(read(itemStack).getIntArray(keyName));
    }

    public static BlockPos getBlockPos(CompoundTag tag, String keyName) {
        return tag == null ? BlockPos.ZERO : fromArray(tag.getIntArray(keyName));
    }

    public static void putBlockPos(CompoundTag tag, String keyName, BlockPos keyValue) {
        tag.putIntArray(keyName, toArray(keyValue));
    }

    public static void putBlockPos(ItemStack itemStack, String keyName, BlockPos keyValue) {
        write(itemStack, tag -> tag.putIntArray(keyName, toArray(keyValue)));
    }

    public static ItemStack putBlockPosItemStack(ItemStack itemStack, String keyName, BlockPos keyValue) {
        putBlockPos(itemStack, keyName, keyValue);
        return itemStack;
    }
}
