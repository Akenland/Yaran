package com.kylenanakdewa.yaran.generators;

import java.awt.Color;
import java.io.File;
import java.util.Random;

import com.kylenanakdewa.yaran.generation.YaranHeightmapGenerator;
import com.kylenanakdewa.yaran.generation.YaranNoiseGenerator;
import com.kylenanakdewa.yaran.generation.YaranHeightmapGenerator.HeightData;
import com.kylenanakdewa.yaran.utils.YaranMath;
import com.kylenanakdewa.yaran.utils.imagemaps.ImageMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

/**
 * A chunk generator that uses the new Yaran generation algorithm. The
 * parameters for the noise generator are taken from the plugin config.yml.
 *
 * @author Kyle Nanakdewa
 */
public class YaranChunkGenerator extends ChunkGenerator {

    /**
     * When debug mode is on, the generator internal functions will be visualized,
     * instead of generating actual terrain.
     */
    private static boolean debugMode;

    /**
     * Enables the new Yaran continent generator, which generates more realistic and
     * tunable continents and oceans, by splitting up land vs. ocean functions.
     */
    private static boolean useContinentGenerator;

    /**
     * The noise generator settings for generating the final terrain height.
     */
    private static ConfigurationSection finalHeightConfig;

    /**
     * The noise generator settings for generating the continent map. This is used
     * to determine land vs water.
     */
    private static ConfigurationSection continentMapConfig;

    /**
     * The noise generator settings for generating the minimum heightmap. This
     * controls the lowest point of terrain, and is used to determine land vs water,
     * as well as plateaus and overall altitude.
     */
    private static ConfigurationSection minHeightConfig;

    /**
     * The noise generator settings for generating the maximum heightmap. This
     * controls the highest point of terrain, and is used to determine where
     * mountains and hills can generate.
     */
    private static ConfigurationSection maxHeightConfig;

    /**
     * The noise generator settings for generating the temperature map.
     */
    private static ConfigurationSection temperatureMapConfig;
    /**
     * The noise generator settings for generating the flying hills.
     */
    private static ConfigurationSection flyingHillsConfig;

    /**
     * Whether to perform 3D cutouts, creating overhangs and large caves.
     */
    // protected static boolean cutouts;
    /**
     * Threshold for cutting out terrain to create overhangs. Higher numbers = less
     * cutouts.
     */
    // protected static double cutoutThreshold;
    /**
     * The frequencies to use. At least one is required. More will increase terrain
     * variation.
     */
    // protected static List<Double> cutoutFrequencies;
    /**
     * The size of each frequency. Must be the same number of values as the list of
     * frequencies.
     */
    // protected static List<Double> cutoutSizes;

    /**
     * The image map to use for altitude changes.
     */
    // protected static GreyscaleImageMap altitudeMap;
    /**
     * The image map to use for minimum height changes.
     */
    // protected static GreyscaleImageMap minHeightMap;
    /**
     * The image map to use for biomes.
     */

    // protected static BiomeImageMap biomeMap;
    /**
     * The image map to use for wool colors.
     */
    // protected static DyeColorImageMap woolMap;

    /**
     * When debug maps are on, the generator internal functions will be visualized
     * on image maps.
     */
    private static boolean drawDebugMaps;

    private static ImageMap maxHeightMap;
    private static ImageMap finalHeightMap;
    private static ImageMap minHeightMap;
    private static ImageMap continentMap;
    private static ImageMap flyingHillsMap;

