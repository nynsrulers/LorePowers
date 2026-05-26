package com.nynsrulers.lorepowers.commands;

import com.nynsrulers.lorepowers.CooldownManager;
import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ManageCMD implements CommandExecutor {
    private final LorePowers plugin;
    public ManageCMD(LorePowers plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lorepowers.manage")) {
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You do not have permission to use this command.");
            return false;
        }
        if (args.length == 0) {
            sendHelpMessage(sender);
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "Config reloaded.");
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            UUID playerToCheck;
            if (args.length == 1 && sender instanceof Player) {
                playerToCheck = ((Player) sender).getUniqueId();
            } else {
                playerToCheck = plugin.getServer().getOfflinePlayer(args[1]).getUniqueId();
            }
            List<String> powers = plugin.getConfig().getStringList("PowerLinks." + playerToCheck);
            if (powers.isEmpty()) {
                sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "That player does not have any powers.");
            } else {
                sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "Powers of " + plugin.getServer().getOfflinePlayer(playerToCheck).getName() + ":");
                for (String power : powers) {
                    if (CooldownManager.getInstance().checkCooldown(playerToCheck, Power.valueOf(power))) {
                        sender.sendMessage(ChatColor.AQUA + "- " + Power.valueOf(power).getName() + ChatColor.RED + " (On Cooldown)");
                        continue;
                    }
                    sender.sendMessage(ChatColor.AQUA + "- " + Power.valueOf(power).getName());
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                sendHelpMessage(sender);
                return false;
            }
            Power powerToAdd;
            try {
                powerToAdd = Power.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "That power does not exist.");
                return false;
            }
            UUID playerToAdd;
            if (args.length == 2 && sender instanceof Player) {
                playerToAdd = ((Player) sender).getUniqueId();
            } else {
                playerToAdd = plugin.getServer().getOfflinePlayer(args[2]).getUniqueId();
            }
            List<String> powers = getPowers(playerToAdd);
            if (powers.contains(powerToAdd.toString())) {
                sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "That player already has that power.");
                return false;
            }
            powers.add(powerToAdd.toString());
            plugin.reloadConfig();
            plugin.getConfig().set("PowerLinks." + playerToAdd.toString(), powers);
            plugin.saveConfig();
            plugin.powerEditCallback(playerToAdd);
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "Added " + powerToAdd.getName() + " to " + plugin.getServer().getOfflinePlayer(playerToAdd).getName() + ".");
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sendHelpMessage(sender);
                return false;
            }
            Power powerToRemove;
            try {
                powerToRemove = Power.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "That power does not exist.");
                return false;
            }
            UUID playerToRemove;
            if (args.length == 2 && sender instanceof Player) {
                playerToRemove = ((Player) sender).getUniqueId();
            } else {
                playerToRemove = plugin.getServer().getOfflinePlayer(args[2]).getUniqueId();
            }
            List<String> powers = getPowers(playerToRemove);
            if (!powers.contains(powerToRemove.toString())) {
                sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "That player does not have that power.");
                return false;
            }
            powers.remove(powerToRemove.toString());
            plugin.reloadConfig();
            plugin.getConfig().set("PowerLinks." + playerToRemove.toString(), powers);
            plugin.saveConfig();
            plugin.powerEditCallback(playerToRemove);
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "Removed " + powerToRemove.getName() + " from " + plugin.getServer().getOfflinePlayer(playerToRemove).getName() + ".");
            return true;
        }
        if (args[0].equalsIgnoreCase("forcecallback")) {
            UUID playerToForce;
            if (args.length == 1 && sender instanceof Player) {
                playerToForce = ((Player) sender).getUniqueId();
            } else {
                playerToForce = plugin.getServer().getOfflinePlayer(args[1]).getUniqueId();
            }
            plugin.powerEditCallback(playerToForce);
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "Forced power edit callback for " + plugin.getServer().getOfflinePlayer(playerToForce).getName() + ".");
            return true;
        }
        if (args[0].equalsIgnoreCase("removecooldowns")) {
            UUID playerToForce;
            if (args.length == 1 && sender instanceof Player) {
                playerToForce = ((Player) sender).getUniqueId();
            } else {
                playerToForce = plugin.getServer().getOfflinePlayer(args[1]).getUniqueId();
            }
            CooldownManager.getInstance().removePlayerCooldowns(playerToForce);
            sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "Removed all cooldowns for " + plugin.getServer().getOfflinePlayer(playerToForce).getName() + ".");
            return true;
        }
        sendHelpMessage(sender);
        return false;
    }
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.DARK_AQUA + "LorePowers Command Help");
        sender.sendMessage(ChatColor.AQUA + "/lorepowers reload: Reload the plugin configuration.");
        sender.sendMessage(ChatColor.AQUA + "/lorepowers list [player]: List the powers of a player.");
        sender.sendMessage(ChatColor.AQUA + "/lorepowers add <power> [player]: Add a power to a player.");
        sender.sendMessage(ChatColor.AQUA + "/lorepowers remove <power> [player]: Remove a power from a player.");
        sender.sendMessage(ChatColor.AQUA + "/lorepowers forcecallback [player]: Force the power edit callback for a player.");
        sender.sendMessage(ChatColor.AQUA + "/lorepowers removecooldowns [player]: Clears all cooldowns for a player.");
    }
    private List<String> getPowers(UUID playerToCheck) {
        return plugin.getConfig().getStringList("PowerLinks." + playerToCheck.toString());
    }
}
