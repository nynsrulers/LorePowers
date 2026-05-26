package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CooldownManager;
import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class FoxMagic implements Listener {
    private final LorePowers plugin;
    public FoxMagic(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        CooldownManager cooldown = CooldownManager.getInstance();
        if (!(e.getDamager() instanceof Player player)) return;
        if (!plugin.checkPower(e.getDamager().getUniqueId(), Power.FOX_MAGIC)) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;
        if (cooldown.checkCooldown(player.getUniqueId(), Power.FOX_MAGIC)) return;
        cooldown.addCooldown(player.getUniqueId(), Power.FOX_MAGIC, 600L);
        player.setVelocity(new Vector(player.getVelocity().getX(), 1.5, player.getVelocity().getZ()));
        player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You pounced on " + e.getEntity().getName() + ChatColor.GREEN + "!");
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 2, 6, 2)) {
                if (!(entity instanceof LivingEntity)) continue;
                if (entity.equals(player)) continue;
                ((LivingEntity) entity).damage(5, player);
                if (entity instanceof Player) {
                    entity.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You were pounced on by " + player.getName() + ChatColor.RED + "!");
                }
                if (!entity.equals(e.getEntity())) {
                    player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You also hit " + entity.getName() + ChatColor.GREEN + "!");
                }
            }
        }, 30L);
    }
}