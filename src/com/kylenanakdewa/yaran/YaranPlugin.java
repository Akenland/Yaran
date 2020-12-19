package com.kylenanakdewa.yaran;

import com.kylenanakdewa.yaran.generators.MinimalChunkGenerator;
import com.kylenanakdewa.yaran.generators.SimplexNoiseChunkGenerator;
import com.kylenanakdewa.yaran.generators.SimplexOctave3dChunkGenerator;
import com.kylenanakdewa.yaran.generators.SimplexOctaveChunkGenerator;
import com.kylenanakdewa.yaran.generators.YaranChunkGenerator;
import com.kylenanakdewa.yaran.worldedit.SimplexNoiseRegionCommand;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Yaran.
 *
 * @author Kyle Nanakdewa
 */
public final class YaranPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		// Main command
		getCommand("yaran").setExecutor(new YaranCommands(this));
		getCommand("2dnoise").setExecutor(new SimplexNoiseRegionCommand());

		// Run all reload tasks
		reload();
	}

	@Override
	public void onDisable() {

	}

	/** Reloads the plugin. */
	void reload() {
		// Disable/cleanup
		onDisable();

		// Load config
		saveDefaultConfig();
		loadConfig();
	}

	/** Retrieve values from config. */
	private void loadConfig() {
		reloadConfig();

		ConfigurationSection generatorSettings = getConfig().getConfigurationSection("generator-settings");
		SimplexOctaveChunkGenerator.setParameters(generatorSettings);
		SimplexNoiseChunkGenerator.setParameters(generatorSettings);
		YaranChunkGenerator.setParameters(generatorSettings);
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		switch (id.toLowerCase()) {
			case "minimal":
				return new MinimalChunkGenerator();
			case "simplex-octave":
				return new SimplexOctaveChunkGenerator();
			case "simplex-octave-3d":
				return new SimplexOctave3dChunkGenerator();
			case "simplex-noise":
				return new SimplexNoiseChunkGenerator();
			case "yaran-new":
				return new YaranChunkGenerator();

			default:
				return super.getDefaultWorldGenerator(worldName, id);
		}
	}

}