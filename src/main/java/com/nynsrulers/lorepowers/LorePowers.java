package com.nynsrulers.lorepowers;

import java.util.*;

import com.nynsrulers.lorepowers.commands.BestSparksIdeaCMD;
import com.nynsrulers.lorepowers.commands.DragonFormCMD;
import com.nynsrulers.lorepowers.commands.ManageCMD;
import com.nynsrulers.lorepowers.commands.ManageTabCompleter;
import com.nynsrulers.lorepowers.powers.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class LorePowers extends JavaPlugin implements Listener {
    public List<UUID> dragonFormActive = new ArrayList<>();
    public List<UUID> sparksIdeaActive = new ArrayList<>();
    public boolean libsDisguisesInstalled = false;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        CoreTools.getInstance().setPlugin(this);
        TimedEffectManager.getInstance().setPlugin(this);
        CooldownManager.getInstance().setPlugin(this);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("lorepowers").setExecutor(new ManageCMD(this));
        getCommand("lorepowers").setTabCompleter(new ManageTabCompleter());
        if (getServer().getPluginManager().getPlugin("LibsDisguises") != null) {
            libsDisguisesInstalled = true;
        }
        CoreTools.getInstance().checkForUpdates();
        registerPowers();
    }

    @Override
    public void onDisable() {
        TimedEffectManager.getInstance().stopAll();
        CooldownManager.getInstance().removeAllCooldowns();
    }

    private void registerPowers() {
        // listeners
        PluginManager mgr = getServer().getPluginManager();
        mgr.registerEvents(new VoidTotems(this), this);
        mgr.registerEvents(new BeeFlight(this), this);
        mgr.registerEvents(new SpeedMine(this), this);
        mgr.registerEvents(new GlitchedPresence(this), this);
        mgr.registerEvents(new MapWarp(this), this);
        mgr.registerEvents(new PiglinAvianTraits(this), this);
        mgr.registerEvents(new HeatResistance(this), this);
        mgr.registerEvents(new PiglinAid(this), this);
        mgr.registerEvents(new AnkleBiter(this), this);
        mgr.registerEvents(new FireBreath(this), this);
        mgr.registerEvents(new PickUp(this), this);
        mgr.registerEvents(new FoxMagic(this), this);
        mgr.registerEvents(new PotatoRuler(this), this);
        mgr.registerEvents(new PearlLink(this), this);
        mgr.registerEvents(new LightWeight(this), this);
        // commands
        getCommand("dragonform").setExecutor(new DragonFormCMD(this));
        getCommand("sparksidea").setExecutor(new BestSparksIdeaCMD(this));
    }

    public boolean checkPower(UUID playerUUID, Power power) {
        return getConfig().getStringList("PowerLinks." + playerUUID.toString()).contains(power.toString());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        powerEditCallback(e.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.MILK_BUCKET) {
            getServer().getScheduler().runTaskLater(this, () -> {
                powerEditCallback(e.getPlayer().getUniqueId());
            }, 20L);
        }
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        getServer().getScheduler().runTaskLater(this, () -> {
            powerEditCallback(e.getPlayer().getUniqueId());
        }, 20L);
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        TimedEffectManager.getInstance().stopTimedPower(e.getPlayer());
        CooldownManager.getInstance().removePlayerCooldowns(e.getPlayer().getUniqueId());
    }

    public void reloadPlugin() {
        reloadConfig();
        CoreTools.getInstance().setPlugin(this);
        TimedEffectManager.getInstance().setPlugin(this);
        CooldownManager.getInstance().setPlugin(this);
        CoreTools.getInstance().checkForUpdates();
        TimedEffectManager.getInstance().stopAll();
        CooldownManager.getInstance().removeAllCooldowns();
        for (Player player : getServer().getOnlinePlayers()) {
            // checks to make sure powers are valid!
            for (String power : getConfig().getStringList("PowerLinks." + player.getUniqueId())) {
                try {
                    Power.valueOf(power);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Power " + power + " (under player " + player.getUniqueId() + ") is not a valid power.");
                }
            }
            powerEditCallback(player.getUniqueId());
        }
    }

    public void powerEditCallback(UUID playerUUID) {
        Player player = getServer().getPlayer(playerUUID);
        // potion effect management
        // todo: make this a switch statement if possible
        if (checkPower(playerUUID, Power.SPEED_MINE)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 2, true, true, true));
            }
        } else {
            if (player != null && player.hasPotionEffect(PotionEffectType.HASTE) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.HASTE)).getAmplifier() == 2) {
                player.removePotionEffect(PotionEffectType.HASTE);
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have lost Speed Mine (Haste 3)!");
            }
        }
        if (checkPower(playerUUID, Power.VILLAGERS_RESPECT)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 1, true, true, true));
            }
        } else {
            if (player != null && player.hasPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE)).getAmplifier() == 1) {
                player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have lost Villager's Respect (Hero of the Village 2)!");
            }
        }
        if (checkPower(playerUUID, Power.PIGLIN_AVIAN_TRAITS)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, true, true));
                player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been given your Piglin and Avian traits!");
            }
        } else {
            if (player != null) {
                boolean hasAllEffects = player.hasPotionEffect(PotionEffectType.SPEED) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.SPEED)).getAmplifier() == 1 &&
                        player.hasPotionEffect(PotionEffectType.JUMP_BOOST) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.JUMP_BOOST)).getAmplifier() == 1 &&
                        player.hasPotionEffect(PotionEffectType.SLOW_FALLING) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.SLOW_FALLING)).getAmplifier() == 0 &&
                        player.hasPotionEffect(PotionEffectType.STRENGTH) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.STRENGTH)).getAmplifier() == 0;

                if (hasAllEffects) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    player.removePotionEffect(PotionEffectType.STRENGTH);
                    player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have lost your Piglin and Avian traits!");
                }
            }
        }
        if (checkPower(playerUUID, Power.DRAGON_FORM)) {
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 0, true, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, true, true));
            }
        } else {
            if (player != null) {
                boolean hasAllEffects = player.hasPotionEffect(PotionEffectType.JUMP_BOOST) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.JUMP_BOOST)).getAmplifier() == 0 &&
                        player.hasPotionEffect(PotionEffectType.SLOW_FALLING) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.SLOW_FALLING)).getAmplifier() == 0;

                if (hasAllEffects) {
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have lost your Dragon Form buffs!");
                }
            }
        }
        if (checkPower(playerUUID, Power.BESTSPARKS_IDEA)) {
            if (player != null) {
                // I don't want any potion effects...
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1, 0, true, true, true));
                // sike
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 0, 0, true, true, true));
            }
            }else{
                if (player != null) {
                    boolean hasAllEffects = player.hasPotionEffect(PotionEffectType.STRENGTH) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.STRENGTH)).getAmplifier() == 1 &&
                            player.hasPotionEffect(PotionEffectType.SLOWNESS) && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.SLOWNESS)).getAmplifier() == 0;
    
                    if (hasAllEffects) {
                        player.removePotionEffect(PotionEffectType.STRENGTH);
                        player.removePotionEffect(PotionEffectType.SLOWNESS);
                        player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "Can't believe bro got debuffed :/");
                    }
                }
            }

        if (player != null) {
            // scale management
            // todo: make this a switch statement if possible
            if (checkPower(playerUUID, Power.BEE_FLIGHT)) {
                player.getAttribute(Attribute.SCALE).setBaseValue(0.3);
            } else if (checkPower(playerUUID, Power.VILLAGERS_RESPECT)) {
                player.getAttribute(Attribute.SCALE).setBaseValue(0.5);
            } else if (checkPower(playerUUID, Power.ANKLE_BITER)) {
                player.getAttribute(Attribute.SCALE).setBaseValue(0.75);
            } else if (checkPower(playerUUID, Power.BESTSPARKS_IDEA)) {
                player.getAttribute(Attribute.SCALE).setBaseValue(0.9);
            } else if (checkPower(playerUUID, Power.DRAGON_FORM)) {
                if (player.getName().equals(".XxdeathflamexX1")) {
                    player.getAttribute(Attribute.SCALE).setBaseValue(2.0);
                } else {
                    player.getAttribute(Attribute.SCALE).setBaseValue(1.5);
                }
            } else {
                player.getAttribute(Attribute.SCALE).setBaseValue(1.0);
            }
            // flight management
            if (checkPower(playerUUID, Power.BEE_FLIGHT)) {
                player.setAllowFlight(true);
            } else {
                if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
            // health management
            // todo: make this a switch statement if possible
            if (checkPower(playerUUID, Power.BEE_FLIGHT)) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(16);
            } else if (checkPower(playerUUID, Power.FOX_MAGIC)) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(18);
            } else if (checkPower(playerUUID, Power.DRAGON_FORM)) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(24);
            } else {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
            }
            // timed effect management
            // todo: make this a switch statement if possible
            if (checkPower(playerUUID, Power.FOX_MAGIC)) {
                TimedEffectManager.getInstance().startTimedPower(player);
            } else {
                TimedEffectManager.getInstance().stopTimedPower(player);
            }
        }
    }
}
