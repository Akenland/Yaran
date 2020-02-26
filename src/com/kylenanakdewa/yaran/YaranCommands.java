package com.kylenanakdewa.yaran;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            plugin.reload();

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

        // Re-create world command
        if (args.length == 2 && args[0].equalsIgnoreCase("recreate")) {
            plugin.reload();

            String worldName = args[1];
            World oldWorld = plugin.getServer().getWorld(worldName);
            ChunkGenerator generator = oldWorld.getGenerator();
            long seed = oldWorld.getSeed();

            // Teleport players out of old world
            boolean isSenderInWorld = false;
            for (LivingEntity entity : oldWorld.getLivingEntities()) {
                if (entity instanceof Player) {
                    entity.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
                }
                if(entity.equals(sender)){
                    isSenderInWorld = true;
                }
            }

            // Unload old world
            if (!plugin.getServer().unloadWorld(oldWorld, false)) {
                sender.sendMessage("Unable to unload world " + worldName);
                return false;
            }

            // Delete old world
            if (!new File(plugin.getServer().getWorldContainer(), worldName).delete()) {
                sender.sendMessage("Unable to delete world " + worldName);
                return false;
            }

            sender.sendMessage("Creating world using generator " + generator.getClass().getSimpleName() + ", name "
                    + worldName + ", seed " + seed);

            WorldCreator creator = new WorldCreator(worldName).generator(generator).seed(seed);
            World newWorld = plugin.getServer().createWorld(creator);

            // Automatically return sender to recreated world, if they were in it previously
            if(isSenderInWorld){
                ((LivingEntity) sender).teleport(newWorld.getSpawnLocation());
            }

            sender.sendMessage("World created. Use /world " + worldName + " to access it.");
            return true;
        }

        // Invalid command
        sender.sendMessage("Invalid arguments.");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Create world command
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("simplex-noise", "simplex-octave", "simplex-octave-3d", "minimal");
        }

        // Main command - return each sub-command
        if (args.length <= 1)
            return Arrays.asList("version", "reload", "create", "recreate");

        // Otherwise return nothing
        return Arrays.asList("");
    }

}