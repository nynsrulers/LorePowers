package com.nynsrulers.lorepowers.powers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.nynsrulers.lorepowers.CooldownManager;
import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PearlLink implements Listener {

    private final LorePowers plugin;
    private final Map<UUID, UUID> pearlLink = new HashMap<>();
    private final Set<UUID> recentlyDied = new HashSet<>();

    public PearlLink(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void pearlLinking(PlayerInteractEntityEvent event) {
        Player tper = event.getPlayer();

        if (!plugin.checkPower(tper.getUniqueId(), Power.PEARL_LINK)) return;

        if (!((event.getRightClicked() instanceof Player tped) && tper.isSneaking())) return;

        if (CooldownManager.getInstance().checkCooldown(tper.getUniqueId(), Power.PEARL_LINK)) {
            event.setCancelled(true);
            tper.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You drained too much dimension power, you can't link anyone right now.");
            return;
        }

        event.setCancelled(true);
        pearlLink.put(tper.getUniqueId(), tped.getUniqueId());

        tper.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have linked " + tped.getName() + " to your next Ender Pearl!");
    }

    @EventHandler
    public void teleportPerson(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player tper = event.getPlayer();
        if (!pearlLink.containsKey(tper.getUniqueId())) return;

        Player tped = Bukkit.getPlayer(pearlLink.get(tper.getUniqueId()));
        if (tped == null) return;

        event.setCancelled(true);
        tped.teleport(event.getTo());
        tped.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.BLUE + "you just got get got");

        CooldownManager.getInstance().addCooldown(tper.getUniqueId(), Power.PEARL_LINK, 2400L);
        pearlLink.remove(tper.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        recentlyDied.add(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void dimensionChange(PlayerChangedWorldEvent event) {
        Player changer = event.getPlayer();

        if (recentlyDied.remove(changer.getUniqueId())) {
            return;
        }

        if (!plugin.checkPower(changer.getUniqueId(), Power.PEARL_LINK)) return;

        Random chance = new Random();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Location coords = changer.getLocation();

            if (chance.nextDouble() < 0.80) {
                Endermite mite = changer.getWorld().spawn(coords, Endermite.class);
                mite.setPortalCooldown(200);
            }

            if (chance.nextDouble() < 0.20) {
                Endermite mite = changer.getWorld().spawn(coords, Endermite.class);
                mite.setPortalCooldown(200);
            }
        });
    }
}