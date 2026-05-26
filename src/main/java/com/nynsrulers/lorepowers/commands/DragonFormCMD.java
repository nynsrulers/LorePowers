package com.nynsrulers.lorepowers.commands;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

public class DragonFormCMD implements CommandExecutor {
  private final LorePowers plugin;
  public DragonFormCMD(LorePowers plugin) {
    this.plugin = plugin;
  }
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This command has been forcibly disabled, as it breaks things and is not ready.");
    sender.sendMessage(ChatColor.RED + "Please give Aelithron a few days, they ran out of time before the server launch.");
    return false;
//    if (!(sender instanceof Player)) {
//      sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This command can only be used by players.");
//      return false;
//    }
//    if (!plugin.libsDisguisesInstalled) {
//        sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "This power is not enabled, as Lib's Disguises is not installed!");
//        return false;
//    }
//    Player player = (Player) sender;
//    if (!plugin.checkPower(player.getUniqueId(), Power.DRAGON_FORM)) {
//      sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You do not have this power!.");
//      if (plugin.dragonFormActive.contains(player.getUniqueId())) {
//        plugin.dragonFormActive.remove(player.getUniqueId());
//        DisguiseAPI.undisguiseToAll(player);
//      }
//      return false;
//    }
//    if (plugin.dragonFormActive.contains(player.getUniqueId())) {
//      plugin.dragonFormActive.remove(player.getUniqueId());
//      DisguiseAPI.undisguiseToAll(player);
//      sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have returned to a human form!");
//      return true;
//    }
//    DisguiseAPI.disguiseEntity(player, new MobDisguise(DisguiseType.ENDER_DRAGON));
//    plugin.dragonFormActive.add(player.getUniqueId());
//    sender.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have transformed into a Dragon form!");
//    return true;
  }
}