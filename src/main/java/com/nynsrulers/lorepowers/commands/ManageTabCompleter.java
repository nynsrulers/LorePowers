package com.nynsrulers.lorepowers.commands;

import com.nynsrulers.lorepowers.Power;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("lorepowers") || command.getName().equalsIgnoreCase("powermgr")) {
            if (args.length == 1) {
                return Arrays.asList("reload", "list", "add", "remove", "forcecallback", "removecooldowns");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("forcecallback") || args[0].equalsIgnoreCase("removecooldowns")) {
                    return onlinePlayers();
                }
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                    return powers();
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                    return onlinePlayers();
                }
            }
        }
        return new ArrayList<>();
    }
    private List<String> onlinePlayers() {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }
    private List<String> powers() {
        List<String> powerNames = new ArrayList<>();
        for (Power power : Power.values()) {
            powerNames.add(power.name());
        }
        return powerNames;
    }
}
