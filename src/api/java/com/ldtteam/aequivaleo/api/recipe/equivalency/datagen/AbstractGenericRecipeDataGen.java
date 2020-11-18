package com.ldtteam.aequivaleo.api.recipe.equivalency.datagen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IGenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeData;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AbstractGenericRecipeDataGen implements IDataProvider
{

    private final DataGenerator dataGenerator;
    @VisibleForTesting
    final         WorldData     generalData  = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    @VisibleForTesting
    final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();

    protected AbstractGenericRecipeDataGen(final DataGenerator dataGenerator) {this.dataGenerator = dataGenerator;}

    @Override
    public void act(final DirectoryCache cache) throws IOException
    {
        this.calculateDataToSave();

        final Gson gson = IAequivaleoAPI.getInstance().getGson();

        this.writeData(
          cache,
          gson,
          generalData
        );

        for (WorldData worldData : worldDataMap.values())
        {
            this.writeData(
              cache,
              gson,
              worldData
            );
        }
    }

    @VisibleForTesting
    void writeData(
      final DirectoryCache cache,
      final Gson gson,
      final WorldData worldData
    ) throws IOException
    {
        for (Map.Entry<ResourceLocation, GenericRecipeData> entry : worldData.getRecipes().entrySet())
        {
            ResourceLocation name = entry.getKey();

            final Path itemPath = dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/recipes/%s", name.getNamespace(), worldData.getPath())).resolve(String.format("%s.json", name.getPath()));

            IDataProvider.save(
              gson,
              cache,
              gson.toJsonTree(entry.getValue()),
              itemPath
            );
        }
    }

    public abstract void calculateDataToSave();

    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          worldId,
          name,
          inputs,
          Collections.emptySet(),
          outputs
        );
    }

    protected void saveData(
      final ResourceLocation worldId,
      final IGenericRecipeEquivalencyRecipe recipeEquivalencyRecipe
    ) {
        this.saveData(
          worldId,
          recipeEquivalencyRecipe.getRecipeName(),
          recipeEquivalencyRecipe.getInputs(),
          recipeEquivalencyRecipe.getRequiredKnownOutputs(),
          recipeEquivalencyRecipe.getOutputs()
        );
    }

    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.worldDataMap.computeIfAbsent(worldId, WorldData::new).recipes.put(name, new GenericRecipeData(inputs, requiredKnownOutputs, outputs));
    }

    protected void saveData(
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          name,
          inputs,
          Collections.emptySet(),
          outputs
        );
    }

    protected void saveData(
      final IGenericRecipeEquivalencyRecipe recipeEquivalencyRecipe
    ) {
        this.saveData(
          recipeEquivalencyRecipe.getRecipeName(),
          recipeEquivalencyRecipe.getInputs(),
          recipeEquivalencyRecipe.getRequiredKnownOutputs(),
          recipeEquivalencyRecipe.getOutputs()
        );
    }

    protected void saveData(
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs) {
        generalData.recipes.put(name, new GenericRecipeData(inputs, requiredKnownOutputs, outputs));
    }

    private static class WorldData {
        private final ResourceLocation                         worldId;
        private final Map<ResourceLocation, GenericRecipeData> recipes = Maps.newHashMap();

        private WorldData(final ResourceLocation worldId) {this.worldId = worldId;}

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public Map<ResourceLocation, GenericRecipeData> getRecipes()
        {
            return recipes;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }

}