    public static void setParameters(ConfigurationSection configSection) {
        configSection = configSection.getConfigurationSection("yaran-new");

        debugMode = configSection.getBoolean("debug");

        useContinentGenerator = configSection.getBoolean("use-continent-generator");

        finalHeightConfig = configSection.getConfigurationSection("final-height");
        continentMapConfig = configSection.getConfigurationSection("continent-map");
        minHeightConfig = configSection.getConfigurationSection("min-height");
        maxHeightConfig = configSection.getConfigurationSection("max-height");
        temperatureMapConfig = configSection.getConfigurationSection("temperature-map");
        flyingHillsConfig = configSection.getConfigurationSection("flying-hills");

        // cutouts = configSection.getBoolean("cutouts");
        // cutoutThreshold = configSection.getDouble("cutout-threshold");
        // cutoutFrequencies = configSection.getDoubleList("cutout-frequencies");
        // cutoutSizes = configSection.getDoubleList("cutout-sizes");

        if (configSection.contains("image-maps", true)) {
            int xOffset = configSection.getInt("image-maps.offset.x", 0);
            int zOffset = configSection.getInt("image-maps.offset.z", 0);

            /*
             * if (configSection.contains("image-maps.amplitude", true)) { String fileName =
             * configSection.getString("image-maps.amplitude", "map_amplitude.png");
             * altitudeMap = new GreyscaleImageMap(new File(plugin.getDataFolder(),
             * fileName), xOffset, zOffset); } if
             * (configSection.contains("image-maps.minimum-height", true)) { String fileName
             * = configSection.getString("image-maps.minimum-height", "map_height.png");
             * minHeightMap = new GreyscaleImageMap(new File(plugin.getDataFolder(),
             * fileName), xOffset, zOffset); } if (configSection.contains("image-maps.wool",
             * true)) { String fileName = configSection.getString("image-maps.wool",
             * "map_wool.png"); woolMap = new DyeColorImageMap(new
             * File(plugin.getDataFolder(), fileName), xOffset, zOffset); } if
             * (configSection.contains("image-maps.biomes", true)) { String fileName =
             * configSection.getString("image-maps.biomes", "map_biomes.png"); biomeMap =
             * new BiomeImageMap(new File(plugin.getDataFolder(), fileName), xOffset,
             * zOffset); }
             */

            drawDebugMaps = configSection.getBoolean("image-maps.draw-debug-maps");
            if (drawDebugMaps) {
                int width = configSection.getInt("image-maps.width");
                int height = configSection.getInt("image-maps.height");

                maxHeightMap = new ImageMap(width, height, xOffset, zOffset);
                finalHeightMap = new ImageMap(width, height, xOffset, zOffset);
                minHeightMap = new ImageMap(width, height, xOffset, zOffset);
                continentMap = new ImageMap(width, height, xOffset, zOffset);
                flyingHillsMap = new ImageMap(width, height, xOffset, zOffset);
            }
        }
    }

