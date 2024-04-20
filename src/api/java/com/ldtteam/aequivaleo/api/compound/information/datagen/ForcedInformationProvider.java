package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.ldtteam.aequivaleo.api.compound.information.datagen.LockedInformationProvider.LOCKED_PATH;
import static com.ldtteam.aequivaleo.api.compound.information.datagen.ValueInformationProvider.VALUE_PATH;

/**
 * Base class for information providers that provide information for the forced case.
 * <p>
 *     Forced information overwrites and calculated information, however base information is still added to it.
 *     This ensures that the base information is always available.
 * </p>
 * <p>
 *     This kind of information should be used for root ingredients, that can not be crafted by the player.
 * </p>
 * <p>
 *     In practice this means that it writes the same information to both locked and value paths.
 * </p>
 */
public abstract class ForcedInformationProvider extends AbstractInformationProvider
{
    private final String        modId;
    private final DataGenerator dataGenerator;

    /**
     * Creates a new forced information provider.
     *
     * @param modId The mod id.
     * @param dataGenerator The data generator.
     * @param holderLookupProvider The holder lookup provider.
     */
    protected ForcedInformationProvider(final String modId, final DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> holderLookupProvider)
    {
        super(holderLookupProvider);
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    /**
     * Gets the paths to write the data to.
     *
     * @param worldPath The path of the world to write the data to.
     * @return The paths to write the data to.
     * @implNote The forced information is always written to the locked and value paths, structured as "data/{modId}/aequivaleo/locked/{worldPath}" and "data/{modId}/aequivaleo/value/{worldPath}".
     */
    @Override
    protected final Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> result = Sets.newLinkedHashSet();

        result.add(dataGenerator.getPackOutput().getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, LOCKED_PATH, worldPath)));
        result.add(dataGenerator.getPackOutput().getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, VALUE_PATH, worldPath)));

        return result;
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
        return StringUtils.capitalize(modId) + " " + "forced information data generator.";
    }
}
