package com.kylenanakdewa.yaran.generators;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

/**
 * The most minimal chunk generator possible in Bukkit. Simply gets the chunk
 * data from the server, and passes it into the generator. Does not do any
 * processing.
 *
 * @author Kyle Nanakdewa
 */
public class MinimalChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return createChunkData(world);
    }

}