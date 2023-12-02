package com.ldtteam.aequivaleo.vanilla.data;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.datagen.ForcedInformationProvider;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.vanilla.Registry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;

public class AequivaleoInformationProvider extends ForcedInformationProvider
{
    
    private final int terrestrialBlocksEarth = 4;
    private final int smallPlantsNature = 1;
    private final int logsNature = 16;
    private final int flowersNature = 4;
    private final int tallPlantsMultiplier = 2;
    private final int mostFoodNature = 4;
    private final int fireAsFuel = 1;
    private final int  fishWater = 4;
    private final int  coralNature = 1;
    private final int  coralWater = 1;
    private final int  coralBlockMultiplier = 4;
    
    public AequivaleoInformationProvider(final DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> holderLookupProvider)
    {
        super(Constants.MOD_ID, dataGenerator, holderLookupProvider);
    }
    
    @Override
    public void calculateDataToSave()
    {
        // plants and stuff
        savePlantsAndStuff();
        
        // aquatic stuff and fish
        saveAquaticStuffAndFish();
        
        // rocks and dirt
        saveRocksAndDirt();
        
        // most normal foods
        saveMostFoods(validation(mostFoodNature));
        
        // ores, metal ingots, gems
        saveOresAndMinerals();
        
        // random terrestrial nonsense
        saveMiscTerrestrialStuff();
        
        // nether
        saveNetherStuff();
        
        // ender
        saveEnderStuff();
        
        // containers
        saveData(Items.WATER_BUCKET, validation(64));
        saveData(Items.LAVA_BUCKET, validation(fireAsFuel * 100));
        
        // controversial
        saveData(Items.NETHER_STAR, validation(1600));
        saveData(Items.HEART_OF_THE_SEA, validation(1600));
    }
    
    private void saveMiscTerrestrialStuff() {
        saveData(Items.GLASS, validation(terrestrialBlocksEarth));
        saveData(Items.CHARCOAL, validation(fireAsFuel * 8));
        saveData(Items.BRICK, validation(16));
        saveData(Items.POISONOUS_POTATO, validation(mostFoodNature));
        saveData(Items.CLAY_BALL, validation(16));
        saveData(Items.FEATHER, validation(16));
        saveData(Items.BROWN_MUSHROOM_BLOCK, validation(smallPlantsNature));
        saveData(Items.RED_MUSHROOM_BLOCK, validation(smallPlantsNature));
        saveData(Items.COBWEB, validation(16));
        saveData(Items.PUMPKIN, validation(64));
        saveData(Items.CARVED_PUMPKIN, validation(4));
        saveData(Items.BELL, validation(128));
        saveData(Items.GUNPOWDER, validation(64));
        saveData(Items.HONEYCOMB, validation(32));
        saveData(Items.HONEY_BLOCK, validation(96));
        saveData(Items.ICE, validation(16));
        saveData(Items.MAGMA_BLOCK, validation(64));
        saveData(Items.OBSIDIAN, validation(terrestrialBlocksEarth));
        saveData(Items.PHANTOM_MEMBRANE, validation(256));
        saveData(Items.RABBIT_FOOT, validation(32));
        saveData(Items.ROTTEN_FLESH, validation(mostFoodNature));
        saveData(Items.SLIME_BALL, validation(32));
        saveData(Items.SNOWBALL, validation(4));
        saveData(Items.SPIDER_EYE, validation(mostFoodNature));
    }
    
    private void saveEnderStuff() {
        saveData(Items.CHORUS_FLOWER, validation(32), validation(8));
        saveData(Items.CHORUS_FRUIT, validation(16), validation(4));
        saveData(Items.POPPED_CHORUS_FRUIT, validation(16), validation(4), validation(fireAsFuel));
        saveData(Items.END_ROD, validation(fireAsFuel * 4), validation(1));
        saveData(Items.END_STONE, validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.ENDER_PEARL, validation(32));
        saveData(Items.SHULKER_SHELL, validation(640), validation(64));
    }
    
