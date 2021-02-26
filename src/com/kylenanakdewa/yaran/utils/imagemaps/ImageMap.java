package com.kylenanakdewa.yaran.utils.imagemaps;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.imageio.ImageIO;

import org.bukkit.Bukkit;

/**
 * Reads a 2D map stored in an image.
 *
 * @author Kyle Nanakdewa
 */
public class ImageMap {

    /** The image file to use. */
    private BufferedImage image;

    /** The X offset of the image, relative to the game world. */
    private int xOffset;
    /** The Z offset of the image, relative to the game world. */
    private int zOffset;

    /**
     * Creates a new blank image map.
     *
     * @param width   the total width of the image
     * @param height  the total height of the image
     * @param xOffset the X offset of the image, relative to the game world
     * @param zOffset the Z offset of the image, relative to the game world
     */
    public ImageMap(int width, int height, int xOffset, int zOffset) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    /**
     * Loads an existing image file.
     *
     * @param imageFile the image file to load
     * @param xOffset   the X offset of the image, relative to the game world
     * @param zOffset   the Z offset of the image, relative to the game world
     */
    public ImageMap(File imageFile, int xOffset, int zOffset) {
        loadImageFile(imageFile);
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    /**
     * Load the image file.
     */
    private void loadImageFile(File file) {
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to load map image file: " + e.getLocalizedMessage());
        }
    }

    /**
     * Saves the image to a file.
     *
     * @param file the file to save the image map to
     */
    public void saveImageFile(File file) {
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Unable to save map image file: " + e.getLocalizedMessage());
        }
    }

    /**
     * Gets the color of a specific pixel on the image. If the requested pixel is
     * out-of-bounds, returns black.
     */
    public Color getPixelColor(int x, int y) {
        if (x >= image.getWidth() || y >= image.getHeight() || x < 0 || y < 0) {
            return Color.BLACK;
        }

        return new Color(image.getRGB(x, y));
    }

    /**
     * Gets the color of a specified pixel on the image, using game world X/Z
     * values. This will use the offset.
     */
    public Color getPixelColorFromGame(int x, int z) {
        x -= xOffset;
        z -= zOffset;

        return getPixelColor(x, z);
    }

    /**
     * Sets the color of a specific pixel on the image. If the specified pixel is
     * out-of-bounds, does nothing.
     */
    public void setPixelColor(int x, int y, Color color) {
        if (x >= image.getWidth() || y >= image.getHeight() || x < 0 || y < 0) {
            return;
        }

        image.setRGB(x, y, color.getRGB());
    }

    /**
     * Sets the color of a specified pixel on the image, using game world X/Z
     * values. This will use the offset.
     */
    public void setPixelColorFromGame(int x, int z, Color color) {
        x -= xOffset;
        z -= zOffset;

        setPixelColor(x, z, color);
    }

    /**
     * Gets the equivalent game world co-ord for an image x/y pixel, using the
     * offset.
     */
    public int[] getGameLocFromPixel(int x, int y) {
        x += xOffset;
        int z = y + zOffset;

        return new int[] { x, z };
    }

}