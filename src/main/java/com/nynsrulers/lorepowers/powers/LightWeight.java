package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LightWeight implements Listener {
    private final LorePowers plugin;
    public LightWeight(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (plugin.checkPower(player.getUniqueId(), Power.LIGHT_WEIGHT) && player.isSneaking() && !player.isOnGround() && player.getVelocity().getY() < 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 0, true, false, false));
        } else {
            if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                PotionEffect effect = player.getPotionEffect(PotionEffectType.SLOW_FALLING);
                if (effect != null && effect.getDuration() <= 20) {
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                }
            }
        }
    }
}