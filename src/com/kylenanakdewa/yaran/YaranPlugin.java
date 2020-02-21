package com.kylenanakdewa.yaran;

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

	}

}