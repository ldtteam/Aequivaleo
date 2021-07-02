package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;

public abstract class ValueInformationProvider extends AbstractInformationProvider
{
    public static final String VALUE_PATH = "value";

    private final String modId;
    private final DataGenerator dataGenerator;

    protected ValueInformationProvider(final String modId, final DataGenerator dataGenerator)
    {
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    @Override
    protected Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> pathSet = Sets.newLinkedHashSet();
        pathSet.add(dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, VALUE_PATH, worldPath)));
        return pathSet;
    }

    @NotNull
    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " " + VALUE_PATH + " information data generator.";
    }
}
