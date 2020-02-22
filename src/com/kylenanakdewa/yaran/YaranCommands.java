package com.kylenanakdewa.yaran;

import java.util.Arrays;
import java.util.List;

import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.generator.ChunkGenerator;

/**
 * Commands for the Yaran plugin.
 *
 * @author Kyle Nanakdewa
 */
public final class YaranCommands implements TabExecutor {

    private final YaranPlugin plugin;

    public YaranCommands(YaranPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Version command
        if (args.length == 0 || args[0].equalsIgnoreCase("version")) {
            sender.sendMessage("Yaran " + plugin.getDescription().getVersion() + " by Kyle Nanakdewa");
            sender.sendMessage("- The Akenland world generator");
            sender.sendMessage("- Website: https://plugins.akenland.com/");
            return true;
        }

        // Reload command
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage("Yaran reloaded.");
            return true;
        }

        // Create world command
        if (args.length == 4 && args[0].equalsIgnoreCase("create") && sender.hasPermission("yaenom.admin")) {
            String generatorId = args[1];
            String worldName = args[2];
            long seed = Long.parseLong(args[3]);

            sender.sendMessage(
                    "Creating world using generator " + generatorId + ", name " + worldName + ", seed " + seed);

            ChunkGenerator generator = plugin.getDefaultWorldGenerator(worldName, generatorId);

            WorldCreator creator = new WorldCreator(worldName).generator(generator).seed(seed);
            plugin.getServer().createWorld(creator);

            sender.sendMessage("World created. Use /world " + worldName + " to access it.");
            return true;
        }

        // Invalid command
        sender.sendMessage("Invalid arguments.");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Main command - return each sub-command
        if (args.length <= 1)
            return Arrays.asList("version", "reload");
        // Otherwise return nothing
        return Arrays.asList("");
    }

}