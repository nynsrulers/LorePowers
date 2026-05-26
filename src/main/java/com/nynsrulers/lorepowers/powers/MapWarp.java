package com.nynsrulers.lorepowers.powers;

import com.nynsrulers.lorepowers.CoreTools;
import com.nynsrulers.lorepowers.LorePowers;
import com.nynsrulers.lorepowers.Power;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

public class MapWarp implements Listener {
    private final LorePowers plugin;
    public MapWarp(LorePowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack_MapWarp(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity lastCause = e.getDamager();
        if (lastCause instanceof Player && plugin.checkPower(lastCause.getUniqueId(), Power.MAP_WARP)) {
            Material weapon = ((Player) lastCause).getInventory().getItemInMainHand().getType();
            if ((weapon.toString().endsWith("_SWORD") && e.getDamage() > 5) ||
                    (weapon.toString().endsWith("_AXE") && e.getDamage() > 9)) {
                e.setCancelled(true);
                lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot attack with this, as you are too weak!");
            }
        }
    }

    @EventHandler
    public void onToolUse(PlayerInteractEvent e) {
        if (e.useInteractedBlock() == Event.Result.DENY || e.useItemInHand() == Event.Result.DENY) return;
        Player lastCause = e.getPlayer();
        if (plugin.checkPower(lastCause.getUniqueId(), Power.MAP_WARP)) {
            Material tool = lastCause.getInventory().getItemInMainHand().getType();
            Material tool2 = lastCause.getInventory().getItemInOffHand().getType();
            if (tool.toString().matches("^(NETHERITE|DIAMOND|GOLDEN|IRON)_(SWORD|PICKAXE|AXE|SHOVEL|HOE)$") ||
                    tool2.toString().matches("^(NETHERITE|DIAMOND|GOLDEN|IRON)_(SWORD|PICKAXE|AXE|SHOVEL|HOE)$")) {
                e.setCancelled(true);
                lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot use this tool, as you are too weak!");
            }
        }
    }

    @EventHandler
    public void onHurt(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        Entity hurtEntity = e.getEntity();
        if (hurtEntity instanceof Player) {
            if (plugin.checkPower(hurtEntity.getUniqueId(), Power.MAP_WARP)) {
                EntityEquipment equipment = ((Player) hurtEntity).getEquipment();
                if (equipment == null) return;
                boolean isWearingTooStrongArmor = false;
                for (ItemStack armor : equipment.getArmorContents()) {
                    if (armor == null) continue;
                    if (armor.getType().toString().startsWith("DIAMOND_") ||
                            armor.getType().toString().startsWith("NETHERITE_") ||
                            armor.getType().toString().startsWith("GOLDEN_") ||
                            armor.getType().toString().startsWith("IRON_") ||
                            armor.getType().toString().startsWith("CHAINMAIL_")) {
                        isWearingTooStrongArmor = true;
                        break;
                    }
                }
                if (isWearingTooStrongArmor) {
                    hurtEntity.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot wear this armor, as you are too weak!");
                    for (ItemStack armor : equipment.getArmorContents()) {
                        hurtEntity.getWorld().dropItem(hurtEntity.getLocation(), armor);
                    }
                    equipment.setArmorContents(new ItemStack[]{});
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        if (e.getItemDrop().getItemStack().getType() != Material.FILLED_MAP) return;
        MapMeta mapMeta = (MapMeta) e.getItemDrop().getItemStack().getItemMeta();
        if (mapMeta == null) return;
        if (plugin.checkPower(e.getPlayer().getUniqueId(), Power.MAP_WARP)) {
            if (mapMeta.getMapView().getWorld().getEnvironment() == World.Environment.NETHER) {
                e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot use this in the Nether!");
                return;
            }
            Location tpLocation = new Location(mapMeta.getMapView().getWorld(), mapMeta.getMapView().getCenterX(), 0, mapMeta.getMapView().getCenterZ());
            tpLocation.setY(tpLocation.getWorld().getHighestBlockYAt(tpLocation));
            e.getPlayer().teleport(tpLocation);
            e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been warped to the map's center!");
            e.setCancelled(true);
        }
    }
}
