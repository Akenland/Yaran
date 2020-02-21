package com.kylenanakdewa.yaran;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

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