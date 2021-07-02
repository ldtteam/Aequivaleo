package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;

import static com.ldtteam.aequivaleo.api.compound.information.datagen.LockedInformationProvider.LOCKED_PATH;
import static com.ldtteam.aequivaleo.api.compound.information.datagen.ValueInformationProvider.VALUE_PATH;

public abstract class ForcedInformationProvider extends AbstractInformationProvider
{
    private final String        modId;
    private final DataGenerator dataGenerator;

    protected ForcedInformationProvider(final String modId, final DataGenerator dataGenerator)
    {
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    @Override
    protected Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> result = Sets.newLinkedHashSet();

        result.add(dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, LOCKED_PATH, worldPath)));
        result.add(dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, VALUE_PATH, worldPath)));

        return result;
    }

    @NotNull
    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " " + "forced information data generator.";
    }
}
