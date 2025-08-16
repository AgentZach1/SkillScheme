package plugin.sirlich.skills.clans.chestplate;

import org.bukkit.event.player.PlayerDropItemEvent;
import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.CooldownSkill;
import plugin.sirlich.utilities.BlockUtils;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.World;
import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import plugin.sirlich.skills.triggers.Trigger;


public class IcePrison extends CooldownSkill
{
    private final List<Item> trackedProjectiles = new ArrayList<>();
    private boolean deployed = false;
    private int radius;
    private int duration;
    private AffectType affect = AffectType.NONE;

    public IcePrison(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"IcePrison");
        this.radius = data.getInt("sphere_radius"); // Radius of the sphere
        this.duration = data.getInt("duration"); // Duration for which the blocks last
    }

    @Override
    public void onAxeRightClick(Trigger event){
        if(isSilenced()){return;};
        if(skillCheck()){return;}
        Player player = getRpgPlayer().getPlayer();
        affect = WeaponUtils.getAffectFromArmorSlot(player, 1);
        
        ItemStack iceModel = new ItemStack(Material.PACKED_ICE);
        if (affect == AffectType.NETHERITE) {
            iceModel = new ItemStack(Material.BASALT);
        }
        Item iceProjectile = player.getWorld().dropItem(player.getEyeLocation(), iceModel);
        iceProjectile.setPickupDelay(Integer.MAX_VALUE); // Prevent players from picking it up
        if (affect == AffectType.DIAMOND) {
            iceProjectile.setVelocity(player.getLocation().getDirection().multiply(2)); // Set its velocity
        } else {
            iceProjectile.setVelocity(player.getLocation().getDirection().multiply(1.5)); // Set its velocity
        }
        // iceProjectile.setVelocity(player.getLocation().getDirection().multiply(1.5)); // Set its velocity

        // Register the projectile with the RpgProjectile system
        RpgProjectile.registerProjectileItem(iceProjectile, getRpgPlayer());
        RpgProjectile rpgIceProjectile = RpgProjectile.getProjectileItem(iceProjectile);
        rpgIceProjectile.addTag("ICEPRISON");

        // Runnable 1: Check proximity for collision
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!iceProjectile.isValid() || iceProjectile.isDead()) {
                    cancel();
                    return;
                }
                if (affect == AffectType.NETHERITE) {
                    iceProjectile.getWorld().spawnParticle(Particle.FLAME, iceProjectile.getLocation(), 1, 0, 0, 0, 0);
                } else {
                    iceProjectile.getWorld().spawnParticle(Particle.SNOWFLAKE, iceProjectile.getLocation(), 1, 0, 0, 0, 0.1);
                }
                // iceProjectile.getWorld().spawnParticle(Particle.SNOWFLAKE, iceProjectile.getLocation(), 1, 0, 0, 0, 0.1);
                iceProjectile.getWorld().playSound(iceProjectile.getLocation(), data.getSound("live_sound"), 1F, 2F);
                // Check for nearby entities
                double collisionRadius = 1.0; // Adjust collision radius
                for (Entity nearby : iceProjectile.getNearbyEntities(collisionRadius, collisionRadius, collisionRadius)) {
                    if (nearby instanceof LivingEntity targetEntity) {
                        if (!targetEntity.equals(player)) {
                            // Trigger the ice prison and stop tracking the projectile
                            player.playSound(player.getLocation(), data.getSound("hit_enemy"), 3F, 0.5F);
                            if (affect == AffectType.CHAINMAIL) {
                                // give targetEntity slow
                                targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3));
                            } else if (affect == AffectType.LEATHER) {
                                // give player speed
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1));
                            } else if (affect == AffectType.GOLD) {
                                // give targetEntity regen
                                targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1));
                            } else if (affect == AffectType.IRON) {
                                // deal a heart of damage to targetEntity
                                targetEntity.damage(2, getRpgPlayer().getPlayer());
                            } else if (affect == AffectType.ELYTRA) {
                                targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
                            } 
                            
                            placeIceBlocks(targetEntity.getLocation(), iceProjectile);

                            iceProjectile.remove();
                            cancel(); // Stop this runnable
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(SkillScheme.getInstance(), 0L, 1L); // Run every tick

        // Schedule the block placement after 2 seconds if the projectile doesn't hit
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iceProjectile.isValid() && !iceProjectile.isDead()) {
                    placeIceBlocks(iceProjectile.getLocation(), iceProjectile);
                    iceProjectile.remove();
                }
            }
        }.runTaskLater(SkillScheme.getInstance(), 40L); // 2 seconds (20 ticks per second)
        // Create a sphere of FROSTED_ICE around the player
        // player.sendMessage("Ice prison cause");
        // deployed = true;
        // BlockUtils.getNearbyBlocks(player.getLocation(), radius).stream()
        //             .filter(block -> block.getType() == Material.AIR || (block.getType() == Material.WATER && block.getBlockData() instanceof Levelled levelledWater && levelledWater.getLevel() == 0)) // Only replace air blocks
        //             .filter(block -> isOnSphereSurface(block.getLocation(), player.getLocation(), radius))
        //             .filter(block -> !isDirectlyAboveCenter(block.getLocation(), player.getLocation()))
        //             .forEach(block -> {
        //                 BlockUtils.tempPlaceBlock(Material.FROSTED_ICE, block.getLocation(), duration);
        //                 runEffects(block.getLocation(), player);
        //             });
        // player.sendMessage("Ice prison placed blocks");
        // new BukkitRunnable() {

        //     @Override
        //     public void run() {
        //         deployed = false;
        //     }

        // }.runTaskLater(SkillScheme.getInstance(), data.getInt("duration"));

        refreshCooldown();
    }

    /**
     * Handle when the projectile hits an entity or the ground.
     */
    @Override
    public void onProjectileHitGround(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Item item)) {
            return;
        }

        // Check if the fireball is tagged as ICEPRISON
        RpgProjectile rpgItem = RpgProjectile.getProjectileItem(item);
        if (rpgItem.hasTag("ICEPRISON")) {
            placeIceBlocks(item.getLocation(), item);
            item.remove();
            // rpgProjectile.deregisterSelf();
        }
    }

    @Override
    public void onProjectileHitEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Item item)) {
            return;
        }
        
        // Check if the fireball is tagged as FIREBLAST
        RpgProjectile rpgItem = RpgProjectile.getProjectileItem(item);
        if (rpgItem == null || !rpgItem.hasTag("ICEPRISON")) {
            System.out.println("Not ice prison");
            return;
        }
        Entity hitEntity = event.getEntity();
        if (hitEntity instanceof LivingEntity livingEntity) {
            if (rpgItem.hasTag("ICEPRISON")) {
            placeIceBlocks(hitEntity.getLocation(), item);
            item.remove();
            // rpgProjectile.deregisterSelf();
        }
        }
        
    }

    /**
     * Place blocks around the location with particle effects.
     */
    private void placeIceBlocks(Location location, Item proj) {
        deployed = true;
        if (affect == AffectType.NETHERITE) {
            BlockUtils.getNearbyBlocks(location, radius).stream()
                .filter(block -> block.getType() == Material.AIR || (block.getType() == Material.WATER && block.getBlockData() instanceof Levelled levelledWater && levelledWater.getLevel() == 0)) // Only replace air blocks
                .filter(block -> isOnSphereSurface(block.getLocation(), location, radius))
                .filter(block -> !isDirectlyAboveCenter(block.getLocation(), location))
                .forEach(block -> {
                    BlockUtils.tempPlaceBlock(Material.WAXED_EXPOSED_COPPER_GRATE, block.getLocation(), duration);
                    runEffects(block.getLocation(), proj);
                });
        } else {
            BlockUtils.getNearbyBlocks(location, radius).stream()
                .filter(block -> block.getType() == Material.AIR || (block.getType() == Material.WATER && block.getBlockData() instanceof Levelled levelledWater && levelledWater.getLevel() == 0)) // Only replace air blocks
                .filter(block -> isOnSphereSurface(block.getLocation(), location, radius))
                .filter(block -> !isDirectlyAboveCenter(block.getLocation(), location))
                .forEach(block -> {
                    BlockUtils.tempPlaceBlock(Material.FROSTED_ICE, block.getLocation(), duration);
                    runEffects(block.getLocation(), proj);
                });
        }
        

        // player.sendMessage("Ice prison placed blocks");
        new BukkitRunnable() {
            @Override
            public void run() {
                deployed = false;
            }
        }.runTaskLater(SkillScheme.getInstance(), duration);
    }

    /**
     * Check if a block is on the surface of a sphere.
     * 
     * @param blockLocation The location of the block being checked.
     * @param center The center of the sphere.
     * @param radius The radius of the sphere.
     * @return True if the block is on the surface; otherwise false.
     */
    private boolean isOnSphereSurface(Location blockLocation, Location center, int radius) {
        double distanceSquared = blockLocation.toVector().distanceSquared(center.toVector());
        double radiusSquared = radius * radius;
        // return distanceSquared >= radiusSquared - 6 && distanceSquared <= radiusSquared + 6;
        double thickness = 2.5; // The range of thickness
        return distanceSquared >= radiusSquared - thickness * radius && 
            distanceSquared <= radiusSquared + thickness * radius;
        // return Math.abs(distanceSquared - radiusSquared) <= radius;
    }

    /**
     * Check if a block is directly above or below the center.
     *
     * @param blockLocation The location of the block being checked.
     * @param center The center of the sphere.
     * @return True if the block is directly above the center; otherwise false.
     */
    private boolean isDirectlyAboveCenter(Location blockLocation, Location center) {
        return blockLocation.getBlockX() == center.getBlockX() &&
            blockLocation.getBlockZ() == center.getBlockZ() &&
            blockLocation.getBlockY() > center.getBlockY(); // Adjust as needed for below
    }

    private void runEffects(Location blockLocation, Item proj) {
        if (affect == AffectType.NETHERITE) {
            proj.getWorld().spawnParticle(Particle.DRIPPING_LAVA, blockLocation, 2, 0.5, 1, 0.5, 0.1);
        } else {
            proj.getWorld().spawnParticle(Particle.SNOWFLAKE, blockLocation, 2, 0.5, 1, 0.5, 0.1);
        }
        
        proj.getWorld().playSound(blockLocation, data.getSound("prison_set"), 1F, 2F);
    }

    // @Override
    // public void onSwordDrop(PlayerDropItemEvent event){
    //     if(skillCheck()){return;}
    //     Player player = getRpgPlayer().getPlayer();
    //     deployed = true;
    //     BlockUtils.tempPlaceBlock(Material.GLASS,player.getLocation(),data.getInt("duration"));
    //     BlockUtils.tempPlaceBlock(Material.GLASS,player.getLocation().add(new Vector(0,1,0)),data.getInt("duration"));
    //     new BukkitRunnable() {

    //         @Override
    //         public void run() {
    //             deployed = false;
    //         }

    //     }.runTaskLater(SkillScheme.getInstance(), data.getInt("duration"));
    //     refreshCooldown();
    // }
    // @Override
    // public void onAxeDrop(PlayerDropItemEvent event){
    //     if(skillCheck()){return;}
    //     Player player = getRpgPlayer().getPlayer();
    //     deployed = true;
    //     BlockUtils.tempPlaceBlock(Material.GLASS,player.getLocation(),data.getInt("duration"));
    //     BlockUtils.tempPlaceBlock(Material.GLASS,player.getLocation().add(new Vector(0,1,0)),data.getInt("duration"));
    //     new BukkitRunnable() {

    //         @Override
    //         public void run() {
    //             deployed = false;
    //         }

    //     }.runTaskLater(SkillScheme.getInstance(), data.getInt("duration"));
    //     refreshCooldown();
    // }


    @Override
    public void onSuffocationDamageSelf(EntityDamageEvent event){
        if(deployed){
            event.setDamage(0);
        }
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isAxe(getRpgPlayer().getPlayer().getItemInHand());
    }

}