    private void saveNetherStuff() {
        saveData(Items.NETHER_WART, validation(4), validation(smallPlantsNature));
        saveData(Items.WARPED_WART_BLOCK, validation(36), validation(36));
        saveData(Items.NETHERRACK, validation((int)(terrestrialBlocksEarth / 4)), validation(4));
        saveData(Items.CRIMSON_NYLIUM, validation(terrestrialBlocksEarth), validation(logsNature), validation(16));
        saveData(Items.WARPED_NYLIUM, validation(terrestrialBlocksEarth), validation(logsNature), validation(16));
        saveData(Items.CRYING_OBSIDIAN, validation(64), validation(16), validation(32), validation(32));
        saveData(Items.GHAST_TEAR, validation(32), validation(24));
        saveData(Items.GLOWSTONE_DUST, validation(16));
        saveData(Items.SHROOMLIGHT, validation(32), validation(32), validation(16));
        saveData(Items.SOUL_SAND, validation(terrestrialBlocksEarth), validation(64));
        saveData(Items.SOUL_SOIL, validation(terrestrialBlocksEarth), validation(64));
        saveData(Items.BASALT, validation(terrestrialBlocksEarth), validation(4));
        saveData(Items.BLAZE_ROD, validation(fireAsFuel * 12), validation(16));
    }
    
    private void saveOresAndMinerals() {
        // ore blocks
        saveData(Items.COAL_ORE, validation(fireAsFuel * 8), validation(terrestrialBlocksEarth + 16), validation(1));
        saveData(Items.COAL, validation(fireAsFuel * 8), validation(16));
        saveData(Items.DIAMOND_ORE, validation(320), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.DIAMOND, validation(320));
        saveData(Items.EMERALD_ORE, validation(64), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.EMERALD, validation(64));
        saveData(Items.NETHER_GOLD_ORE, validation(72), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.GILDED_BLACKSTONE, validation(72), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.GOLD_ORE, validation(72), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.GOLD_INGOT, validation(72), validation(fireAsFuel));
        saveData(Items.IRON_ORE, validation(72), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.IRON_INGOT, validation(72), validation(fireAsFuel));
        saveData(Items.COPPER_ORE, validation(72), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.COPPER_INGOT, validation(72), validation(fireAsFuel));
        saveData(Items.REDSTONE_ORE, validation(32), validation(terrestrialBlocksEarth), validation(1));
        saveData(Items.REDSTONE, validation(32));
        saveData(Items.LAPIS_ORE, validation(48), validation(terrestrialBlocksEarth), validation(30), validation(1));
        saveData(Items.LAPIS_LAZULI, validation(48), validation(32));
        saveData(Items.NETHER_QUARTZ_ORE, validation(terrestrialBlocksEarth + 16), validation(8), validation(1));
        saveData(Items.QUARTZ, validation(16), validation(8));
        saveData(Items.ANCIENT_DEBRIS, validation(720), validation(600), validation(720), validation(720), validation(1));
        saveData(Items.NETHERITE_SCRAP, validation(720), validation(600), validation(720), validation(720));
    }
    
    private void saveRocksAndDirt() {
        saveTerrestrialBlocks(validation(terrestrialBlocksEarth));
        
        saveData(Items.MOSSY_COBBLESTONE, validation(terrestrialBlocksEarth), validation(smallPlantsNature));
        saveData(Items.PODZOL, validation(terrestrialBlocksEarth), validation(smallPlantsNature));
        saveData(Items.GRASS_BLOCK, validation(terrestrialBlocksEarth), validation(smallPlantsNature));
        saveData(Items.MYCELIUM, validation(terrestrialBlocksEarth), validation(smallPlantsNature));
    }
    
