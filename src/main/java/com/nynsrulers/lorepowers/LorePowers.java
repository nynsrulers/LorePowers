package com.nynsrulers.lorepowers;

import java.util.*;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class LorePowers extends JavaPlugin implements Listener {
    public HashMap<UUID, BukkitTask> beeFlightTasks = new HashMap<>();
    public List<UUID> dragonFormActive = new ArrayList<>();
    public List<UUID> sparksIdeaActive = new ArrayList<>();
    public boolean libsDisguisesInstalled = false;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        CoreTools.getInstance().setPlugin(this);
        TimedEffectManager.getInstance().setPlugin(this);
        CooldownManager.getInstance().setPlugin(this);
        getCommand("lorepowers").setExecutor(new ManageCMD(this));
        getCommand("lorepowers").setTabCompleter(new ManageTabCompleter());
        getCommand("dragonform").setExecutor(new DragonFormCMD(this));
        getCommand("sparksidea").setExecutor(new BestSparksIdea(this));
        if (getServer().getPluginManager().getPlugin("LibsDisguises") != null) {
            libsDisguisesInstalled = true;
        }
        CoreTools.getInstance().checkForUpdates();
    }

    @Override
    public void onDisable() {
        TimedEffectManager.getInstance().stopAll();
        CooldownManager.getInstance().removeAllCooldowns();
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

    @EventHandler
    public void onTotem_VoidTotems(EntityResurrectEvent e) {
        if (e.isCancelled()) return;
        Entity lastCause = e.getEntity().getLastDamageCause().getDamageSource().getCausingEntity();
        if (lastCause instanceof Player && checkPower(lastCause.getUniqueId(), Power.VOID_TOTEMS)) {
            e.setCancelled(true);
            EntityEquipment killedItems = e.getEntity().getEquipment();
            if (killedItems != null) {
                if (killedItems.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING) {
                    killedItems.setItemInMainHand(null);
                } else if (killedItems.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
                    killedItems.setItemInOffHand(null);
                }
            }
            e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "Your totem was voided by the powers of " + lastCause.getName() + "!");
            lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You voided " + e.getEntity().getName() + "'s totem!");
            ((Player) lastCause).addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 400, 2, true, true, true));
            ((Player) lastCause).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 2, true, true, true));
        }
    }

    @EventHandler
    public void onAttack_BeeFlight(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Player player && checkPower(e.getDamager().getUniqueId(), Power.BEE_FLIGHT)) {
            if (player.getInventory().getItemInMainHand().getType().toString().endsWith("_SWORD") && Math.random() < 0.1) {
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1, true, true, true));
                ((Player) e.getDamager()).damage(2);
                e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You have been stung by " + e.getDamager().getName() + ChatColor.RED + "!");
                e.getDamager().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You stung " + e.getEntity().getName() + ChatColor.GREEN + "!");
            }

        }
    }
    @EventHandler
    public void onDamage_BeeFlight(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player player) || !checkPower(player.getUniqueId(), Power.BEE_FLIGHT)) return;
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
    public void onToggleFlight_BeeFlight(PlayerToggleFlightEvent e) {
        if (e.isCancelled()) return;
        if (!checkPower(e.getPlayer().getUniqueId(), Power.BEE_FLIGHT)) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (e.isFlying()) {
            if (CooldownManager.getInstance().checkCooldown(e.getPlayer().getUniqueId(), Power.BEE_FLIGHT)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You are too tired to fly right now.");
                return;
            }
            e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You are now flying with bee wings (for up to 30 seconds)!");
            BukkitTask task = getServer().getScheduler().runTaskLater(this, () -> {
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

    @EventHandler
    public void onAttack_GlitchedPresence(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.GLITCHED_PRESENCE)) {
            Entity lastCause = e.getDamager();
            if (lastCause instanceof Player) {
                if (!((Player) lastCause).getInventory().getItemInMainHand().getType().toString().endsWith("_SWORD")) {
                    ((Player) lastCause).damage(e.getDamage(), e.getEntity());
                    e.setDamage(0);
                    lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You were glitched by " + e.getEntity().getName() + "'s presence!");
                    e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You glitched " + lastCause.getName() + "'s presence!");
                } else {
                    e.setDamage(e.getDamage() * 1.5);
                }
            }
        }
    }

    @EventHandler
    public void onAttack_MapWarp(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity lastCause = e.getDamager();
        if (lastCause instanceof Player && checkPower(lastCause.getUniqueId(), Power.MAP_WARP)) {
            Material weapon = ((Player) lastCause).getInventory().getItemInMainHand().getType();
            if ((weapon.toString().endsWith("_SWORD") && e.getDamage() > 5) ||
                    (weapon.toString().endsWith("_AXE") && e.getDamage() > 9)) {
                e.setCancelled(true);
                lastCause.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot attack with this, as you are too weak!");
            }
        }
    }
    @EventHandler
    public void onToolUse_MapWarp(PlayerInteractEvent e) {
        if (e.useInteractedBlock() == Result.DENY || e.useItemInHand() == Result.DENY) return;
        Player lastCause = e.getPlayer();
        if (checkPower(lastCause.getUniqueId(), Power.MAP_WARP)) {
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
    public void onHurt_MapWarp(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        Entity hurtEntity = e.getEntity();
        if (hurtEntity instanceof Player) {
            if (checkPower(hurtEntity.getUniqueId(), Power.MAP_WARP)) {
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
    public void onDrop_MapWarp(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        if (e.getItemDrop().getItemStack().getType() != Material.FILLED_MAP) return;
        MapMeta mapMeta = (MapMeta) e.getItemDrop().getItemStack().getItemMeta();
        if (mapMeta == null) return;
        if (checkPower(e.getPlayer().getUniqueId(), Power.MAP_WARP)) {
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

    @EventHandler
    public void onBoat_SpeedMine(VehicleEnterEvent e) {
        if (e.getVehicle() instanceof Boat && e.getEntered() instanceof Player) {
            if (checkPower(((Player) e.getEntered()).getUniqueId(), Power.SPEED_MINE)) {
                e.setCancelled(true);
                e.getEntered().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "You cannot ride in boats, as your arms too strong!");
                e.getVehicle().remove();
            }
        }
    }

    @EventHandler
    public void onHit_PiglinAvianTraits(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.PIGLIN_AVIAN_TRAITS)) {
            Entity lastCause = e.getDamager();
            if (lastCause instanceof Player) {
                if (((Player) lastCause).getInventory().getItemInMainHand().getType().toString().endsWith("_AXE")) {
                    e.setDamage(e.getDamage() * 1.5);
                }
            }
        }
    }

    @EventHandler
    public void onDamage_HeatResistance(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player && checkPower(e.getEntity().getUniqueId(), Power.HEAT_RESISTANCE)) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                    e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    e.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                e.setCancelled(true);
            }
        }
    }

    // TODO: Fix this power, it is somewhat broken!
    @EventHandler
    public void onDamageByEnemy_PiglinAid(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(causingEntity instanceof LivingEntity)) return;
        if (!(e.getEntity() instanceof Player)) return;
        if (checkPower(e.getEntity().getUniqueId(), Power.PIGLIN_AID)) {
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
    public void onDamageByWielder_PiglinAid(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (causingEntity == null) return;
        if (!(causingEntity instanceof Player)) return;
        if (checkPower(causingEntity.getUniqueId(), Power.PIGLIN_AID)) {
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
    public void onPiglinDeath_PiglinAid(EntityDeathEvent e) {
        if (e.getEntity() instanceof PiglinAbstract) {
            for (Entity entity : e.getEntity().getNearbyEntities(20, 20, 20)) {
                if (entity instanceof Player) {
                    if (checkPower(entity.getUniqueId(), Power.PIGLIN_AID)) {
                        entity.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "A piglin has died near you!");
                        ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 1, true, true, true));
                        ((Player) entity).damage(5);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onTarget_PiglinAid(EntityTargetLivingEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof PiglinAbstract) {
            if (e.getTarget() == null) return;
            if (checkPower(e.getTarget().getUniqueId(), Power.PIGLIN_AID)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAttack_AnkleBiter(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Player player && checkPower(e.getDamager().getUniqueId(), Power.ANKLE_BITER)) {
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR && Math.random() < 0.1) {
                ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 9, true, true, true));
                e.getEntity().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.RED + "Your ankles have been bitten by " + e.getDamager().getName() + ChatColor.RED + ", so you are immobilized!");
                e.getDamager().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You have bitten the ankles of " + e.getEntity().getName() + ChatColor.GREEN + "!");
            }
        }
    }

    @EventHandler
    public void onDamage_FireBreath(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!checkPower(e.getDamager().getUniqueId(), Power.FIRE_BREATH)) return;
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

    @EventHandler
    public void onAttack_FoxMagic(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        CooldownManager cooldown = CooldownManager.getInstance();
        if (!(e.getDamager() instanceof Player player)) return;
        if (!checkPower(e.getDamager().getUniqueId(), Power.FOX_MAGIC)) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;
        if (cooldown.checkCooldown(player.getUniqueId(), Power.FOX_MAGIC)) return;
        cooldown.addCooldown(player.getUniqueId(), Power.FOX_MAGIC, 600L);
        player.setVelocity(new Vector(player.getVelocity().getX(), 1.5, player.getVelocity().getZ()));
        player.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You pounced on " + e.getEntity().getName() + ChatColor.GREEN + "!");
        getServer().getScheduler().runTaskLater(this, () -> {
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

    @EventHandler
    public void onPlayerClick_PickUp(PlayerInteractEntityEvent e) {
        if (e.isCancelled()) return;
        if (!checkPower(e.getPlayer().getUniqueId(), Power.PICK_UP)) return;
        if (!e.getPlayer().isSneaking()) return;
        if (!(e.getRightClicked() instanceof Player carried)) return;
        e.getPlayer().addPassenger(carried);
        e.getPlayer().sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You picked up " + carried.getName() + ChatColor.GREEN + "!");
        carried.sendMessage(CoreTools.getInstance().getPrefix() + ChatColor.GREEN + "You were picked up by " + e.getPlayer().getName() + ChatColor.GREEN + "!");
    }
    @EventHandler
    public void onSneak_PickUp(PlayerToggleSneakEvent e) {
        if (e.isCancelled()) return;
        if (!checkPower(e.getPlayer().getUniqueId(), Power.PICK_UP)) return;
        if (!e.isSneaking()) return;
        if (e.getPlayer().getPassengers().isEmpty()) return;
        for (Entity passenger : e.getPlayer().getPassengers()) {
            if (!(passenger instanceof Player)) continue;
            e.getPlayer().removePassenger(passenger);
            passenger.setVelocity(new Vector(0, 0.5, 0));
        }
    }

    @EventHandler
    public void onEat_PotatoRuler(PlayerItemConsumeEvent e) {
        if (e.isCancelled()) return;
        if (!checkPower(e.getPlayer().getUniqueId(), Power.POTATO_RULER)) return;
        if (e.getItem().getType() != Material.BAKED_POTATO) return;
        getServer().getScheduler().runTaskLater(this, () -> {
            int foodLevel = e.getPlayer().getFoodLevel() + 3; // 3 + the 5 from the baked potato itself (8 total)
            if (foodLevel > 20) foodLevel = 20;
            float saturationLevel = e.getPlayer().getSaturation() + 0.4F; // 0.4 + 1.2 from the baked potato itself (1.6 total)
            e.getPlayer().setFoodLevel(foodLevel);
            e.getPlayer().setSaturation(saturationLevel);
        }, 5L);
    }
    @EventHandler
    public void onHarvest_PotatoRuler(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.POTATOES) return;
        if (!checkPower(e.getPlayer().getUniqueId(), Power.POTATO_RULER)) return;
        if (e.getBlock().getBlockData() instanceof Ageable a && a.getAge() >= a.getMaximumAge()) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(Material.POTATO));
        }
    }

    void reloadPlugin() {
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
        } else {
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
