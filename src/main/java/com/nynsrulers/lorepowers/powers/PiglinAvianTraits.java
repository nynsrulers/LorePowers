package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PiglinAvianTraits implements Listener {
    private final LorePowers plugin;
    public PiglinAvianTraits (LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && plugin.checkPower(e.getEntity().getUniqueId(), Power.PIGLIN_AVIAN_TRAITS)) {
            Entity lastCause = e.getDamager();
            if (lastCause instanceof Player) {
                if (((Player) lastCause).getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
                    e.setDamage(e.getDamage() * 1.5);
                }
            }
        }
    }
}
