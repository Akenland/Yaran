package com.kylenanakdewa.yaran.generators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

/**
 * A chunk generator that uses the SimplexOctaveGenerator. The parameters for
 * the octave generator are taken from the plugin config.yml.
 * <p>
 * This creates ???
 *
 * @author Kyle Nanakdewa
 */
public class SimplexOctaveChunkGenerator extends ChunkGenerator {

    /**
     * Amount of octaves to create.
     */
    private static int octaves;
    /**
     * Sets the scale used for all coordinates passed to this generator. This is the
     * equivalent to setting each coordinate to the specified value.
     */
    private static double scale;

    /**
     * How much to alter the frequency by each octave.
     */
    private static double frequency;
    /**
     * How much to alter the amplitude by each octave.
     */
    private static double amplitude;
    /**
     * If true, normalize the value to [-1, 1].
     */
    private static boolean normalize;

    /**
     * The maximum world height above the origin height.
     */
    private static int maximumHeight;
    /**
     * The origin height - the lowest point of the world.
     */
    private static int originHeight;
    /**
     * The exponent to raise heights to.
     */
    private static double exponent;

    /**
     * Whether to enable the 3D simplex octave generator.
     */
    private static boolean enable3d;

    public static void setParameters(ConfigurationSection configSection) {
        configSection = configSection.getConfigurationSection("simplex-octave");

        octaves = configSection.getInt("octaves");
        scale = configSection.getDouble("scale");

        frequency = configSection.getDouble("frequency");
        amplitude = configSection.getDouble("amplitude");
        normalize = configSection.getBoolean("normalize");

        maximumHeight = configSection.getInt("maximum-height");
        originHeight = configSection.getInt("origin-height");
        exponent = configSection.getDouble("exponent");

        enable3d = configSection.getBoolean("enable-3d");
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(world, octaves);
        generator.setScale(scale);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double noise = generator.noise(chunkX * 16 + x, chunkZ * 16 + z, frequency, amplitude, normalize);
                int height = (int) (Math.pow(noise + 1, exponent) * maximumHeight + originHeight);

                // Place blocks
                chunk.setBlock(x, height, z, Material.GRASS_BLOCK);
                chunk.setBlock(x, height - 1, z, Material.DIRT);
                for (int i = height - 2; i > 0; i--)
                    chunk.setBlock(x, i, z, Material.STONE);
                chunk.setBlock(x, 0, z, Material.BEDROCK);

                if (enable3d) {
                    for (int y = 0; y < 255; y++) {
                        double noise3d = generator.noise(chunkX * 16 + x, y, chunkZ * 16 + z, frequency, amplitude,
                                true);

                        switch ((int) Math.round(noise3d)) {
                            case -1:
                                chunk.setBlock(x, y, z, Material.AIR);
                                break;
                            case 0:
                                break;
                            case 1:
                                chunk.setBlock(x, y, z, Material.STONE);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

        return chunk;
    }

}