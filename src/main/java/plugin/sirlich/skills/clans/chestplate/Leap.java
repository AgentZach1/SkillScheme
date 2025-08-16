package plugin.sirlich.skills.clans.chestplate;

import plugin.sirlich.skills.meta.CooldownSkill;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.triggers.Trigger;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.utilities.BlockUtils;
import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import java.util.List;
import org.bukkit.block.Block;

public class Leap extends CooldownSkill
{
    private static List<Double> power = getYaml("Leap").getDoubleList("values.power");
    private AffectType affect = AffectType.NONE;
    private final List<Item> blazePowders = new ArrayList<>();
    private final List<Item> healthItems = new ArrayList<>();
    private final List<Item> slowItems = new ArrayList<>();

    public Leap(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"Leap");
    }

    @Override
    public void onAxeRightClick(Trigger event){
        if(isSilenced()){return;};
        if(skillCheck()){return;}
        Player self = event.getSelf();
        getRpgPlayer().getPlayer().getWorld().playSound(self.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.2F, 2.0F);
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 1);
        Vector direction = self.getLocation().getDirection().normalize();
        if (affect == AffectType.DIAMOND) {
            Vector leap = direction.multiply(power.get(getLevel())+0.3);
            self.setVelocity(leap);
        } else if (affect == AffectType.ELYTRA) {
            Vector leap = direction.multiply(power.get(getLevel()) + 0.8);
            self.setVelocity(leap);
        }else {
            Vector leap = direction.multiply(power.get(getLevel()));
            self.setVelocity(leap);
        }

        // self.setVelocity(new Vector(self.getLocation().getDirection().multiply(power.get(getLevel())).getX(), 0.4, self.getLocation().getDirection().multiply(power.get(getLevel())).getZ()));
        
        if (affect == AffectType.LEATHER) {
            // give speed to player
            self.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
            self.getWorld().spawnParticle(Particle.SWEEP_ATTACK, self.getLocation(), 2, 0.3, 0.1, 0.3, 1);
        } else if (affect == AffectType.CHAINMAIL) {
            // apply slowness to nearby living entities not player
            handleChain();
            // List<Entity> nearby = self.getNearbyEntities(3.0, 3.0, 3.0);
            // for (Entity ent : nearby) {
            //     if (ent instanceof LivingEntity target && !ent.equals(self)) {
            //         target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            //         target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 130, 0.3, 1, 0.3, 0.1);
            //     }
            // }
        } else if (affect == AffectType.GOLD) {
            // leave behind healing items
            handleHealing();
        } else if (affect == AffectType.IRON) {
            // give user slow fall for a 
            self.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 1));
        } else if (affect == AffectType.NETHERITE) {
            // Leave behind fire items
            handleNetherite();
        }
        refreshCooldown();
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isAxe(getRpgPlayer().getPlayer().getItemInHand());
    }

    private void handleNetherite() {
        for (int z = 0; z < 6; z++) {
            ItemStack blazePowderStack = new ItemStack(Material.BLAZE_POWDER);
            Item blazePowder = getRpgPlayer().getPlayer().getWorld().dropItem(getRpgPlayer().getPlayer().getLocation(), blazePowderStack);
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
        }
        
    
        // Check for collisions between blaze powder and nearby entities
        handleBlazePowderCollisions();
    }

    private void handleBlazePowderCollisions() {
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

    private void handleHealing() {
        for (int z = 0; z < 6; z++) {
            ItemStack mushroomStack = new ItemStack(Material.RED_MUSHROOM_BLOCK);
            Item redMushroom = getRpgPlayer().getPlayer().getWorld().dropItem(getRpgPlayer().getPlayer().getLocation(), mushroomStack);
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
        }
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

    private void handleChain() {
        for (int z = 0; z < 4; z++) {
            ItemStack cobwebStack = new ItemStack(Material.COBWEB);
            ItemStack heavyCoreStack = new ItemStack(Material.HEAVY_CORE);
            Item cobweb = getRpgPlayer().getPlayer().getWorld().dropItem(getRpgPlayer().getPlayer().getLocation(), cobwebStack);
            Item core = getRpgPlayer().getPlayer().getWorld().dropItem(getRpgPlayer().getPlayer().getLocation(), heavyCoreStack);
            cobweb.setPickupDelay(Integer.MAX_VALUE); // Prevent pickup by players
            core.setPickupDelay(Integer.MAX_VALUE);
            Vector randomDirection = generateRandomDirection().normalize().multiply(0.2);
            cobweb.setVelocity(randomDirection); // Add a slight velocity
            randomDirection = generateRandomDirection().normalize().multiply(0.2);
            core.setVelocity(randomDirection); // Add a slight velocity
            slowItems.add(cobweb);
            slowItems.add(core);

            // Schedule the removal of the items after 5 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (cobweb.isValid()) {
                        cobweb.remove();
                        slowItems.remove(cobweb);
                    }
                    if (core.isValid()) {
                        core.remove();
                        slowItems.remove(core);
                    }
                }
            }.runTaskLater(plugin.sirlich.SkillScheme.getInstance(), 100L); // 5 seconds (100 ticks)
        }
        // Check for collisions between blaze powder and nearby entities
        handleSlowCollisions();
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
                            if (slowItem.getItemStack().getType() == Material.HEAVY_CORE) {
                                // even slower
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                                BlockData blockData = Bukkit.getServer().createBlockData(Material.HEAVY_CORE);
                                target.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, target.getLocation(), 50, 0.3, 1, 0.3, blockData);
                                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_HORSE_LAND, 1.5F, 1.0F);
                                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CREAKING_HEART_BREAK, 0.9F, 0.5F);
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
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 0L, 1L);

        
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
}
