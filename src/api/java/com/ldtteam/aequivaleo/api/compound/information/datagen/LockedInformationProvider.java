package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;

/**
 * Base class for information providers that provide information for the locked case.
 * <p>
 *     Locked information is always added to the information of the container.
 * </p>
 * <p>
 *     It can as such be used to overwrite calculated information.
 * </p>
 */
public abstract class LockedInformationProvider extends AbstractInformationProvider
{

    /**
     * The path to write the locked information to.
     */
    static final String LOCKED_PATH = "locked";

    private final String modId;
    private final DataGenerator dataGenerator;

    /**
     * Creates a new locked information provider.
     *
     * @param modId The mod id.
     * @param dataGenerator The data generator.
     */
    protected LockedInformationProvider(final String modId, final DataGenerator dataGenerator)
    {
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    /**
     * Gets the paths to write the data to.
     *
     * @param worldPath The path of the world to write the data to.
     * @return The paths to write the data to.
     * @implNote The locked information is always written to the locked path, structured as "data/{modId}/aequivaleo/locked/{worldPath}".
     */
    @NotNull
    @Override
    protected final Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> pathSet = Sets.newLinkedHashSet();
        pathSet.add(dataGenerator.getPackOutput().getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, LOCKED_PATH, worldPath)));
        return pathSet;
    }

    /**
     * Gets the name of the information provider.
     *
     * @return The name of the information provider.
     */
    @NotNull
    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " " + LOCKED_PATH + " information data generator.";
    }
}
