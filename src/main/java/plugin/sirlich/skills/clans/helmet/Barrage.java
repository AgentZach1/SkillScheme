package plugin.sirlich.skills.clans.helmet;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.ChargeSkill;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.skills.meta.AffectType;
import plugin.sirlich.utilities.BlockUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.FireworkMeta;
import java.util.Random;

public class Barrage extends ChargeSkill {
    public Barrage(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "Barrage", false, true);
    }
    private int schedularID;
    private AffectType affect = AffectType.NONE;
    private final List<Item> blazePowders = new ArrayList<>();
    private final List<Item> healthItems = new ArrayList<>();
    private final List<Item> slowItems = new ArrayList<>();
    private final List<Item> dmgItems = new ArrayList<>();
    @Override
    public boolean isCharging(){
        return getRpgPlayer().isDrawingBow() && getRpgPlayer().isBowFullyCharged();
    }

    @Override
    public void onArrowHitGround(ProjectileHitEvent event){
        RpgProjectile rpgProjectile = RpgProjectile.getProjectile(event.getEntity().getUniqueId());
        if(rpgProjectile.hasTag("REMOVE_ON_HIT")){
            
            if (affect == AffectType.NETHERITE) {
                // Drop fire items
                ItemStack blazePowderStack = new ItemStack(Material.BLAZE_POWDER);
                Item blazePowder = getRpgPlayer().getPlayer().getWorld().dropItem(event.getEntity().getLocation(), blazePowderStack);
                blazePowder.setPickupDelay(Integer.MAX_VALUE); // Prevent pickup by players
                Vector randomDirection = generateRandomDirection().normalize().multiply(0.2);
                blazePowder.setVelocity(randomDirection); // Add a slight velocity
                blazePowders.add(blazePowder);

                // Schedule the removal of the blaze powder after 5 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (blazePowder.isValid()) {
                            blazePowder.remove();
                            blazePowders.remove(blazePowder);
                        }
                    }
                }.runTaskLater(plugin.sirlich.SkillScheme.getInstance(), 100L); // 5 seconds (100 ticks)
                World world = getRpgPlayer().getPlayer().getWorld();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (blazePowders.isEmpty()) {
                            this.cancel();
                            return;
                        }

                        for (Item blazePowder : new ArrayList<>(blazePowders)) {
                            if (!blazePowder.isValid()) continue;

                            // Check for nearby entities
                            List<Entity> nearbyEntities = blazePowder.getNearbyEntities(1.0, 1.0, 1.0);
                            for (Entity entity : nearbyEntities) {
                                if (entity instanceof LivingEntity target && !entity.equals(getRpgPlayer().getPlayer())) {
                                    // Apply fire ticks to the target
                                    target.setFireTicks(20); // 1 second of fire

                                    // Remove the blaze powder
                                    blazePowder.remove();
                                    blazePowders.remove(blazePowder);

                                    // Optional: Play particle effect on the target
                                    world.spawnParticle(Particle.FLAME, target.getLocation(), 10, 0.5, 0.5, 0.5, 0);
                                    break;
                                }
                            }
                        }
                    }
                }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 0L, 1L);
            }
            else if (affect == AffectType.IRON) {
                // Drop dmg 
                ItemStack dmgStack = new ItemStack(Material.FIREWORK_STAR);
                Item dmgItem = getRpgPlayer().getPlayer().getWorld().dropItem(event.getEntity().getLocation(), dmgStack);
                dmgItem.setPickupDelay(Integer.MAX_VALUE); // Prevent pickup by players
                Vector randomDirection = generateRandomDirection().normalize().multiply(0.2);
                dmgItem.setVelocity(randomDirection); // Add a slight velocity
                dmgItems.add(dmgItem);
                

                // Schedule the removal of the blaze powder after 5 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (dmgItem.isValid()) {
                            dmgItem.remove();
                            dmgItems.remove(dmgItem);
                        }
                    }
                }.runTaskLater(plugin.sirlich.SkillScheme.getInstance(), 100L); // 5 seconds (100 ticks)
                World world = getRpgPlayer().getPlayer().getWorld();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (dmgItems.isEmpty()) {
                            this.cancel();
                            return;
                        }

                        for (Item dmgItem : new ArrayList<>(dmgItems)) {
                            if (!dmgItem.isValid()) continue;

                            // Check for nearby entities
                            List<Entity> nearbyEntities = dmgItem.getNearbyEntities(1.0, 1.0, 1.0);
                            for (Entity entity : nearbyEntities) {
                                if (entity instanceof LivingEntity target && !entity.equals(getRpgPlayer().getPlayer())) {
                                    target.damage(2, getRpgPlayer().getPlayer()); 

                                    dmgItem.remove();
                                    dmgItems.remove(dmgItem);
                                    target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 3, 1.0, 1.0, 1.0, 0, null, true);
                                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 4.0F, 0.4F);

                                    // Optional: Play particle effect on the target
                                    world.spawnParticle(Particle.CRIMSON_SPORE, target.getLocation(), 10, 0.5, 0.2, 0.5, 0);
                                    break;
                                }
                            }
                        }
                    }
                }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 0L, 1L);

            } else if (affect == AffectType.DIAMOND) {
                // Spawn random types of fireworks at location
                spawnRandomFirework(event.getEntity().getLocation());

            } else if (affect == AffectType.GOLD) {
                // Drops healing items
                handleHealing(event.getEntity());

            } else if (affect == AffectType.CHAINMAIL) {
                // Drops slowness items
                Location entLoc = event.getEntity().getLocation();
                Block block = entLoc.getBlock();
                if (block.isEmpty()) {
                    BlockUtils.tempPlaceBlock(Material.COBWEB, entLoc, 40);
                } else {
                    // find the closest empty block in a 3x3 cube with the item location centered in the middle
                    Location closestEmpty = findClosestEmptyBlock(entLoc, 1);
                    if (closestEmpty != null) {
                        BlockUtils.tempPlaceBlock(Material.COBWEB, closestEmpty, 40);
                    }   
                }

            } else if (affect == AffectType.LEATHER) {
                // Give blindness to 1x1x1
                Location impactLoc = event.getEntity().getLocation();
                World world = impactLoc.getWorld();
                
                // Spawn squid ink particle cloud
                new BukkitRunnable() {
                    int duration = 20; // 1 second (20 ticks) of particles
                    double radius = 1.5; // 3x3 area
                    
                    @Override
                    public void run() {
                        if (duration-- <= 0) {
                            this.cancel();
                            return;
                        }
                        
                        // Create particle cloud
                        for (int i = 0; i < 30; i++) {
                            double x = impactLoc.getX() + (Math.random() - 0.5) * radius * 2;
                            double y = impactLoc.getY() + Math.random() * radius;
                            double z = impactLoc.getZ() + (Math.random() - 0.5) * radius * 2;
                            world.spawnParticle(Particle.SQUID_INK, x, y, z, 1, 0, 0, 0, 0);
                        }
                        
                        // Check for entities in radius
                        for (LivingEntity entity : world.getLivingEntities()) {
                            if (entity.getLocation().distance(impactLoc) <= radius) {
                                // Check for existing blindness
                                PotionEffect currentBlindness = entity.getPotionEffect(PotionEffectType.BLINDNESS);
                                int newDuration = 60; // 3 seconds (60 ticks)
                                
                                if (currentBlindness != null) {
                                    newDuration = currentBlindness.getDuration() + 60; // Add 3 seconds
                                }
                                
                                // Apply/refresh blindness
                                entity.addPotionEffect(new PotionEffect(
                                    PotionEffectType.BLINDNESS,
                                    newDuration,
                                    0, // Amplifier 0
                                    false, // No ambient particles
                                    true // Show icon
                                ));
                                
                                // Additional effect particles on entity
                                world.spawnParticle(Particle.WITCH, entity.getEyeLocation(), 5, 0.3, 0.3, 0.3, 0.1);
                            }
                        }
                    }
                }.runTaskTimer(SkillScheme.getInstance(), 0, 1);
            }
            event.getEntity().remove();
        }
    }

    @Override
    public void onBowFire(EntityShootBowEvent event){
        if(isCharging()){
            shootArrows();
        }
    }

    private void shootArrows(){
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 0);
        schedularID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SkillScheme.getInstance(), new Runnable() {
            int charges = getCharges();
            public void run() {
                if(charges == 0) {
                    Bukkit.getServer().getScheduler().cancelTask(schedularID);
                } else {
                    shootArrow();
                    charges = charges - 1;
                }
            }
        }, 0L, 1);
    }

    private void shootArrow(){
        if(isSilenced()){return;};
        Player player = getRpgPlayer().getPlayer();
        Vector random = new Vector((Math.random() - 0.5D) / 10.0D, (Math.random() - 0.5D) / 10.0D, (Math.random() - 0.5D) / 10.0D);
        Arrow arrow = getRpgPlayer().getPlayer().launchProjectile(Arrow.class);
        player.playSound(player.getLocation(), Sound.valueOf("ENTITY_ARROW_SHOOT"), 1.0F, 1.0F);
        RpgProjectile rpgProjectile = RpgProjectile.registerProjectile(arrow, getRpgPlayer());
        rpgProjectile.addTag("REMOVE_ON_HIT");
        arrow.setVelocity(player.getLocation().getDirection().add(random).multiply(3));

        new BukkitRunnable() {
            
            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    this.cancel();
                    return;
                }
                if (affect == AffectType.NETHERITE) {
                    arrow.getWorld().spawnParticle(Particle.DRIPPING_LAVA, arrow.getLocation(), 3, 0, 0, 0, 1);
                }
                else if (affect == AffectType.TURTLE) {
                    // Drip water
                    arrow.getWorld().spawnParticle(Particle.DRIPPING_WATER, arrow.getLocation(), 3, 0, 0, 0, 1);
                    if (arrow.getLocation().getBlock().isLiquid()) {
                        // increase speed of arrow in the velocity that it was already going in
                        Vector currentVel = arrow.getVelocity();
                        // Check if velocity is significant enough to normalize
                        if (currentVel.lengthSquared() > 0.0001) {
                            try {
                                Vector waterBoost = currentVel.clone().normalize().multiply(2.0);
                                Vector newVelocity = currentVel.add(waterBoost);
                                
                                // Verify the new velocity is finite
                                if (Double.isFinite(newVelocity.getX()) && 
                                    Double.isFinite(newVelocity.getY()) && 
                                    Double.isFinite(newVelocity.getZ())) {
                                    arrow.setVelocity(newVelocity);
                                } else {
                                    Bukkit.getLogger().warning("[SkillScheme] Invalid velocity detected (non-finite values)");
                                }
                            } catch (IllegalArgumentException e) {
                                Bukkit.getLogger().warning("[SkillScheme] Error applying water boost: " + e.getMessage());
                            }
                        } else {
                            Bukkit.getLogger().warning("[SkillScheme] Arrow velocity too small for water boost");
                        }

                        // Vector waterBoost = currentVel.normalize().multiply(2.0); // 50% speed boost
                        // arrow.setVelocity(currentVel.add(waterBoost));
                        // double resistanceFactor = 0.7; // How much to reduce the natural water slowdown
                        // Vector waterResistance = new Vector(
                        //     currentVel.getX() * (1 - resistanceFactor/3), // Less X resistance
                        //     currentVel.getY() * (1 - resistanceFactor),   // More Y resistance
                        //     currentVel.getZ() * (1 - resistanceFactor/3)  // Less Z resistance
                        // );
                        
                        // // Apply speed boost in original direction
                        // Vector waterBoost = currentVel.normalize().multiply(0.5); // 50% speed boost
                        // Vector newVelocity = currentVel.add(waterBoost).subtract(waterResistance);
                        // arrow.setVelocity(currentVel.add(waterBoost));
                        arrow.getWorld().spawnParticle(Particle.BUBBLE, arrow.getLocation(), 3, 0, 0, 0, 1);
                        arrow.getWorld().spawnParticle(Particle.BUBBLE_POP, arrow.getLocation(), 3, 0, 0, 0, 0);
                        arrow.getWorld().playSound(arrow.getLocation(), Sound.valueOf("BLOCK_LANTERN_HIT"), 1F, 2F);
                    }

                } else if (affect == AffectType.IRON) {
                    arrow.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 2, 0, 0, 0, 0.1);

                } else if (affect == AffectType.DIAMOND) {
                    // Blue dust
                    Color dustColor = Color.fromRGB(0, 0, 255);
                    DustOptions dust = new DustOptions(dustColor, 2.0F);
                    arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 2, 0, 0, 0, dust);

                } else if (affect == AffectType.GOLD) {
                    // Hearts
                    arrow.getWorld().spawnParticle(Particle.HEART, arrow.getLocation(), 1, 0, 0, 0, 0.1);

                } else if (affect == AffectType.CHAINMAIL && !arrow.isOnGround() && !arrow.isInWater()) {
                    // Drops web items
                    ItemStack cobwebStack = new ItemStack(Material.COBWEB);
                    Item cobweb = arrow.getWorld().dropItem(arrow.getLocation(), cobwebStack);
                    cobweb.setPickupDelay(Integer.MAX_VALUE);
                    Vector randomDirection = generateRandomDirection().normalize().multiply(0.2);
                    cobweb.setVelocity(randomDirection);
                    slowItems.add(cobweb);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (cobweb.isValid()) {
                                cobweb.remove();
                                slowItems.remove(cobweb);
                            }
                        }
                    }.runTaskLater(plugin.sirlich.SkillScheme.getInstance(), 40L);
                    handleSlowCollisions();
                } else if (affect == AffectType.LEATHER) {
                    // Black smoke
                    Color dustColor = Color.fromRGB(0, 0, 0);
                    DustOptions dust = new DustOptions(dustColor, 2.0F);
                    arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 2, 0, 0, 0, dust);
                    // Give player speed while arrows are alive
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
                    

                } 
                if (!arrow.isOnGround() && !arrow.isInWater()) {
                    arrow.getWorld().playSound(arrow.getLocation(), Sound.valueOf("BLOCK_LANTERN_HIT"), 1F, 2F);
                }
                
                
            }
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 2L, 2L);
    }

    /**
     * Generates a random direction vector around the player.
     *
     * @return A randomly generated Vector with a uniform distribution.
     */
    private Vector generateRandomDirection() {
        double x = -1 + (2 * Math.random()); // Random value between -1 and 1
        double y = -1 + (2 * Math.random()); // Random value between -1 and 1
        double z = -1 + (2 * Math.random()); // Random value between -1 and 1
        return new Vector(x, y, z); // Unnormalized vector
    }

    private Location findClosestEmptyBlock(Location center, int radius) {
        // We'll search from the center outwards in expanding cubes
        for (int r = 0; r <= radius; r++) {
            // Check all blocks in the current radius
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        // Only check blocks at the current radius (surface of the cube)
                        if (Math.abs(x) == r || Math.abs(y) == r || Math.abs(z) == r) {
                            Block block = center.clone().add(x, y, z).getBlock();
                            if (block.isEmpty()) {
                                return block.getLocation();
                            }
                        }
                    }
                }
            }
        }
        return null; // No empty block found in the radius
    }   

    private void handleSlowCollisions() {
        World world = getRpgPlayer().getPlayer().getWorld();

        new BukkitRunnable() {
            @Override
            public void run() {
                // If all health items are gone, cancel this task
                if (slowItems.isEmpty()) {
                    this.cancel();
                    return;
                }
                for (Item slowItem : new ArrayList<>(slowItems)) {
                    if (!slowItem.isValid()) continue;

                    // Check for nearby entities
                    List<Entity> nearbyEntities = slowItem.getNearbyEntities(0.5, 0.5, 0.5);
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof LivingEntity target && !entity.equals(getRpgPlayer().getPlayer())) {
                            // target.setFireTicks(20); // 1 second of fire
                            if (slowItem.getItemStack().getType() == Material.COBWEB) {
                                // slow
                                // target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                                Location itemLoc = slowItem.getLocation();
                                Block block = itemLoc.getBlock();
                                if (block.isEmpty()) {
                                    BlockUtils.tempPlaceBlock(Material.COBWEB, itemLoc, 40);
                                } else {
                                    // find the closest empty block in a 3x3 cube with the item location centered in the middle
                                    Location closestEmpty = findClosestEmptyBlock(itemLoc, 1);
                                    if (closestEmpty != null) {
                                        BlockUtils.tempPlaceBlock(Material.COBWEB, closestEmpty, 40);
                                    }   
                                }
                                BlockData blockData = Bukkit.getServer().createBlockData(Material.COBWEB);
                                target.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, target.getLocation(), 50, 0.3, 1, 0.3, blockData);
                                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PARROT_IMITATE_SPIDER, 1.5F, 2.0F);
                                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CROP_BREAK, 0.9F, 1.0F);
                            }
                            
                            slowItem.remove();
                            slowItems.remove(slowItem);

                            // Optional: Play particle effect on the target
                            world.spawnParticle(Particle.SMOKE, target.getLocation(), 130, 0.1, 0.5, 0.1, 0);
                            break;
                        }
                    }
                }

            }   
            
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 0L, 1L);

        
    }

    private void handleHealing(Entity ent) {
        ItemStack mushroomStack = new ItemStack(Material.RED_MUSHROOM_BLOCK);
        Item redMushroom = ent.getWorld().dropItem(ent.getLocation(), mushroomStack);
        redMushroom.setPickupDelay(Integer.MAX_VALUE); // Prevent pickup by players
        Vector randomDirection = generateRandomDirection().normalize().multiply(0.2);
        redMushroom.setVelocity(randomDirection); // Add a slight velocity
        healthItems.add(redMushroom);

        // Schedule the removal of the blaze powder after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (redMushroom.isValid()) {
                    redMushroom.remove();
                    healthItems.remove(redMushroom);
                }
            }
        }.runTaskLater(plugin.sirlich.SkillScheme.getInstance(), 100L); // 5 seconds (100 ticks)
        // Check for collisions between blaze powder and nearby entities
        handleRedMushroomCollisions();
    }

    private void handleRedMushroomCollisions() {
        World world = getRpgPlayer().getPlayer().getWorld();

        new BukkitRunnable() {
            @Override
            public void run() {
                // If all health items are gone, cancel this task
                if (healthItems.isEmpty()) {
                    this.cancel();
                    return;
                }
                for (Item redMushroom : new ArrayList<>(healthItems)) {
                    if (!redMushroom.isValid()) continue;

                    // Check for nearby entities
                    List<Entity> nearbyEntities = redMushroom.getNearbyEntities(0.5, 0.5, 0.5);
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof LivingEntity target) {
                            // target.setFireTicks(20); // 1 second of fire
                            target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 2));
                            BlockData blockData = Bukkit.getServer().createBlockData(Material.RED_MUSHROOM_BLOCK);
                            target.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, target.getLocation(), 50, 0.3, 1, 0.3, blockData);
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.5F, 2.0F);
                            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.9F, 1.0F);
                            redMushroom.remove();
                            healthItems.remove(redMushroom);

                            // Optional: Play particle effect on the target
                            world.spawnParticle(Particle.HEART, target.getLocation(), 2, 0.1, 0.5, 0.1, 0);
                            break;
                        }
                    }
                }

            }   
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 20L, 1L);

        
    }

    private void spawnRandomFirework(Location location) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        
        // Randomize firework effects
        Random random = new Random();
        int effectCount = 1 + random.nextInt(3); // 1-3 effects per firework
        
        for (int i = 0; i < effectCount; i++) {
            FireworkEffect.Builder builder = FireworkEffect.builder();
            
            // Random type
            FireworkEffect.Type[] types = FireworkEffect.Type.values();
            builder.with(types[random.nextInt(types.length)]);
            
            // Random colors (1-3 colors)
            List<Color> colors = new ArrayList<>();
            int colorCount = 1 + random.nextInt(3);
            for (int c = 0; c < colorCount; c++) {
                colors.add(Color.fromRGB(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                ));
            }
            builder.withColor(colors);
            
            // Random fade colors (0-2 colors)
            if (random.nextBoolean()) {
                List<Color> fades = new ArrayList<>();
                int fadeCount = random.nextInt(3);
                for (int f = 0; f < fadeCount; f++) {
                    fades.add(Color.fromRGB(
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256))
                    );
                }
                builder.withFade(fades);
            }
            
            // Random flicker and trail
            builder.flicker(random.nextBoolean());
            builder.trail(random.nextBoolean());
            
            meta.addEffect(builder.build());
        }
        
        // Random power (1-3)
        meta.setPower(1 + random.nextInt(3));
        firework.setFireworkMeta(meta);
        
        // Detonate immediately (since we're at hit location)
        Bukkit.getScheduler().runTaskLater(SkillScheme.getInstance(), () -> {
            firework.detonate();
        }, 1L);
    }
}
