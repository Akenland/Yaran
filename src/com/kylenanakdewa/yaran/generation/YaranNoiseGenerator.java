package com.kylenanakdewa.yaran.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.kylenanakdewa.yaran.utils.YaranMath;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.noise.NoiseGenerator;

/**
 * Generates noise in multiple frequencies.
 *
 * @author Kyle Nanakdewa
 */
public class YaranNoiseGenerator {

    /**
     * The noise generator to use.
     */
    private final NoiseGenerator generator;

    /**
     * The frequencies to use, and their sizes. At least one is required. More will
     * increase terrain variation.
     */
    private final Map<Double, Double> frequencies;

    /**
     * The exponent to raise noise to.
     */
    private final double exponent;

    /**
     * The sigmoid multiplier. Higher valleys make the curve steeper, and the peaks
     * and valleys flatter. Set to 0 to disable.
     */
    private final double sigmoidMultiplier;

    /**
     * The sigmoid scale. Creates multiple levels of plateaus when set to a number
     * in the range 0..1. Set to 0 or 1 to use default scale.
     */
    private final double sigmoidScale;

    /**
     * Creates a noise generator with the specified settings and the specified noise
     * generator algorithm.
     *
     * @param frequencies       a map of frequencies and their relative size/weight
     * @param exponent          the exponent to raise noise to, higher values will
     *                          create flatter valleys and steeper cliffs
     * @param sigmoidMultiplier the sigmoid function multiplier, higher values will
     *                          create flatter peaks and valleys, and steeper
     *                          cliffs, use 0 to disable
     * @param generator         the noise generator algorithm to use, typically
     *                          Simplex Noise
     */
    public YaranNoiseGenerator(Map<Double, Double> frequencies, double exponent, double sigmoidMultiplier,
            NoiseGenerator generator) {
        this.frequencies = frequencies;
        this.exponent = exponent;
        this.sigmoidMultiplier = sigmoidMultiplier;
        this.sigmoidScale = 0;
        this.generator = generator;
    }

    /**
     * Creates a noise generator with the settings specified in a config file and
     * the specified noise generator algorithm.
     *
     * @param config    the configuration section containing the settings for this
     *                  generator
     * @param generator the noise generator algorithm to use, typically Simplex
     *                  Noise
     */
    public YaranNoiseGenerator(ConfigurationSection config, NoiseGenerator generator) {
        // Frequencies
        frequencies = new HashMap<Double, Double>();
        for (String entry : config.getStringList("frequencies")) {
            double frequency = Double.parseDouble(entry.split(":")[0]);
            double size = Double.parseDouble(entry.split(":")[1]);
            frequencies.put(frequency, size);
        }

        // Exponent
        exponent = config.getDouble("exponent", 1);

        // Sigmoid multiplier
        sigmoidMultiplier = config.getDouble("sigmoid-multiplier", 0);
        // Sigmoid scale
        sigmoidScale = config.getDouble("sigmoid-scale", 0);

        this.generator = generator;
    }

    /**
     * Creates a noise generator with a single frequency and the specified noise
     * generator algorithm.
     *
     * @param frequency the frequency to use for noise generation
     * @param generator the noise generator algorithm to use, typically Simplex
     *                  Noise
     */
    public YaranNoiseGenerator(double frequency, double exponent, double sigmoidMultiplier, NoiseGenerator generator) {
        this(new HashMap<Double, Double>(), exponent, sigmoidMultiplier, generator);
        frequencies.put(frequency, 1d);
    }

    /**
     * Generates 2D noise for the specified coordinates, in the range 0..1.
     *
     * @param x the X coordinate to generate noise at
     * @param z the Z coordinate to generate noise at
     * @return resulting noise at given location, in the range 0..1
     */
    public double getNoise(int x, int z) {
        double noise = 0;

        // Generate multiple layers of noise, in various frequencies
        double totalSize = 0;
        for (Entry<Double, Double> entry : frequencies.entrySet()) {
            double frequency = entry.getKey();
            double size = entry.getValue();

            // Generate a single noise layer
            double noiseLayer = generator.noise(x * frequency, z * frequency);

            // Noise function generates in -1..1 range, need to convert to 0..1 range
            noiseLayer = (noiseLayer + 1) / 2;

            // Adjust the relative size/weight of this layer
            noiseLayer = size * noiseLayer;

            // Add to total noise (this will be outside 0..1 range for now)
            noise += noiseLayer;

            // Calculate the total size, so it can be adjusted back to 0..1 range
            totalSize += size;
        }

        // Divide noise by sizes, to get back to 0..1 range
        noise /= totalSize;

        // Raise noise to a power (redistribution)
        noise = Math.pow(noise, exponent);

        // Sigmoid function to create plateaus
        if (sigmoidMultiplier != 0) {
            if (sigmoidScale != 0 && sigmoidScale != 1) {
                noise = YaranMath.staircaseSigmoid(noise, sigmoidMultiplier, sigmoidScale);
            } else {
                double k = 2 * sigmoidMultiplier * noise - sigmoidMultiplier;
                noise = 1 / (1 + Math.pow(Math.E, -k));
            }
        }

        return noise;
    }

    /**
     * Generates 2D noise for the specified coordinates, in the provided range.
     *
     * @param x   the X coordinate to generate noise at
     * @param z   the Z coordinate to generate noise at
     * @param min the minimum value for noise (inclusive)
     * @param max the maximum value for noise (inclusive)
     * @return resulting noise at given location, in the provided range
     */
    public int getScaledNoise(int x, int z, int min, int max) {
        double noise = getNoise(x, z);

        double scaledNoise = noise * (max - min) + min;

        return (int) Math.round(scaledNoise);
    }

}