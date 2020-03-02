package com.kylenanakdewa.yaran.generators;

import java.io.File;
import java.util.List;
import java.util.Random;

import com.kylenanakdewa.yaran.utils.imagemaps.BiomeImageMap;
import com.kylenanakdewa.yaran.utils.imagemaps.DyeColorImageMap;
import com.kylenanakdewa.yaran.utils.imagemaps.GreyscaleImageMap;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.noise.SimplexNoiseGenerator;

/**
 * A chunk generator that uses the SimplexNoiseGenerator. The parameters for the
 * noise generator are taken from the plugin config.yml.
 * <p>
 * This creates ???
 *
 * @author Kyle Nanakdewa
 */
public class SimplexNoiseChunkGenerator extends ChunkGenerator {

    /**
     * The frequencies to use. At least one is required. More will increase terrain
     * variation.
     */
    protected static List<Double> frequencies;
    /**
     * The size of each frequency. Must be the same number of values as the list of
     * frequencies.
     */
    protected static List<Double> sizes;
    /**
     * The exponent to raise noise to.
     */
    protected static double exponent;

    /**
     * How far the terrain can be above the minimum height * total sizes * 4.
     */
    protected static int finalAmplitude;
    /**
     * The minimum terrain height. All terrain will be generated at or above this
     * level.
     */
    protected static int minimumHeight;

    /**
     * Whether to perform 3D cutouts, creating overhangs and large caves.
     */
    protected static boolean cutouts;
    /**
     * Threshold for cutting out terrain to create overhangs. Higher numbers = less
     * cutouts.
     */
    protected static double cutoutThreshold;
    /**
     * The frequencies to use. At least one is required. More will increase terrain
     * variation.
     */
    protected static List<Double> cutoutFrequencies;
    /**
     * The size of each frequency. Must be the same number of values as the list of
     * frequencies.
     */
    protected static List<Double> cutoutSizes;

    /**
     * The image map to use for altitude changes.
     */
    protected static GreyscaleImageMap altitudeMap;
    /**
     * The image map to use for minimum height changes.
     */
    protected static GreyscaleImageMap minHeightMap;
    /**
     * The image map to use for biomes.
     */
    protected static BiomeImageMap biomeMap;
    /**
     * The image map to use for wool colors.
     */
    protected static DyeColorImageMap woolMap;

    public static void setParameters(ConfigurationSection configSection) {
        configSection = configSection.getConfigurationSection("simplex-noise");

        frequencies = configSection.getDoubleList("frequencies");
        sizes = configSection.getDoubleList("sizes");
        exponent = configSection.getDouble("exponent");

        finalAmplitude = configSection.getInt("amplitude");
        minimumHeight = configSection.getInt("minimum-height");

        cutouts = configSection.getBoolean("cutouts");
        cutoutThreshold = configSection.getDouble("cutout-threshold");
        cutoutFrequencies = configSection.getDoubleList("cutout-frequencies");
        cutoutSizes = configSection.getDoubleList("cutout-sizes");

        if (configSection.contains("image-maps", true)) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Yaran");

            int xOffset = configSection.getInt("image-maps.offset.x", 0);
            int zOffset = configSection.getInt("image-maps.offset.z", 0);

            if (configSection.contains("image-maps.amplitude", true)) {
                String fileName = configSection.getString("image-maps.amplitude", "map_amplitude.png");
                altitudeMap = new GreyscaleImageMap(new File(plugin.getDataFolder(), fileName), xOffset, zOffset);
            }
            if (configSection.contains("image-maps.minimum-height", true)) {
                String fileName = configSection.getString("image-maps.minimum-height", "map_height.png");
                minHeightMap = new GreyscaleImageMap(new File(plugin.getDataFolder(), fileName), xOffset, zOffset);
            }
            if (configSection.contains("image-maps.wool", true)) {
                String fileName = configSection.getString("image-maps.wool", "map_wool.png");
                woolMap = new DyeColorImageMap(new File(plugin.getDataFolder(), fileName), xOffset, zOffset);
            }
            if (configSection.contains("image-maps.biomes", true)) {
                String fileName = configSection.getString("image-maps.biomes", "map_biomes.png");
                biomeMap = new BiomeImageMap(new File(plugin.getDataFolder(), fileName), xOffset, zOffset);
            }
        }
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        SimplexNoiseGenerator generator = new SimplexNoiseGenerator(world);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Calculate world co-ords, using chunk co-ords
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                // Use noise to calculate terrain height
                int height = getTerrainHeight(worldX, worldZ, generator);

                // Place blocks
                chunk = generateChunkBlocks(chunk, x, height, z);

