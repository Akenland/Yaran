package com.kylenanakdewa.yaran.generators;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
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
     * How far the terrain can be above/below the origin height.
     */
    protected static int amplitude;
    /**
     * The origin height. Terrain will be centered on this height.
     */
    protected static int originHeight;

    /**
     * Whether to perform 3D cutouts, creating overhangs and large caves.
     */
    protected static boolean cutouts;
    /**
     * Percentage of valid cutouts to perform. 1 = 100% of possible cutouts, 0 = no
     * cutouts.
     */
    protected static double cutoutThreshold;

    public static void setParameters(ConfigurationSection configSection) {
        configSection = configSection.getConfigurationSection("simplex-noise");

        frequencies = configSection.getDoubleList("frequencies");
        sizes = configSection.getDoubleList("sizes");
        exponent = configSection.getDouble("exponent");

        amplitude = configSection.getInt("amplitude");
        originHeight = configSection.getInt("origin-height");

        cutouts = configSection.getBoolean("cutouts");
        cutoutThreshold = configSection.getDouble("cutout-threshold");
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

                // Generate noise at various frequencies (octaves)
                double noise = 0;
                for (double frequency : frequencies) {
                    double size = sizes.get(frequencies.indexOf(frequency));

                    double unshiftedNoise = generator.noise(worldX * frequency, worldZ * frequency);
                    unshiftedNoise = (unshiftedNoise + 1) / 2; // Convert from -1 to 1 range, into 0 to 1 range
                    noise += size * unshiftedNoise;
                }

                // Raise noise to a power (redistribution)
                noise = Math.pow(noise, exponent);

                // Use noise to calculate terrain height
                int height = (int) ((noise * amplitude) + originHeight);

                // Place blocks
                chunk.setBlock(x, height, z, Material.GRASS_BLOCK);
                chunk.setBlock(x, height - 1, z, Material.DIRT);
                for (int i = height - 2; i > 0; i--)
                    chunk.setBlock(x, i, z, Material.STONE);
                chunk.setBlock(x, 0, z, Material.BEDROCK);

                // 3D cutouts
                if (cutouts) {
                    for (int y = 0; y <= height; y++) {
                        // Generate noise at various frequencies (octaves)
                        double cutoutNoise = 0;
                        double totalSize = 0;
                        for (double frequency : frequencies) {
                            double size = sizes.get(frequencies.indexOf(frequency));
                            totalSize += size;

                            double unshiftedNoise = generator.noise(worldX * frequency, y * frequency,
                                    worldZ * frequency);
                            unshiftedNoise = (unshiftedNoise + 1) / 2; // Convert from -1 to 1 range, into 0 to 1 range
                            cutoutNoise += size * unshiftedNoise;
                        }
                        cutoutNoise = cutoutNoise / totalSize;
                        // Raise noise to a power (redistribution)
                        cutoutNoise = Math.pow(cutoutNoise, exponent);

                        // Determine threshold for this location
                        double heightPercentage = ((double) y / (double) height); // 0 = bedrock, 1 = surface

                        if (cutoutNoise * cutoutThreshold <= heightPercentage) {
                            chunk.setBlock(x, y + originHeight, z, Material.AIR);
                            // chunk.setBlock(x, height, z, Material.GLASS);
                        }
                    }
                }
            }
        }

        return chunk;
    }

}