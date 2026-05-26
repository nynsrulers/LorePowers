package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PiglinAid implements Listener {
    private final LorePowers plugin;
    public PiglinAid(LorePowers plugin) {
        this.plugin = plugin;
    }

    // TODO: Fix this power, it is somewhat broken!
    @EventHandler
    public void onDamageByEnemy(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(causingEntity instanceof LivingEntity)) return;
        if (!(e.getEntity() instanceof Player)) return;
        if (plugin.checkPower(e.getEntity().getUniqueId(), Power.PIGLIN_AID)) {
            for (Entity entity : e.getEntity().getNearbyEntities(20, 20, 20)) {
                if (entity instanceof PiglinAbstract) {
                    try {
                        ((PiglinAbstract) entity).setTarget((LivingEntity) causingEntity);
                        ((PiglinAbstract) entity).attack(causingEntity);
                    } catch (ClassCastException ignored) {}
                }
            }
        }
    }

    @EventHandler
    public void onDamageByWielder(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (causingEntity == null) return;
        if (!(causingEntity instanceof Player)) return;
        if (plugin.checkPower(causingEntity.getUniqueId(), Power.PIGLIN_AID)) {
            for (Entity entity : e.getEntity().getNearbyEntities(20, 20, 20)) {
                if (entity instanceof PiglinAbstract) {
                    try {
                        ((PiglinAbstract) entity).setTarget((LivingEntity) e.getEntity());
                        ((PiglinAbstract) entity).attack(e.getEntity());
                    } catch (ClassCastException ignored) {}
                }
            }
        }
    }

    @EventHandler
    public void onPiglinDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof PiglinAbstract) {
            for (Entity entity : e.getEntity().getNearbyEntities(20, 20, 20)) {
                if (entity instanceof Player) {
                    if (plugin.checkPower(entity.getUniqueId(), Power.PIGLIN_AID)) {
                        entity.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "A piglin has died near you!");
                        ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 1, true, true, true));
                        ((Player) entity).damage(5);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof PiglinAbstract) {
            if (e.getTarget() == null) return;
            if (plugin.checkPower(e.getTarget().getUniqueId(), Power.PIGLIN_AID)) {
                e.setCancelled(true);
            }
        }
    }
}