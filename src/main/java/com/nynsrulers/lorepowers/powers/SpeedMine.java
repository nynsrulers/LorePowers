package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class SpeedMine implements Listener {
    private final LorePowers plugin;
    public SpeedMine(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBoat(VehicleEnterEvent e) {
        if (e.getVehicle() instanceof Boat && e.getEntered() instanceof Player) {
            if (plugin.checkPower(e.getEntered().getUniqueId(), Power.SPEED_MINE)) {
                e.setCancelled(true);
                e.getEntered().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot ride in boats, as your arms too strong!");
                e.getVehicle().remove();
            }
        }
    }
}
