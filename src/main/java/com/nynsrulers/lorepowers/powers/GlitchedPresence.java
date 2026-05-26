package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GlitchedPresence implements Listener {
    private final LorePowers plugin;
    public GlitchedPresence(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && plugin.checkPower(e.getEntity().getUniqueId(), Power.GLITCHED_PRESENCE)) {
            Entity lastCause = e.getDamager();
            if (lastCause instanceof Player) {
                if (!((Player) lastCause).getInventory().getItemInMainHand().getType().toString().endsWith("_SWORD")) {
                    ((Player) lastCause).damage(e.getDamage(), e.getEntity());
                    e.setDamage(0);
                    lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You were glitched by " + e.getEntity().getName() + "'s presence!");
                    e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You glitched " + lastCause.getName() + "'s presence!");
                } else {
                    e.setDamage(e.getDamage() * 1.5);
                }
            }
        }
    }
}