    /**
     * If debug maps are enabled, saves the resulting maps to the specified folder.
     *
     * Note that the maps will only contain data after the world has been generated.
     *
     * @param folder the folder to save the image files to
     */
    public static void saveDebugMaps(File folder) {
        maxHeightMap.saveImageFile(new File(folder, "debug_map_max_height.png"));
        finalHeightMap.saveImageFile(new File(folder, "debug_map_final_height.png"));
        minHeightMap.saveImageFile(new File(folder, "debug_map_min_height.png"));
        continentMap.saveImageFile(new File(folder, "debug_map_continent.png"));
        flyingHillsMap.saveImageFile(new File(folder, "debug_map_flying_hills.png"));
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        YaranHeightmapGenerator generator = new YaranHeightmapGenerator(world.getSeed(), finalHeightConfig,
                continentMapConfig, minHeightConfig, maxHeightConfig);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Calculate world co-ords, using chunk co-ords
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                // Use noise to calculate terrain height
                HeightData heightData = generator.getHeightData(worldX, worldZ);

                // Debug mode - show min height, max height, and final height
                if (debugMode) {
                    chunk.setBlock(x, heightData.maxHeight, z, Material.GLASS);
                    chunk.setBlock(x, heightData.finalHeight, z, Material.GRASS_BLOCK);
                    chunk.setBlock(x, heightData.minHeight, z, Material.STONE);
                    chunk.setBlock(x, 0, z, Material.BEDROCK);
                }

                // Place blocks
                else {
                    chunk = generateChunkBlocks(world.getSeed(), chunk, biome, x, z, worldX, worldZ, heightData);
                }

                // Debug mode - draw min height, max height, and final height to image maps
                /*
                 * if (drawDebugMaps) { maxHeightMap.setPixelColorFromGame(worldX, worldZ, new
                 * Color(0, 0, heightData.maxHeight));
                 * finalHeightMap.setPixelColorFromGame(worldX, worldZ, new Color(0,
                 * heightData.finalHeight, 0)); minHeightMap.setPixelColorFromGame(worldX,
                 * worldZ, new Color(heightData.minHeight, 0, 0));
                 *
                 * float continentColorValue = (float) heightData.continentNoise;
                 * continentMap.setPixelColorFromGame(worldX, worldZ, new
                 * Color(continentColorValue, continentColorValue, continentColorValue)); }
                 */

                // 3D cutouts
                // chunk = generate3dCutouts(chunk, generator, x, z, worldX, height, worldZ);

                // Set biomes
                // biome = setBiomes(biome, x, z, worldX, worldZ);

                // Place biome-specific blocks
                // chunk = setBiomeBlocks(chunk, biome, x, height, z);

                // Wool color map
                // chunk = generateWoolOverlay(chunk, x, z, worldX, worldZ);
            }
        }

        return chunk;
    }

    /**
     * Generates the blocks in a chunk, using chunk X and Z values, and the terrain
     * height.
     * <p>
     * This only generates using the height provided - overhangs and other cutouts
     * are ignored.
     */
    private ChunkData generateChunkBlocks(ChunkData chunk, int x, int height, int z) {
        if (height > 247) {
            for (int i = height; i > 0; i--) {
                // Material blockToPlace = (i > 247) ? Material.GOLD_BLOCK : Material.STONE;
                Material blockToPlace = Material.STONE;
                chunk.setBlock(x, i, z, blockToPlace);
            }
        } else if (height > 90) {
            for (int i = height; i > 0; i--) {
                Material blockToPlace = (i >= height - 3 && new Random().nextBoolean()) ? Material.DIRT
                        : new Random().nextBoolean() ? Material.STONE : Material.GRAVEL;
                chunk.setBlock(x, i, z, blockToPlace);
            }
        } else if (height < 63) {
            for (int i = height; i > 0; i--) {
                // Material blockToPlace = (i > 43) ? Material.LAPIS_BLOCK : Material.STONE;
                Material blockToPlace = Material.STONE;
                if (i == 62)
                    blockToPlace = Material.SAND;
                chunk.setBlock(x, i, z, blockToPlace);
            }
            for (int i = 62; i > height; i--) {
                chunk.setBlock(x, i, z, Material.WATER);
            }
        } else {
            chunk.setBlock(x, height, z, Material.GRASS_BLOCK);
            chunk.setBlock(x, height - 1, z, Material.DIRT);
            for (int i = height - 2; i > 0; i--)
                chunk.setBlock(x, i, z, Material.STONE);
        }
        chunk.setBlock(x, 0, z, Material.BEDROCK);

        return chunk;
    }

    private ChunkData generateChunkBlocks(long seed, ChunkData chunk, BiomeGrid biome, int x, int z, int worldX,
            int worldZ, HeightData heightData) {

        //// TEMPERATURE
        // Currently designed around cold maps
        long temperatureSeed = seed * "TEMPERATURE".hashCode();
        YaranNoiseGenerator temperatureMap = new YaranNoiseGenerator(temperatureMapConfig,
                new SimplexNoiseGenerator(temperatureSeed));
        double temperature = Math.max(temperatureMap.getNoise(worldX, worldZ), 0.6);

        //// HEIGHT VALUES
        int minHeight = heightData.minHeight;
        int maxHeight = heightData.maxHeight;
        int finalHeight = heightData.finalHeight;

        // Continent generator - Updated height values - Water level relative
        if (useContinentGenerator) {

            // Whether continent should generate: +1 for land, 0 for coastline, -1 for ocean
            double continentValue = YaranMath.rescale(heightData.continentNoise, 0, 1, -1, 1);
            double absoluteContinentValue = Math.abs(continentValue);

            // Minimum terrain height, y-value, start at water level
            minHeight = 61;

            // Pull minimum height towards the water level when near a coastline
            double minHeightNoise = heightData.minHeightNoise;
            if (absoluteContinentValue < 0.5) {
                // When value is 0.5, exp is 1
                // When value is 0, exp is 3.25
                double exponent = 3.25 - (absoluteContinentValue * 4.5);
                minHeightNoise = Math.pow(minHeightNoise, exponent);
            }

            // Pull maximum height towards the water level when near a coastline
            double maxHeightNoise = heightData.maxHeightNoise;
            //maxHeightNoise *= absoluteContinentValue;
            if (absoluteContinentValue < 1) {
                // When value is 0.5, exp is 1
                // When value is 0, exp is 10
                // double exponent = 10 - (absoluteContinentValue * 18);
                // maxHeightNoise = Math.pow(maxHeightNoise, exponent);

                // Sigmoid attempt
                maxHeightNoise *= YaranMath.sigmoid(absoluteContinentValue, 10);
            }

            // Adjust final noise based on continent value
            double finalHeightNoise = heightData.finalHeightNoise;
            // finalHeightNoise *= absoluteContinentValue;

            // If land
            if (continentValue > 0) {
                // Terrain min height will be between y62 (0) and y128 (+66)
                int minHeightAboveWater = YaranMath.rescaleToInt(minHeightNoise, 0, 1, 0, 66);
                minHeight += minHeightAboveWater;

                // Terrain max height will at most y224, 32 below world height limit
                maxHeight = YaranMath.rescaleToInt(maxHeightNoise, 0, 1, minHeight, 224);
            }
            // If ocean
            else {
                // Terrain min height will be between y62 (-0) and y32 (-30)
                int minHeightBelowWater = YaranMath.rescaleToInt(minHeightNoise, 0, 1, 0, 30);
                minHeight -= minHeightBelowWater;

                // Terrain max height will be y64, just above water level (allows islands)
                maxHeight = YaranMath.rescaleToInt(maxHeightNoise, 0, 1, minHeight, 64);
            }
            // Final terrain height
            finalHeight = YaranMath.rescaleToInt(finalHeightNoise, 0, 1, minHeight, maxHeight);
        }

        int heightDifference = finalHeight - minHeight;

        // Debug mode - draw min height, max height, and final height to image maps
        if (drawDebugMaps) {
            maxHeightMap.setPixelColorFromGame(worldX, worldZ, new Color(0, 0, maxHeight));
            finalHeightMap.setPixelColorFromGame(worldX, worldZ, new Color(0, finalHeight, 0));
            minHeightMap.setPixelColorFromGame(worldX, worldZ, new Color(minHeight, 0, 0));

            float continentColorValue = (float) heightData.continentNoise;
            continentMap.setPixelColorFromGame(worldX, worldZ,
                    new Color(continentColorValue, continentColorValue, continentColorValue));
        }

        //// TERRAIN
        // Water
        if (finalHeight <= 63) {
            // Beach
            if (finalHeight >= 60) {
                // Set beach biome
                Biome beachBiome = temperature > 0.5 ? Biome.BEACH : Biome.SNOWY_BEACH;
                for (int y = 0; y < 256; y++) {
                    biome.setBiome(x, y, z, beachBiome);
                }

                // Sand on beaches
                for (int y = finalHeight; y > finalHeight - 4; y--) {
                    chunk.setBlock(x, y, z, Material.SAND);
                }

            }

            // Ocean
            else {
                // Set ocean biome
                Biome oceanBiome = temperature > 0.7 ? Biome.OCEAN
                        : temperature > 0.3 ? Biome.COLD_OCEAN : Biome.FROZEN_OCEAN;
                for (int y = 0; y < 256; y++) {
                    biome.setBiome(x, y, z, oceanBiome);
                }

                // Gravel seabed
                for (int y = finalHeight; y > finalHeight - 4; y--) {
                    chunk.setBlock(x, y, z, Material.GRAVEL);
                }
            }

            // Stone below
            for (int y = finalHeight - 4; y > 0; y--) {
                chunk.setBlock(x, y, z, Material.STONE);
            }

            // Water
            for (int y = 62; y > finalHeight; y--) {
                chunk.setBlock(x, y, z, Material.WATER);
            }
        }

        // Flat land
        else if (heightDifference < 10) {
            // Set land biome
            Biome landBiome;
            if (temperature > 0.75) {
                landBiome = Biome.GIANT_SPRUCE_TAIGA;
            } else if (temperature > 0.5) {
                landBiome = Biome.TAIGA;
            } else if (temperature > 0.25) {
                landBiome = Biome.SNOWY_TAIGA;
            } else {
                landBiome = Biome.SNOWY_TUNDRA;
            }
            for (int y = 0; y < 256; y++) {
                biome.setBiome(x, y, z, landBiome);
            }

            // Grass
            chunk.setBlock(x, finalHeight, z, Material.GRASS_BLOCK);

            // Dirt
            for (int y = finalHeight - 1; y > finalHeight - 4; y--) {
                chunk.setBlock(x, y, z, Material.DIRT);
            }

            // Stone
            for (int y = finalHeight - 4; y > 0; y--) {
                chunk.setBlock(x, y, z, Material.STONE);
            }
        }

        // Mountains
        else {
            // Set mountain biome
            Biome mountainBiome;
            if (temperature > 0.75) {
                mountainBiome = Biome.MOUNTAINS;
            } else if (temperature > 0.5) {
                mountainBiome = Biome.WOODED_MOUNTAINS;
            } else if (temperature > 0.25) {
                mountainBiome = Biome.SNOWY_TAIGA_MOUNTAINS;
            } else {
                mountainBiome = Biome.SNOWY_MOUNTAINS;
            }
            for (int y = 0; y < 256; y++) {
                biome.setBiome(x, y, z, mountainBiome);
            }

            // Top ground cover
            for (int y = finalHeight; y > finalHeight - 4; y--) {
                // Random chance
                Random random = new Random(seed * worldX * worldZ);
                double randomValue = random.nextDouble();
                Material blockToPlace;
                // 50% chance of grass/dirt, 25% chance of gravel, 25% chance of stone
                if (randomValue > 0.5) {
                    blockToPlace = (y == finalHeight) ? Material.GRASS_BLOCK : Material.DIRT;
                } else if (randomValue > 0.25) {
                    blockToPlace = Material.GRAVEL;
                } else {
                    blockToPlace = Material.STONE;
                }

                chunk.setBlock(x, y, z, blockToPlace);
            }

            // Stone
            for (int y = finalHeight - 4; y > 0; y--) {
                chunk.setBlock(x, y, z, Material.STONE);
            }
        }

        //// OTHER FEATURES
        // Flying Hills
        if (flyingHillsConfig.getBoolean("enabled")) {
            long flyingHillsSeed = seed * "FLYING_HILLS".hashCode();
            YaranNoiseGenerator flyingHillsGenerator = new YaranNoiseGenerator(flyingHillsConfig,
                    new SimplexNoiseGenerator(flyingHillsSeed));
            double flyingHillsNoise = flyingHillsGenerator.getNoise(worldX, worldZ);
            if (drawDebugMaps) {
                float colorValue = (float) flyingHillsNoise;
                flyingHillsMap.setPixelColorFromGame(worldX, worldZ, new Color(colorValue, colorValue, colorValue));
            }
            for (int y = finalHeight; y < finalHeight + 50; y++) {
                double yPercentage = YaranMath.rescale(y, finalHeight, finalHeight + 50, 0, 1);
                // double threshold = 0.5 + 0.5 * Math.pow(yPercentage, 2);
                double l = 1.25 * yPercentage - 1;
                double threshold = 2 * (0.25 + Math.pow(l, 3) + Math.pow(l, 2));

                if (flyingHillsNoise > threshold) {
                    if (y < finalHeight + 48) {
                        chunk.setBlock(x, y, z, Material.STONE);
                    } else if (y < finalHeight + 50) {
                        chunk.setBlock(x, y, z, Material.DIRT);
                    } else {
                        chunk.setBlock(x, y, z, Material.GRASS_BLOCK);
                    }
                }
            }
        }

        // Bedrock floor
        chunk.setBlock(x, 0, z, Material.BEDROCK);

        return chunk;
    }

    /**
     * Sets the blocks, according to biomes.
     */
    /*
     * private ChunkData setBiomeBlocks(ChunkData chunk, BiomeGrid biomes, int x,
     * int height, int z) { // Change top 2-5 blocks int depth = new
     * Random().nextInt(4) + 2; for (int i = height; i > height - depth; i--) {
     *
     * // Only place blocks if a block already exists if (!chunk.getBlockData(x, i,
     * z).getMaterial().isAir()) {
     *
     * switch (biomes.getBiome(x, i, z)) {
     *
     * case BEACH: case SNOWY_BEACH: case OCEAN: // Fill water if (height < 62) {
     * for (int iWater = height + 1; iWater <= 62; iWater++) { chunk.setBlock(x,
     * iWater, z, Material.WATER); } }
     *
     * // If above 80, place stone or gravel if (i > 80) { Material blockToPlace =
     * new Random().nextBoolean() ? Material.GRAVEL : Material.STONE;
     * chunk.setBlock(x, i, z, blockToPlace); } // If between 80 and 64, place
     * grass/dirt else if (i >= 64) { Material blockToPlace = i == height ?
     * Material.GRASS_BLOCK : Material.DIRT; chunk.setBlock(x, i, z, blockToPlace);
     * } // Otherwise, place sand else { chunk.setBlock(x, i, z, Material.SAND); }
     * break;
     *
     * case STONE_SHORE: // Fill water if (height < 62) { for (int iWater = height +
     * 1; iWater <= 62; iWater++) { chunk.setBlock(x, iWater, z, Material.WATER); }
     * } // Place stone or gravel if (i > 0) { Material blockToPlace = new
     * Random().nextBoolean() ? Material.GRAVEL : Material.STONE; chunk.setBlock(x,
     * i, z, blockToPlace); } break;
     *
     * case MOUNTAINS: case MODIFIED_GRAVELLY_MOUNTAINS: case GRAVELLY_MOUNTAINS:
     * case MOUNTAIN_EDGE: case SNOWY_MOUNTAINS: case SNOWY_TAIGA_MOUNTAINS: case
     * TAIGA_MOUNTAINS: case WOODED_MOUNTAINS: // If above 245, place snow block if
     * (i > 245) { chunk.setBlock(x, i, z, Material.SNOW_BLOCK); } // If above 200,
     * or random chance if above 80, place stone or gravel else if (i > 200 || (i >
     * 80 && new Random().nextBoolean())) { Material blockToPlace = new
     * Random().nextBoolean() ? Material.GRAVEL : Material.STONE; chunk.setBlock(x,
     * i, z, blockToPlace); } // Otherwise place grass/dirt else { Material
     * blockToPlace = i == height ? Material.GRASS_BLOCK : Material.DIRT;
     * chunk.setBlock(x, i, z, blockToPlace); } break;
     *
     * default: // If above 200, or random chance if above 85, place stone or gravel
     * if (i > 200 || (i > 85 && new Random().nextBoolean())) { Material
     * blockToPlace = new Random().nextBoolean() ? Material.GRAVEL : Material.STONE;
     * chunk.setBlock(x, i, z, blockToPlace); } else { // For all other biomes,
     * grass and dirt Material blockToPlace = i == height ? Material.GRASS_BLOCK :
     * Material.DIRT; chunk.setBlock(x, i, z, blockToPlace); } break;
     *
     * }
     *
     * }
     *
     * }
     *
     * // Bedrock floor chunk.setBlock(x, 0, z, Material.BEDROCK);
     *
     * return chunk; }
     */

    @Override
    public boolean shouldGenerateCaves() {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

}