    private void saveAquaticStuffAndFish() {
        // coral, there's a lot of it
        saveCoral(validation(coralNature), validation(coralWater));
        saveCoralBlocks(validation(coralNature * coralBlockMultiplier), validation(coralWater * coralBlockMultiplier));
        
        // other aquatic stuff
        saveData(Items.PRISMARINE_CRYSTALS, validation(32), validation(3), validation(6));
        saveData(Items.PRISMARINE_SHARD, validation(16), validation(2), validation(4));
        saveData(Items.SCUTE, validation(64), validation(64));
        saveData(Items.SEA_PICKLE, validation(48), validation(4), validation(4), validation(16));
        saveData(Items.SEAGRASS, validation(4), validation(4));
        saveData(Items.TURTLE_EGG, validation(32), validation(64));
        saveData(Items.INK_SAC, validation(48), validation(48), validation(48));
        saveData(Items.GLOW_INK_SAC, validation(48), validation(48), validation(48));
        saveData(Items.WET_SPONGE, validation(64), validation(64), validation(32));
        saveData(Items.NAUTILUS_SHELL, validation(320), validation(32));
        saveMostFish(validation(mostFoodNature), validation(fishWater));
    }
    
    private void savePlantsAndStuff() {
        saveLeavesAndSmallPlants(validation(smallPlantsNature));
        saveTallPlants(validation(smallPlantsNature * tallPlantsMultiplier));
        saveLogsAndSaplings(validation(logsNature));
        saveFlowersAndDye(validation(flowersNature));
        saveDoubleFlowers(validation(flowersNature * 2));
        saveData(Items.BAMBOO, validation((int)(logsNature / 16)));
        saveData(Items.BONE, validation(flowersNature * 3), validation(48));
        saveData(Items.STRING, validation((int)(flowersNature / 4)));
    }
    
    private void saveMostFish(CompoundInstance nature, CompoundInstance water) {
        saveData(Items.SALMON, nature, water);
        saveData(Items.COD, nature, water);
        saveData(Items.PUFFERFISH, nature, water);
        saveData(Items.TROPICAL_FISH, nature, water);
    }
    
    private void saveMostFoods(CompoundInstance nature) {
        saveData(Items.APPLE, nature);
        saveData(Items.BEEF, nature);
        saveData(Items.BEETROOT_SEEDS, nature);
        saveData(Items.CRIMSON_FUNGUS, nature);
        saveData(Items.EGG, nature);
        saveData(Items.CARROT, nature);
        saveData(Items.CHICKEN, nature);
        saveData(Items.MELON_SLICE, nature);
        saveData(Items.MUTTON, nature);
        saveData(Items.RABBIT, nature);
        saveData(Items.SUGAR_CANE, nature);
        saveData(Items.WARPED_FUNGUS, nature);
        saveData(Items.WHEAT, nature);
        saveData(Items.WHEAT_SEEDS, nature);
        saveData(Items.MILK_BUCKET, nature);
        saveData(Items.PORKCHOP, nature);
        saveData(Items.POTATO, nature);
        saveData(Items.RABBIT_HIDE, nature);
        saveData(Items.RED_MUSHROOM, nature);
        saveData(Items.BROWN_MUSHROOM, nature);
    }
    
    private void saveTerrestrialBlocks(CompoundInstance earth) {
        saveData(Items.BLACKSTONE, earth);
        saveData(Items.COBBLESTONE, earth);
        saveData(Items.DIRT, earth);
        saveData(Items.FLINT, earth);
        saveData(Items.GRAVEL, earth);
        saveData(Items.RED_SAND, earth);
        saveData(Items.SAND, earth);
    }
    
    private void saveTallPlants(CompoundInstance nature) {
        
        saveData(Items.LARGE_FERN, nature);
        saveData(Items.TALL_GRASS, nature);
    }
    
