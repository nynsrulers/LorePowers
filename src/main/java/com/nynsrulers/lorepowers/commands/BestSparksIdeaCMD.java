package com.nynsrulers.lorepowers.commands;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class BestSparksIdeaCMD implements CommandExecutor {
    private final LorePowers plugin;

    public BestSparksIdeaCMD(LorePowers plugin) {
        this.plugin = plugin;
    }

    // PLEASE NOTE I COPIED OFF OF THE DRAGON FORM COMMAND IF SOME OF THIS DOESN'T WORK THEN MY BAD
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This command can only be used by players.");
            return false;
        }
        if (!plugin.libsDisguisesInstalled) {
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This power is not enabled, as Lib's Disguises is not installed!");
            return false;
        }
        Player player = (Player) sender;
        if (!plugin.checkPower(player.getUniqueId(), Power.BESTSPARKS_IDEA)) {
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You do not have this power!");
            if (plugin.sparksIdeaActive.contains(player.getUniqueId())) {
                plugin.sparksIdeaActive.remove(player.getUniqueId());
                DisguiseAPI.undisguiseToAll(player);
            }
            return false;
        }
        if (plugin.sparksIdeaActive.contains(player.getUniqueId())) {
            plugin.sparksIdeaActive.remove(player.getUniqueId());
            DisguiseAPI.undisguiseToAll(player);
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have become normal :(");
            return true;
        }
        DisguiseAPI.disguiseEntity(player, new MobDisguise(DisguiseType.CREEPER));
        plugin.sparksIdeaActive.add(player.getUniqueId());
        sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have transformed into a creeper!");
        return true;
    }
}
