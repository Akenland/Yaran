package com.kylenanakdewa.yaran.generation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.noise.SimplexNoiseGenerator;

/**
 * Generates a heightmap using multiple Simplex Noise generators.
 *
 * @author Kyle Nanakdewa
 */
public class YaranHeightmapGenerator {

    /**
     * The noise generator for the continent map. Controls land vs water.
     */
    private final YaranNoiseGenerator continentMapGenerator;

    /**
     * The noise generator for the minimum terrain height. Controls land vs water,
     * as well as plateaus and overall altitude.
     */
    private final YaranNoiseGenerator minHeightGenerator;

    /**
     * The noise generator for the maximum terrain height. For example, this
     * controls where mountains and hills generate.
     */
    private final YaranNoiseGenerator maxHeightGenerator;

    /**
     * The noise generator for the final terrain height.
     */
    private final YaranNoiseGenerator finalHeightGenerator;

    public YaranHeightmapGenerator(long seed, ConfigurationSection finalHeightConfig,
            ConfigurationSection continentMapConfig, ConfigurationSection minHeightConfig,
            ConfigurationSection maxHeightConfig) {
        finalHeightGenerator = new YaranNoiseGenerator(finalHeightConfig, new SimplexNoiseGenerator(seed));

        long continentMapSeed = seed * "CONTINENT".hashCode();
        continentMapGenerator = new YaranNoiseGenerator(continentMapConfig,
                new SimplexNoiseGenerator(continentMapSeed));

        long minHeightSeed = seed * "MINIMUM".hashCode();
        minHeightGenerator = new YaranNoiseGenerator(minHeightConfig, new SimplexNoiseGenerator(minHeightSeed));

        long maxHeightSeed = seed * "MAXIMUM".hashCode();
        maxHeightGenerator = new YaranNoiseGenerator(maxHeightConfig, new SimplexNoiseGenerator(maxHeightSeed));
    }

    /**
     * Generates the terrain height for the specified coordinates.
     *
     * @param x the X coordinate to get the height at
     * @param z the Z coordinate to get the height at
     * @return the terrain Y height
     */
    public int getHeight(int x, int z) {
        return getHeightData(x, z).finalHeight;
    }

    /**
     * Generates the terrain height data for the specified coordinates.
     *
     * @param x the X coordinate to get the height data at
     * @param z the Z coordinate to get the height data at
     * @return the terrain minimum height, maximum height, and final height
     */
    public HeightData getHeightData(int x, int z) {
        //int seaFloor = continentMapGenerator.getScaledNoise(x, z, 45, 61);
        int minHeight = minHeightGenerator.getScaledNoise(x, z, 45, 128);
        int maxHeight = maxHeightGenerator.getScaledNoise(x, z, minHeight, 224);
        int finalHeight = finalHeightGenerator.getScaledNoise(x, z, minHeight, maxHeight);

        return new HeightData(minHeight, maxHeight, finalHeight);
    }

    /**
     * Contains the minimum height, maximum height, and final height of the terrain.
     */
    public class HeightData {
        public final int minHeight;
        public final int maxHeight;
        public final int finalHeight;

        private HeightData(int minHeight, int maxHeight, int finalHeight) {
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.finalHeight = finalHeight;
        }
    }

}