package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;

public abstract class LockedInformationProvider extends AbstractInformationProvider
{

    public static final String LOCKED_PATH = "locked";

    private final String modId;
    private final DataGenerator dataGenerator;

    protected LockedInformationProvider(final String modId, final DataGenerator dataGenerator)
    {
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    @Override
    protected Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> pathSet = Sets.newLinkedHashSet();
        pathSet.add(dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, LOCKED_PATH, worldPath)));
        return pathSet;
    }

    @NotNull
    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " " + LOCKED_PATH + " information data generator.";
    }
}