                // 3D cutouts
                chunk = generate3dCutouts(chunk, generator, x, z, worldX, height, worldZ);

                // Set biomes
                biome = setBiomes(biome, x, z, worldX, worldZ);

                // Place biome-specific blocks
                chunk = setBiomeBlocks(chunk, biome, x, height, z);

                // Wool color map
                chunk = generateWoolOverlay(chunk, x, z, worldX, worldZ);
            }
        }

        return chunk;
    }

    /**
     * Gets the amplitude modifier for the specified world coordinates, using the
     * amplitude image map.
     * <p>
     * If no map is available, will return 1.
     */
    private double getTerrainAmplitudeModifier(int worldX, int worldZ) {
        if (altitudeMap != null) {
            double heightModifier = altitudeMap.getPixelGreyscaleFromGame(worldX, worldZ);
            return 0.75 * heightModifier + 0.75;
        } else
            return 1;
    }

    /**
     * Gets the minimum height modifier for the specified world coordinates, using
     * the amplitude image map.
     * <p>
     * If no map is available, will return 1.
     */
    private double getTerrainHeightModifier(int worldX, int worldZ) {
        if (minHeightMap != null) {
            double heightModifier = minHeightMap.getPixelGreyscaleFromGame(worldX, worldZ);
            return 0.4 * heightModifier + 0.8;
        } else
            return 1;
    }

    /**
     * Gets the terrain height for the specified world coordinates, using 2D simplex
     * noise.
     */
    private int getTerrainHeight(int worldX, int worldZ, SimplexNoiseGenerator generator) {
        // Get image map height multiplier
        double amplitudeModifier = getTerrainAmplitudeModifier(worldX, worldZ);

        // Generate noise at various frequencies (octaves)
        double noise = 0;
        for (double frequency : frequencies) {
            double size = sizes.get(frequencies.indexOf(frequency));

            // Generate noise in -1 to 1 range
            double singleNoise = generator.noise(worldX * frequency, worldZ * frequency);

            // Convert from -1 to 1 range, into 0 to 1 range
            singleNoise = (singleNoise + 1) / 2;

            // Adjust noise using amplitude modifier
            singleNoise = amplitudeModifier * size * singleNoise;

            // Add to total noise
            noise += singleNoise;
        }

        // Raise noise to a power (redistribution)
        noise = Math.pow(noise, exponent);

        // Use noise to calculate height
        int modifiedMinimumHeight = (int) (minimumHeight * getTerrainHeightModifier(worldX, worldZ));
        int height = (int) ((noise * finalAmplitude) + modifiedMinimumHeight);

        return height;
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
                //Material blockToPlace = (i > 247) ? Material.GOLD_BLOCK : Material.STONE;
                Material blockToPlace = Material.STONE;
                chunk.setBlock(x, i, z, blockToPlace);
            }
        } else if (height > 90) {
            for (int i = height; i > 0; i--) {
                Material blockToPlace = (i >= height - 3 && new Random().nextBoolean()) ? Material.GRAVEL
                        : Material.STONE;
                chunk.setBlock(x, i, z, blockToPlace);
            }
        } else if (height < 63) {
            for (int i = height; i > 0; i--) {
                //Material blockToPlace = (i > 43) ? Material.LAPIS_BLOCK : Material.STONE;
                Material blockToPlace = Material.STONE;
                if (i == 62)
                    blockToPlace = Material.SAND;
                chunk.setBlock(x, i, z, blockToPlace);
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

    /**
     * Generates 3D cutouts in a chunk, using 3D simplex noise. This will cut
     * existing blocks out of the chunk, creating overhangs and caves.
     */
    private ChunkData generate3dCutouts(ChunkData chunk, SimplexNoiseGenerator generator, int x, int z, int worldX,
            int height, int worldZ) {
        if (cutouts) {
            for (int y = 0; y <= height; y++) {
                // Generate noise at various frequencies (octaves)
                double cutoutNoise = 0;
                double totalSize = 0;
                for (double frequency : cutoutFrequencies) {
                    double size = cutoutSizes.get(cutoutFrequencies.indexOf(frequency))
                            * getTerrainAmplitudeModifier(worldX, worldZ);
                    totalSize += size;

                    double unshiftedNoise = generator.noise(worldX * frequency, y * frequency, worldZ * frequency);
                    unshiftedNoise = (unshiftedNoise + 1) / 2; // Convert from -1 to 1 range, into 0 to 1 range
                    cutoutNoise += size * unshiftedNoise;
                }
                cutoutNoise = cutoutNoise / totalSize;

                // Determine threshold for this location
                double heightPercentage = ((double) y / (double) height); // 0 = bedrock, 1 = surface
                // heightPercentage = Math.max(heightPercentage, 0.25); // Min threshold of 0.25

                if (cutoutNoise * cutoutThreshold <= heightPercentage) {
                    int modifiedMinimumHeight = (int) (minimumHeight * getTerrainHeightModifier(worldX, worldZ));
                    chunk.setBlock(x, y + (modifiedMinimumHeight), z, Material.AIR);
                }
            }
        }

        return chunk;
    }

    /**
     * Generates wool blocks, using a provided image map. The wool will generate at
     * the world height limit.
     */
    private ChunkData generateWoolOverlay(ChunkData chunk, int x, int z, int worldX, int worldZ) {
        if (woolMap != null) {
            DyeColor color = woolMap.getPixelDyeColorFromGame(worldX, worldZ);
            if (color != null)
                chunk.setBlock(x, 255, z, new Wool(color));
        }

        return chunk;
    }

    /**
     * Sets biomes, using a biome map image.
     */
    private BiomeGrid setBiomes(BiomeGrid biomes, int x, int z, int worldX, int worldZ) {
        if (biomeMap != null) {
            Biome biome = biomeMap.getPixelBiomeFromGame(worldX, worldZ);
            if (biome != null)
                for (int y = 0; y < 256; y++)
                    biomes.setBiome(x, y, z, biome);
        }

        return biomes;
    }

    /**
     * Sets the blocks, according to biomes.
     */
    private ChunkData setBiomeBlocks(ChunkData chunk, BiomeGrid biomes, int x, int height, int z) {
        // Change top 2-5 blocks
        int depth = new Random().nextInt(4) + 2;
        for (int i = height; i > height - depth; i--) {

            // Only place blocks if a block already exists
            if (!chunk.getBlockData(x, i, z).getMaterial().isAir()) {

                switch (biomes.getBiome(x, i, z)) {

                    case BEACH:
                    case SNOWY_BEACH:
                    case OCEAN:
                        // Fill water
                        if (height < 62) {
                            for (int iWater = height + 1; iWater <= 62; iWater++) {
                                chunk.setBlock(x, iWater, z, Material.WATER);
                            }
                        }

                        // If above 80, place stone or gravel
                        if (i > 80) {
                            Material blockToPlace = new Random().nextBoolean() ? Material.GRAVEL : Material.STONE;
                            chunk.setBlock(x, i, z, blockToPlace);
                        }
                        // If between 80 and 64, place grass/dirt
                        else if (i >= 64) {
                            Material blockToPlace = i == height ? Material.GRASS_BLOCK : Material.DIRT;
                            chunk.setBlock(x, i, z, blockToPlace);
                        }
                        // Otherwise, place sand
                        else {
                            chunk.setBlock(x, i, z, Material.SAND);
                        }
                        break;

                    case MOUNTAINS:
                    case MODIFIED_GRAVELLY_MOUNTAINS:
                    case GRAVELLY_MOUNTAINS:
                    case MOUNTAIN_EDGE:
                    case SNOWY_MOUNTAINS:
                    case SNOWY_TAIGA_MOUNTAINS:
                    case TAIGA_MOUNTAINS:
                    case WOODED_MOUNTAINS:
                    case STONE_SHORE:
                        // If above 245, place snow block
                        if (i > 245) {
                            chunk.setBlock(x, i, z, Material.SNOW_BLOCK);
                        }
                        // If above 200, or random chance if above 80, place stone or gravel
                        else if (i > 200 || (i > 80 && new Random().nextBoolean())) {
                            Material blockToPlace = new Random().nextBoolean() ? Material.GRAVEL : Material.STONE;
                            chunk.setBlock(x, i, z, blockToPlace);
                        }
                        // Otherwise place grass/dirt
                        else {
                            Material blockToPlace = i == height ? Material.GRASS_BLOCK : Material.DIRT;
                            chunk.setBlock(x, i, z, blockToPlace);
                        }
                        break;

                    default:
                        // If above 200, or random chance if above 85, place stone or gravel
                        if (i > 200 || (i > 85 && new Random().nextBoolean())) {
                            Material blockToPlace = new Random().nextBoolean() ? Material.GRAVEL : Material.STONE;
                            chunk.setBlock(x, i, z, blockToPlace);
                        } else {
                            // For all other biomes, grass and dirt
                            Material blockToPlace = i == height ? Material.GRASS_BLOCK : Material.DIRT;
                            chunk.setBlock(x, i, z, blockToPlace);
                        }
                        break;

                }

            }

        }

        // Bedrock floor
        chunk.setBlock(x, 0, z, Material.BEDROCK);

        return chunk;
    }

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