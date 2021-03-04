package com.kylenanakdewa.yaran.utils;

/**
 * Contains static functions used for various mathematical funtions in Yaran.
 *
 * @author Kyle Nanakdewa
 */
public final class YaranMath {
    private YaranMath() {
    }

    /**
     * Adjusts a value in the 0..1 range, using a sigmoid (s-shaped) function, to
     * smoothly push it closer to 0 or 1.
     * <p>
     * The steepness affects the range of returned values. It must be greater than
     * 2e in order to return values in the full range of 0..1, otherwise the range
     * will be smaller.
     *
     * @param x         the value to adjust, in the range 0..1 (inclusive)
     * @param steepness the intensity of the sigmoid function, higher values will
     *                  flatten values faster, and increase the steepness of the
     *                  sigmoid curve
     * @return the adjusted value, in the range 0..1 (inclusive)
     */
    public static double sigmoid(double x, double steepness) {
        double k = 2 * steepness * x - steepness;
        return 1 / (1 + Math.pow(Math.E, -k));
    }

    /**
     * A staircase version of the sigmoid function. Creates a smooth curve with
     * multiple levels.
     * <p>
     * Works for all values, not just in the 0..1 range.
     *
     * @param x         the value to adjust
     * @param steepness the intensity of the sigmoid function, higher values will
     *                  flatten values faster, and increase the steepness of the
     *                  sigmoid curve
     * @param scale     the relative scale of the sigmoid curve, compared to the
     *                  non-staircase version (1 will behave identically, in the
     *                  range 0..1)
     * @return the adjusted value
     */
    public static double staircaseSigmoid(double x, double steepness, double scale) {
        double xScaled = Math.pow(scale, -1) * x;
        double xScaledFloored = Math.floor(xScaled);
        return scale * (sigmoid(xScaled - xScaledFloored, steepness) + xScaledFloored);
    }

    /**
     * Rescales a value from the range oldMin..oldMax to the range newMin..newMax.
     * All min and max values are inclusive.
     * <p>
     * For example, rescaling the value 0.5 in the old range 0..1, to the new range
     * 50..100, will return 75.
     * <p>
     * To normalize values into the 0..1 range, use 0 and 1 as newMin and newMax,
     * respectively.
     *
     * @param x      the value to adjust
     * @param oldMin the original scale's minimum value, inclusive
     * @param oldMax the original scale's maximum value, inclusive
     * @param newMin the new scale's minimum value, inclusive
     * @param newMax the new scale's maximum value, inclusive
     * @return the adjusted value
     */
    public static double rescale(double x, double oldMin, double oldMax, double newMin, double newMax) {
        return (newMax - newMin) * (x - oldMin) / (oldMax - oldMin) + newMin;
    }

}