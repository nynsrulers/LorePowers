package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CooldownManager;
import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class BeeFlight implements Listener {
    public HashMap<UUID, BukkitTask> beeFlightTasks = new HashMap<>();
    private final LorePowers plugin;
    public BeeFlight(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Player player && plugin.checkPower(e.getDamager().getUniqueId(), Power.BEE_FLIGHT)) {
            if (player.getInventory().getItemInMainHand().getType().toString().endsWith("_SWORD") && Math.random() < 0.1) {
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1, true, true, true));
                ((Player) e.getDamager()).damage(2);
                e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been stung by " + e.getDamager().getName() + ChatColor.RED + "!");
                e.getDamager().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You stung " + e.getEntity().getName() + ChatColor.GREEN + "!");
            }

        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player player) || !plugin.checkPower(player.getUniqueId(), Power.BEE_FLIGHT)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!player.isFlying()) return;
        player.setFlying(false);
        player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You were damaged, and have stopped flying with bee wings.");
        CooldownManager.getInstance().addCooldown(player.getUniqueId(), Power.BEE_FLIGHT, 300L);
        if (beeFlightTasks.containsKey(player.getUniqueId())) {
            beeFlightTasks.get(player.getUniqueId()).cancel();
            beeFlightTasks.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        if (e.isCancelled()) return;
        if (!plugin.checkPower(e.getPlayer().getUniqueId(), Power.BEE_FLIGHT)) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (e.isFlying()) {
            if (CooldownManager.getInstance().checkCooldown(e.getPlayer().getUniqueId(), Power.BEE_FLIGHT)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You are too tired to fly right now.");
                return;
            }
            e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You are now flying with bee wings (for up to 30 seconds)!");
            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (e.getPlayer().isFlying() && e.getPlayer().getGameMode() != GameMode.CREATIVE && e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                    e.getPlayer().setFlying(false);
                    e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have stopped flying with bee wings!");
                    CooldownManager.getInstance().addCooldown(e.getPlayer().getUniqueId(), Power.BEE_FLIGHT, 300L);
                }
            }, 600L);
            beeFlightTasks.put(e.getPlayer().getUniqueId(), task);
        } else {
            e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have stopped flying with bee wings!");
            CooldownManager.getInstance().addCooldown(e.getPlayer().getUniqueId(), Power.BEE_FLIGHT, 300L);
            if (beeFlightTasks.containsKey(e.getPlayer().getUniqueId())) {
                beeFlightTasks.get(e.getPlayer().getUniqueId()).cancel();
                beeFlightTasks.remove(e.getPlayer().getUniqueId());
            }
        }
    }
}