package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class BaseInformationProvider extends AbstractInformationProvider
{
    public static final String BASE_PATH = "base";

    private final String modId;
    private final DataGenerator dataGenerator;

    protected BaseInformationProvider(final String modId, final DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> holderLookupProvider)
    {
        super(holderLookupProvider);
        this.modId = modId;
        this.dataGenerator = dataGenerator;
    }

    @Override
    protected Set<Path> getPathsToWrite(String worldPath)
    {
        final Set<Path> pathSet = Sets.newLinkedHashSet();
        pathSet.add(dataGenerator.getPackOutput().getOutputFolder().resolve(String.format("data/%s/aequivaleo/%s/%s", modId, BASE_PATH, worldPath)));
        return pathSet;
    }

    @NotNull
    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " " + BASE_PATH + " information data generator.";
    }
}
