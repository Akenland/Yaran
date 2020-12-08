package com.kylenanakdewa.yaran.worldedit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

/**
 * A WorldEdit region generator command that uses the SimplexNoiseGenerator. The
 * parameters for the noise generator are set in the command.
 *
 * @author Kyle Nanakdewa
 */
public class SimplexNoiseRegionCommand implements TabExecutor {

    /**
     * The default frequencies for noise generation.
     */
    private static Map<Double, Double> defaultFrequencies = getDefaultFrequencies();

    private static Map<Double, Double> getDefaultFrequencies() {
        Map<Double, Double> map = new HashMap<Double, Double>();
        map.put(0.005, 2.0);
        map.put(0.0075, 2.0);
        map.put(0.05, 0.25);
        return map;
    }

    /**
     * The default exponent for noise generation.
     */
    private static int defaultExponent = 3;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Sender must specify pattern and amplitude, and must be player
        if (args.length < 2 || !(sender instanceof org.bukkit.entity.Player)) {
            return false;
        }

        Player actor = BukkitAdapter.adapt((org.bukkit.entity.Player) sender);

        Pattern pattern;

        CuboidRegion region;

        int amplitude;
        Map<Double, Double> frequencies = defaultFrequencies;
        double exponent = defaultExponent;

        try {
            // WorldEdit pattern parser
            pattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(args[0], new ParserContext());

            // Amplitude parser
            amplitude = Integer.parseInt(args[1]);

            // Frequencies parser
            if (args.length >= 3) {
                frequencies = new HashMap<Double, Double>();
                for (String entry : args[2].split(",")) {
                    double frequency = Double.parseDouble(entry.split(":")[0]);
                    double size = Double.parseDouble(entry.split(":")[1]);
                    frequencies.put(frequency, size);
                }
            }

            // Exponent parser
            if (args.length == 4) {
                exponent = Double.parseDouble(args[3]);
            }

            // Region selection
            region = (CuboidRegion) WorldEdit.getInstance().getSessionManager().get(actor)
                    .getSelection(actor.getWorld());
        } catch (InputParseException | NumberFormatException | IncompleteRegionException e) {
            sender.sendMessage(e.getLocalizedMessage());
            return false;
        }

        SimplexNoiseRegionGenerator generator = new SimplexNoiseRegionGenerator(region, amplitude, frequencies,
                exponent);

        int blocksChanged;

        try (EditSession session = WorldEdit.getInstance().newEditSession(actor)) {
            blocksChanged = generator.generate(session, pattern);
        } catch (MaxChangedBlocksException e) {
            sender.sendMessage(e.getLocalizedMessage());
            return false;
        }

        sender.sendMessage("Yaran generated " + blocksChanged + " blocks.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(args.length == 1) {
            return WorldEdit.getInstance().getPatternFactory().getSuggestions(args[0]);
        }

        return Arrays.asList("");
    }

}
