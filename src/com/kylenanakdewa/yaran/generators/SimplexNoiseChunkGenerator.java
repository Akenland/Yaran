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
     * The frequencies to use. At least one is required. More will increase terrain variation.
     */
    protected static List<Double> frequencies;
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

    public static void setParameters(ConfigurationSection configSection) {
        configSection = configSection.getConfigurationSection("simplex-noise");

        frequencies = configSection.getDoubleList("frequencies");
        exponent = configSection.getDouble("exponent");

        amplitude = configSection.getInt("amplitude");
        originHeight = configSection.getInt("origin-height");

        // enable3d = configSection.getBoolean("enable-3d");
        // cutoutThreshold = configSection.getDouble("cutout-threshold");
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
                    double unshiftedNoise = generator.noise(worldX * frequency, worldZ * frequency);
                    unshiftedNoise = (unshiftedNoise + 1) / 2; // Convert from -1 to 1 range, into 0 to 1 range
                    noise += (1 / frequency) * unshiftedNoise;
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

                /*if (enable3d) {
                    for (int y = 0; y < 255; y++) {
                        double noise3d = generator.noise(chunkX * 16 + x, y, chunkZ * 16 + z, frequency, amplitude,
                                true);

                        if (noise3d > cutoutThreshold) {
                            chunk.setBlock(x, y, z, Material.AIR);
                        }
                    }
                }*/
            }
        }

        return chunk;
    }

}