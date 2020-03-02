package com.kylenanakdewa.yaran.utils.imagemaps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

/**
 * Reads a 2D map stored in an image, and allows easy access to biome values in
 * the image.
 *
 * @author Kyle Nanakdewa
 */
public class BiomeImageMap extends ImageMap {

    /** The color mappings for biomes. */
    private static final Map<Color, Biome> BIOME_COLOR_MAPPINGS = loadColorMappings();

    public BiomeImageMap(File imageFile, int xOffset, int zOffset) {
        super(imageFile, xOffset, zOffset);
    }

    /**
     * Load the world-color mappings.
     */
    private static Map<Color, Biome> loadColorMappings() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Yaran");
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("biome-color-mappings");

        Map<Color, Biome> colorMappings = new HashMap<Color, Biome>();

        for (Map.Entry<String, Object> mapping : config.getValues(false).entrySet()) {
            Color color = Color.decode(mapping.getKey());

            String biomeName = mapping.getValue().toString().toUpperCase();
            Biome biome = Biome.valueOf(biomeName);

            colorMappings.put(color, biome);
        }

        return colorMappings;
    }

    /**
     * Gets the dye color of a specific pixel on the image. If the requested pixel
     * is out-of-bounds, returns black. If the requested pixel is not a dye color,
     * returns null.
     */
    public Biome getPixelBiome(int x, int y) {
        Color color = getPixelColor(x, y);

        return BIOME_COLOR_MAPPINGS.get(color);
    }

    /**
     * Gets the dye color of a specified pixel on the image, using game world X/Z
     * values. This will use the offset.
     */
    public Biome getPixelBiomeFromGame(int x, int z) {
        Color color = getPixelColorFromGame(x, z);

        return BIOME_COLOR_MAPPINGS.get(color);
    }

}