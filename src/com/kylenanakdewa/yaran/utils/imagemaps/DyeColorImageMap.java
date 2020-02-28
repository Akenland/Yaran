package com.kylenanakdewa.yaran.utils.imagemaps;

import java.io.File;

import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * Reads a 2D map stored in an image, and allows easy access to dye color values
 * in the image.
 *
 * @author Kyle Nanakdewa
 */
public class DyeColorImageMap extends ImageMap {

    public DyeColorImageMap(File imageFile, int xOffset, int zOffset) {
        super(imageFile, xOffset, zOffset);
    }

    /**
     * Converts a Java Color object to Bukkit's Color object.
     */
    private Color convertJavaColorToBukkitColor(java.awt.Color color) {
        return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Gets the dye color of a specific pixel on the image. If the requested pixel
     * is out-of-bounds, returns black. If the requested pixel is not a dye color,
     * returns null.
     */
    public DyeColor getPixelDyeColor(int x, int y) {
        java.awt.Color javaColor = getPixelColor(x, y);
        Color color = convertJavaColorToBukkitColor(javaColor);

        return DyeColor.getByColor(color);
    }

    /**
     * Gets the dye color of a specified pixel on the image, using game world
     * X/Z values. This will use the offset.
     */
    public DyeColor getPixelDyeColorFromGame(int x, int z) {
        java.awt.Color javaColor = getPixelColorFromGame(x, z);
        Color color = convertJavaColorToBukkitColor(javaColor);

        return DyeColor.getByColor(color);
    }

}