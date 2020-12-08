package com.kylenanakdewa.yaran.worldedit;

import java.util.Map;
import java.util.Map.Entry;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.World;
import org.bukkit.util.noise.SimplexNoiseGenerator;

/**
 * A WorldEdit region generator that uses the SimplexNoiseGenerator. The
 * parameters for the noise generator are taken from the plugin config.yml, but
 * can be overriden via commands.
 *
 * @author Kyle Nanakdewa
 */
public class SimplexNoiseRegionGenerator {

    /**
     * The WorldEdit region to generate terrain in.
     */
    private final CuboidRegion region;

    /**
     * The frequencies to use, and their sizes. At least one is required. More will
     * increase terrain variation.
     */
    private Map<Double, Double> frequencies;

    /**
     * The exponent to raise noise to.
     */
    private double exponent;

    /**
     * How far the terrain can be above the minimum height * total sizes * 4.
     */
    private int finalAmplitude;

    /**
     * The minimum terrain height. All terrain will be generated at or above this
     * level.
     */
    private int minimumHeight;

    public SimplexNoiseRegionGenerator(CuboidRegion region, int amplitude, Map<Double, Double> frequencies,
            double exponent) {
        this.region = region;
        this.frequencies = frequencies;
        this.exponent = exponent;
        finalAmplitude = amplitude;
        minimumHeight = region.getMinimumY();
    }

    /**
     * Gets the terrain height for the specified world coordinates, using 2D simplex
     * noise.
     */
    private int getTerrainHeight(int worldX, int worldZ, SimplexNoiseGenerator generator) {
        // Generate noise at various frequencies (octaves)
        double noise = 0;
        for (Entry<Double, Double> entry : frequencies.entrySet()) {
            double frequency = entry.getKey();
            double size = entry.getValue();

            // Generate noise in -1 to 1 range
            double singleNoise = generator.noise(worldX * frequency, worldZ * frequency);

            // Convert from -1 to 1 range, into 0 to 1 range
            singleNoise = (singleNoise + 1) / 2;

            // Adjust noise using amplitude modifier
            singleNoise = size * singleNoise;

            // Add to total noise
            noise += singleNoise;
        }

        // Raise noise to a power (redistribution)
        noise = Math.pow(noise, exponent);

        // Use noise to calculate height
        int height = (int) ((noise * finalAmplitude) + minimumHeight);

        return height;
    }

    public int generate(EditSession session, Pattern pattern) throws MaxChangedBlocksException {
        int blocksChanged = 0;

        World world = BukkitAdapter.adapt(region.getWorld());
        SimplexNoiseGenerator generator = new SimplexNoiseGenerator(world);

        for (BlockVector2 column : region.asFlatRegion()) {
            int x = column.getX();
            int z = column.getZ();

            int height = getTerrainHeight(x, z, generator);

            BlockVector3 bottom = BlockVector3.at(x, minimumHeight, z);
            BlockVector3 top = BlockVector3.at(x, height, z);
            CuboidRegion columnRegion = new CuboidRegion(bottom, top);

            blocksChanged += session.setBlocks(columnRegion, pattern);
        }

        return blocksChanged;
    }

}