    private void saveCoral(CompoundInstance nature, CompoundInstance water) {
        saveData(Items.BRAIN_CORAL, nature, water);
        saveData(Items.BUBBLE_CORAL, nature, water);
        saveData(Items.DEAD_BRAIN_CORAL, nature, water);
        saveData(Items.DEAD_BUBBLE_CORAL, nature, water);
        saveData(Items.DEAD_FIRE_CORAL, nature, water);
        saveData(Items.DEAD_HORN_CORAL, nature, water);
        saveData(Items.DEAD_TUBE_CORAL, nature, water);
        saveData(Items.FIRE_CORAL, nature, water);
        saveData(Items.HORN_CORAL, nature, water);
        saveData(Items.TUBE_CORAL, nature, water);
        saveData(Items.BRAIN_CORAL_FAN, nature, water);
        saveData(Items.BUBBLE_CORAL_FAN, nature, water);
        saveData(Items.DEAD_BRAIN_CORAL_FAN, nature, water);
        saveData(Items.DEAD_BUBBLE_CORAL_FAN, nature, water);
        saveData(Items.DEAD_FIRE_CORAL_FAN, nature, water);
        saveData(Items.DEAD_HORN_CORAL_FAN, nature, water);
        saveData(Items.DEAD_TUBE_CORAL_FAN, nature, water);
        saveData(Items.FIRE_CORAL_FAN, nature, water);
        saveData(Items.HORN_CORAL_FAN, nature, water);
        saveData(Items.TUBE_CORAL_FAN, nature, water);
    }
    
    private void saveCoralBlocks(CompoundInstance nature, CompoundInstance water) {
        saveData(Items.BRAIN_CORAL_BLOCK, nature, water);
        saveData(Items.BUBBLE_CORAL_BLOCK, nature, water);
        saveData(Items.DEAD_BRAIN_CORAL_BLOCK, nature, water);
        saveData(Items.DEAD_BUBBLE_CORAL_BLOCK, nature, water);
        saveData(Items.DEAD_FIRE_CORAL_BLOCK, nature, water);
        saveData(Items.DEAD_HORN_CORAL_BLOCK, nature, water);
        saveData(Items.DEAD_TUBE_CORAL_BLOCK, nature, water);
        saveData(Items.FIRE_CORAL_BLOCK, nature, water);
        saveData(Items.HORN_CORAL_BLOCK, nature, water);
        saveData(Items.TUBE_CORAL_BLOCK, nature, water);
    }
    
    private void saveDoubleFlowers(CompoundInstance nature) {
        saveData(Items.LILAC, nature);
        saveData(Items.PEONY, nature);
        saveData(Items.ROSE_BUSH, nature);
        saveData(Items.SUNFLOWER, nature);
    }
    
    private void saveFlowersAndDye(CompoundInstance nature) {
        
        saveData(Items.ALLIUM, nature);
        saveData(Items.AZURE_BLUET, nature);
        saveData(Items.BLUE_ORCHID, nature);
        saveData(Items.COCOA_BEANS, nature);
        saveData(Items.CORNFLOWER, nature);
        saveData(Items.DANDELION, nature);
        saveData(Items.LILY_OF_THE_VALLEY, nature);
        saveData(Items.ORANGE_TULIP, nature);
        saveData(Items.OXEYE_DAISY, nature);
        saveData(Items.PINK_TULIP, nature);
        saveData(Items.POPPY, nature);
        saveData(Items.RED_TULIP, nature);
        saveData(Items.WHITE_TULIP, nature);
        saveData(Items.BLACK_DYE, nature);
        saveData(Items.BLUE_DYE, nature);
        saveData(Items.BROWN_DYE, nature);
        saveData(Items.CYAN_DYE, nature);
        saveData(Items.GRAY_DYE, nature);
        saveData(Items.GREEN_DYE, nature);
        saveData(Items.LIGHT_BLUE_DYE, nature);
        saveData(Items.LIGHT_GRAY_DYE, nature);
        saveData(Items.LIME_DYE, nature);
        saveData(Items.MAGENTA_DYE, nature);
        saveData(Items.ORANGE_DYE, nature);
        saveData(Items.PINK_DYE, nature);
        saveData(Items.PURPLE_DYE, nature);
        saveData(Items.RED_DYE, nature);
        saveData(Items.WHITE_DYE, nature);
        saveData(Items.YELLOW_DYE, nature);
        
        // weird ones, not flowers, but still dyes
        saveData(Items.BEETROOT, nature);
        saveData(Items.CACTUS, nature);
        
        // weirder still, making wool equal to dyes
        saveData(Items.WHITE_WOOL, nature);
        saveData(Items.RED_WOOL, nature);
        saveData(Items.ORANGE_WOOL, nature);
        saveData(Items.YELLOW_WOOL, nature);
        saveData(Items.GREEN_WOOL, nature);
        saveData(Items.BLUE_WOOL, nature);
        saveData(Items.PURPLE_WOOL, nature);
        saveData(Items.PINK_WOOL, nature);
        saveData(Items.LIME_WOOL, nature);
        saveData(Items.CYAN_WOOL, nature);
        saveData(Items.GRAY_WOOL, nature);
        saveData(Items.LIGHT_GRAY_WOOL, nature);
        saveData(Items.BLACK_WOOL, nature);
        saveData(Items.BROWN_WOOL, nature);
        saveData(Items.LIGHT_BLUE_WOOL, nature);
        saveData(Items.MAGENTA_WOOL, nature);
    }
    
