package com.kylenanakdewa.yaran.generators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.noise.SimplexOctaveGenerator;

/**
 * SimplexOctave3dChunkGenerator
 */
public class SimplexOctave3dChunkGenerator extends SimplexOctaveChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(world, octaves);
        generator.setScale(scale);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double noise2d = generator.noise(chunkX * 16 + x, chunkZ * 16 + z, frequency, amplitude, normalize);
                int height = (int) (Math.pow(noise2d + 1, exponent) * maximumHeight + originHeight);

                for (int y = 0; y < height; y++) {
                    double noise3d = generator.noise(chunkX * 16 + x, y, chunkZ * 16 + z, frequency, amplitude,
                            normalize);

                    double chance = noise3d + noise2d;

                    // Place blocks
                    chunk.setBlock(x, height, z, Material.GRASS_BLOCK);
                    chunk.setBlock(x, height - 1, z, Material.DIRT);
                    for (int i = height - 2; i > 0; i--)
                        chunk.setBlock(x, i, z, Material.STONE);
                    chunk.setBlock(x, 0, z, Material.BEDROCK);

                    if (chance < cutoutThreshold) {
                        chunk.setBlock(x, y, z, Material.AIR);
                    }
                }
            }
        }

        return chunk;
    }

}