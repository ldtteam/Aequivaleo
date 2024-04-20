package com.ldtteam.aequivaleo.api.config;

import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.common.ForgeI18n;

import java.util.List;
import java.util.function.Predicate;

/**
 * Defines the abstract structure of a configuration defining class in aequivaleo.
 * <p>
 * Can be used by plugins to create their own configs when they are loaded, so that they
 * can be accessed by the rest of the system.
 */
public abstract class AbstractAequivaleoConfiguration {
    /**
     * Creates a category in the config file.
     *
     * @param builder The builder to create the category in.
     * @param key     The key of the category.
     */
    protected void createCategory(final Builder builder, final String key) {
        builder.comment(ForgeI18n.parseMessage(commentTKey(key))).push(key);
    }

    /**
     * Swaps to a category in the config file.
     *
     * @param builder The builder to create the category in.
     * @param key The key of the category.
     */
    protected void swapToCategory(final Builder builder, final String key) {
        finishCategory(builder);
        createCategory(builder, key);
    }

    /**
     * Finishes a category in the config file.
     *
     * @param builder The builder to finish the category in.
     */
    protected void finishCategory(final Builder builder) {
        builder.pop();
    }

    /**
     * Creates a translation key for a config entry with the given key, to be displayed as the name of that config entry.
     *
     * @param key The key of the config entry.
     * @return The translation key for the config entry.
     */
    private static String nameTKey(final String key) {
        return Constants.MOD_ID + ".config." + key;
    }

    /**
     * Creates a translation key for a comment with the given key, to be displayed as the comment of a config entry.
     *
     * @param key The key of the config entry.
     * @return The translation key for the comment of the config entry.
     */
    private static String commentTKey(final String key) {
        return nameTKey(key) + ".comment";
    }

    /**
     * Builds the base of a config entry with the given key.
     * <p>
     * In particular this sets the comment and translation key of the config entry.
     * </p>
     *
     * @param builder The builder to build the config entry with.
     * @param key The key of the config entry.
     * @return The builder with the base of the config entry built.
     */
    private static Builder buildBase(final Builder builder, final String key) {
        return builder.comment(ForgeI18n.parseMessage(commentTKey(key))).translation(nameTKey(key));
    }

    /**
     * Defines a boolean config entry with the given key and default value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @return The defined boolean config entry.
     */
    protected static BooleanValue defineBoolean(final Builder builder, final String key, final boolean defaultValue) {
        return buildBase(builder, key).define(key, defaultValue);
    }

    /**
     * Defines an integer config entry with the given key, default value, without minimum value or maximum value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @return The defined integer config entry.
     */
    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue) {
        return defineInteger(builder, key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Defines an integer config entry with the given key, default value, minimum value and maximum value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @param min The minimum value of the config entry.
     * @param max The maximum value of the config entry.
     * @return The defined integer config entry.
     */
    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue, final int min, final int max) {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    /**
     * Defines a long config entry with the given key, default value, without minimum value or maximum value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @return The defined long config entry.
     */
    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue) {
        return defineLong(builder, key, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Defines a long config entry with the given key, default value, minimum value and maximum value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @param min The minimum value of the config entry.
     * @param max The maximum value of the config entry.
     * @return The defined long config entry.
     */
    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue, final long min, final long max) {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    /**
     * Defines a double config entry with the given key, default value, without minimum value or maximum value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @return The defined double config entry.
     */
    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue) {
        return defineDouble(builder, key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    /**
     * Defines a double config entry with the given key, default value, minimum value and maximum value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @param min The minimum value of the config entry.
     * @param max The maximum value of the config entry.
     * @return The defined double config entry.
     */
    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue, final double min, final double max) {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    /**
     * Defines a list config entry with the given key, default value and element validator.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @param elementValidator The validator for the elements of the list.
     * @param <T> The type of the elements of the list.
     * @return The defined list config entry.
     */
    protected static <T> ConfigValue<List<? extends T>> defineList(
            final Builder builder,
            final String key,
            final List<? extends T> defaultValue,
            final Predicate<Object> elementValidator) {
        return buildBase(builder, key).defineList(key, defaultValue, elementValidator);
    }

    /**
     * Defines an enum config entry with the given key and default value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @param <V> The type of the enum.
     * @return The defined enum config entry.
     */
    protected static <V extends Enum<V>> EnumValue<V> defineEnum(final Builder builder, final String key, final V defaultValue) {
        return buildBase(builder, key).defineEnum(key, defaultValue);
    }

    /**
     * Defines a string config entry with the given key and default value.
     *
     * @param builder The builder to define the config entry with.
     * @param key The key of the config entry.
     * @param defaultValue The default value of the config entry.
     * @return The defined string config entry.
     */
    protected static ConfigValue<String> defineString(final Builder builder, final String key, final String defaultValue) {
        return buildBase(builder, key).define(key, defaultValue);
    }
}
