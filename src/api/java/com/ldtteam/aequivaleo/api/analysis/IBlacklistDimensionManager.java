package com.ldtteam.aequivaleo.api.analysis;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Manager which deals with blacklisted dimensions.
 * Dimensions which are blacklisted are not analyzed.
 */
public interface IBlacklistDimensionManager
{
    /**
     * Gives access to the manager.
     *
     * @return The manager.
     */
    static IBlacklistDimensionManager getInstance() {
        return IAequivaleoAPI.getInstance().getBlacklistDimensionManager();
    }

    /**
     * Blacklists a world with the given registry key.
     *
     * @param worldRegistryKey The key for the dimension to blacklist.
     * @return The instance.
     */
    default IBlacklistDimensionManager blacklist(final ResourceKey<Level> worldRegistryKey) {
        return this.blacklist(worldRegistryKey.location());
    }

    /**
     * Blacklists a world with the given name.
     *
     * @param worldKey The name for the dimension to blacklist.
     * @return The instance.
     */
    IBlacklistDimensionManager blacklist(final ResourceLocation worldKey);

    /**
     * Indicates if a dimension with the given key is blacklisted.
     *
     * @param worldRegistryKey They key of the dimension to check.
     * @return {@code true} when the dimension is blacklisted.
     */
    default boolean isBlacklisted(final ResourceKey<Level> worldRegistryKey) {
        return isBlacklisted(worldRegistryKey.location());
    }

    /**
     * Indicates if a dimension with the given name is blacklisted.
     *
     * @param worldKey They name of the dimension to check.
     * @return {@code true} when the dimension is blacklisted.
     */
    boolean isBlacklisted(final ResourceLocation worldKey);
}
