package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;

/**
 * Base class for information providers that provide information for the base case.
 * <p>
 *     Base information is always added to the information of the container,
 *     even when the container is forced.
 * </p>
 * <p>
 *     This ensures that the base information is always available.
 * </p>
 */
public abstract class BaseInformationProvider extends AbstractInformationProvider
{
    private static final String BASE_PATH = "base";

    private final String modId;
    private final DataGenerator dataGenerator;

    /**
     * Creates a new base information provider.
     *
     * @param modId The mod id.
     * @param dataGenerator The data generator.
     */
    protected BaseInformationProvider(final String modId, final DataGenerator dataGenerator)
    {
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    /**
     * Gets the paths to write the data to.
     *
     * @param worldPath The path of the world to write the data to.
     * @return The paths to write the data to.
     * @implNote The base information is always written to the base path, structured as "data/{modId}/aequivaleo/base/{worldPath}".
     */
    @Override
    protected final Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> pathSet = Sets.newLinkedHashSet();
        pathSet.add(dataGenerator.getPackOutput().getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, BASE_PATH, worldPath)));
        return pathSet;
    }

    /**
     * Gets the name of the information provider.
     * @return The name of the information provider.
     */
    @NotNull
    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " " + BASE_PATH + " information data generator.";
    }
}