    private void saveLogsAndSaplings(CompoundInstance nature) {
        saveData(Items.ACACIA_LOG, nature);
        saveData(Items.BIRCH_LOG, nature);
        saveData(Items.DARK_OAK_LOG, nature);
        saveData(Items.JUNGLE_LOG, nature);
        saveData(Items.OAK_LOG, nature);
        saveData(Items.SPRUCE_LOG, nature);
        saveData(Items.CRIMSON_STEM, nature);
        saveData(Items.WARPED_STEM, nature);
        saveData(Items.STRIPPED_ACACIA_LOG, nature);
        saveData(Items.STRIPPED_BIRCH_LOG, nature);
        saveData(Items.STRIPPED_DARK_OAK_LOG, nature);
        saveData(Items.STRIPPED_JUNGLE_LOG, nature);
        saveData(Items.STRIPPED_OAK_LOG, nature);
        saveData(Items.STRIPPED_SPRUCE_LOG, nature);
        saveData(Items.STRIPPED_CRIMSON_STEM, nature);
        saveData(Items.STRIPPED_WARPED_STEM, nature);
        saveData(Items.ACACIA_SAPLING, nature);
        saveData(Items.BIRCH_SAPLING, nature);
        saveData(Items.DARK_OAK_SAPLING, nature);
        saveData(Items.JUNGLE_SAPLING, nature);
        saveData(Items.OAK_SAPLING, nature);
        saveData(Items.SPRUCE_SAPLING, nature);
    }
    
    private void saveLeavesAndSmallPlants(CompoundInstance nature) {
        saveData(Items.ACACIA_LEAVES, nature);
        saveData(Items.BIRCH_LEAVES, nature);
        saveData(Items.DARK_OAK_LEAVES, nature);
        saveData(Items.JUNGLE_LEAVES, nature);
        saveData(Items.OAK_LEAVES, nature);
        saveData(Items.SPRUCE_LEAVES, nature);
        
        saveData(Items.DEAD_BUSH, nature);
        saveData(Items.FERN, nature);
        saveData(Items.GRASS, nature);
        saveData(Items.CRIMSON_ROOTS, nature);
        saveData(Items.NETHER_SPROUTS, nature);
        saveData(Items.WARPED_ROOTS, nature);
        
        saveData(Items.KELP, nature);
        saveData(Items.LILY_PAD, nature);
        
        saveData(Items.SWEET_BERRIES, nature);
        saveData(Items.TWISTING_VINES, nature);
        saveData(Items.VINE, nature);
        saveData(Items.WEEPING_VINES, nature);
        saveData(Items.MUSHROOM_STEM, nature);
    }
    
    private void saveData(Item item, CompoundInstance... instances) {
        saveData(newLinkedHashSet(item, new ItemStack(item)), instances);
    }
    
    private LinkedHashSet<Object> newLinkedHashSet(final Object... internal) {
        return new LinkedHashSet<>(Arrays.asList(internal));
    }
    
    private void saveData(LinkedHashSet<Object> items, CompoundInstance... instances) {
        save(specFor(items).withCompounds(instances[0]));
    }
    
    private static CompoundInstance validation(double d) { return new CompoundInstance(Registry.VALIDATION.get(), d); }
    
}
