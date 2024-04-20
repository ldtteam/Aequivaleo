package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for information providers that provide information for the value case.
 * <p>
 *     Value information can be seen as a calculation hint for the value of a object.
 *     It will be overwritten by locked information, and by calculated information, if desired.
 * </p>
 * <p>
 *     For the calculation it is considered a "candidate" and as such it is not guaranteed to be the final value.
 *     If another value is calculated, then the mediation engines will be invoked to determine the correct value.
 * </p>
 */
public abstract class ValueInformationProvider extends AbstractInformationProvider
{
    /**
     * The value path.
     */
    static final String VALUE_PATH = "value";

    private final String modId;
    private final DataGenerator dataGenerator;

    /**
     * Creates a new value information provider.
     *
     * @param modId The mod id.
     * @param dataGenerator The data generator.
     * @param holderLookupProvider The holder lookup provider.
     */
    protected ValueInformationProvider(final String modId, final DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> holderLookupProvider)
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
     * @implNote The value information is always written to the value path, structured as "data/{modId}/aequivaleo/value/{worldPath}".
     */
    @NotNull
    @Override
    protected final Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> pathSet = Sets.newLinkedHashSet();
        pathSet.add(dataGenerator.getPackOutput().getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, VALUE_PATH, worldPath)));
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
        return StringUtils.capitalize(modId) + " " + VALUE_PATH + " information data generator.";
    }
}
