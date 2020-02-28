package com.kylenanakdewa.yaran.utils.imagemaps;

import java.awt.Color;
import java.io.File;

/**
 * Reads a 2D map stored in an image, and allows easy access to greyscale values
 * in the image.
 *
 * @author Kyle Nanakdewa
 */
public class GreyscaleImageMap extends ImageMap {

    public GreyscaleImageMap(File imageFile, int xOffset, int zOffset) {
        super(imageFile, xOffset, zOffset);
    }

    /**
     * Converts a color to a greyscale value, in the range 0-1, where 0 is black,
     * and 1 is white. If the color is not greyscale, returns 0.
     */
    private double convertColorToGreyscale(Color color) {
        // Make sure color is greyscale
        if (color.equals(Color.BLACK) || color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
            return 0;
        }

        return color.getRed() / 255d;
    }

    /**
     * Gets the greyscale value of a specific pixel on the image, where 0 is black,
     * and 1 is white. If the requested pixel is out-of-bounds or is not greyscale,
     * returns 0 for black.
     */
    public double getPixelGreyscale(int x, int y) {
        Color color = getPixelColor(x, y);

        return convertColorToGreyscale(color);
    }

    /**
     * Gets the greyscale value of a specified pixel on the image, using game world
     * X/Z values. This will use the offset.
     */
    public double getPixelGreyscaleFromGame(int x, int z) {
        Color color = getPixelColorFromGame(x, z);

        return convertColorToGreyscale(color);
    }

}