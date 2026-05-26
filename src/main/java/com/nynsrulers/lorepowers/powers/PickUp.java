package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

public class PickUp implements Listener {
    private final LorePowers plugin;
    public PickUp(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent e) {
        if (e.isCancelled()) return;
        if (!plugin.checkPower(e.getPlayer().getUniqueId(), Power.PICK_UP)) return;
        if (!e.getPlayer().isSneaking()) return;
        if (!(e.getRightClicked() instanceof Player carried)) return;
        e.getPlayer().addPassenger(carried);
        e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You picked up " + carried.getName() + ChatColor.GREEN + "!");
        carried.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You were picked up by " + e.getPlayer().getName() + ChatColor.GREEN + "!");
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (e.isCancelled()) return;
        if (!plugin.checkPower(e.getPlayer().getUniqueId(), Power.PICK_UP)) return;
        if (!e.isSneaking()) return;
        if (e.getPlayer().getPassengers().isEmpty()) return;
        for (Entity passenger : e.getPlayer().getPassengers()) {
            if (!(passenger instanceof Player)) continue;
            e.getPlayer().removePassenger(passenger);
            passenger.setVelocity(new Vector(0, 0.5, 0));
        }
    }

}