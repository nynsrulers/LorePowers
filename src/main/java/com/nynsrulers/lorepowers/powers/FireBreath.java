package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class FireBreath implements Listener {
    private final LorePowers plugin;
    public FireBreath(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!plugin.checkPower(e.getDamager().getUniqueId(), Power.FIRE_BREATH)) return;
        if (!(e.getDamager() instanceof Player player)) return;
        if (player.isSneaking()) {
            player.setFireTicks(100);
            player.damage(2);
            Location playerLocation = player.getLocation();
            Vector direction = playerLocation.getDirection().normalize();
            for (int i = 1; i <= 5; i++) {
                Location checkLocation = playerLocation.clone().add(direction.clone().multiply(i));
                player.getWorld().spawnParticle(Particle.FLAME, checkLocation, 10, 0.2, 0.2, 0.2, 0.01);
                for (Entity entity : player.getWorld().getNearbyEntities(checkLocation, 1, 1, 1)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        entity.setFireTicks(100);
                        ((LivingEntity) entity).damage(3);
                    }
                }
            }
        }
    }

}
