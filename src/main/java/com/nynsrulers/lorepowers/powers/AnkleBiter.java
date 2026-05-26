package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AnkleBiter implements Listener {
    private final LorePowers plugin;
    public AnkleBiter(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Player player && plugin.checkPower(e.getDamager().getUniqueId(), Power.ANKLE_BITER)) {
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR && Math.random() < 0.1) {
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 9, true, true, true));
                e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "Your ankles have been bitten by " + e.getDamager().getName() + ChatColor.RED + ", so you are immobilized!");
                e.getDamager().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have bitten the ankles of " + e.getEntity().getName() + ChatColor.GREEN + "!");
            }
        }
    }
}
