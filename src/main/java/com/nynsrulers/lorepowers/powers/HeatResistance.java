package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class HeatResistance implements Listener {
    private final LorePowers plugin;
    public HeatResistance(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && plugin.checkPower(e.getEntity().getUniqueId(), Power.HEAT_RESISTANCE)) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                    e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    e.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                e.setCancelled(true);
            }
        }
    }
}