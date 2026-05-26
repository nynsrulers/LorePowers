package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotatoRuler implements Listener {
    private final LorePowers plugin;
    public PotatoRuler(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (e.isCancelled()) return;
        if (!plugin.checkPower(e.getPlayer().getUniqueId(), Power.POTATO_RULER)) return;
        if (e.getItem().getType() != Material.BAKED_POTATO) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            int foodLevel = e.getPlayer().getFoodLevel() + 3; // 3 + the 5 from the baked potato itself (8 total)
            if (foodLevel > 20) foodLevel = 20;
            float saturationLevel = e.getPlayer().getSaturation() + 0.4F; // 0.4 + 1.2 from the baked potato itself (1.6 total)
            e.getPlayer().setFoodLevel(foodLevel);
            e.getPlayer().setSaturation(saturationLevel);
        }, 5L);
    }

    @EventHandler
    public void onHarvest(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.POTATOES) return;
        if (!plugin.checkPower(e.getPlayer().getUniqueId(), Power.POTATO_RULER)) return;
        if (e.getBlock().getBlockData() instanceof Ageable a && a.getAge() >= a.getMaximumAge()) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(Material.POTATO));
        }
    }
}