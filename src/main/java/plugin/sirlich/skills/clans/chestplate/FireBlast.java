package plugin.sirlich.skills.clans.chestplate;

import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.CooldownSkill;
import org.bukkit.entity.Fireball;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import java.util.List;
import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;




public class FireBlast extends CooldownSkill
{
    public FireBlast(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"FireBlast");
    }

    private double fire_duration;
    private double damage;
    private double knockback_strength;
    private double radius;
    private AffectType affect = AffectType.NONE;

    @Override
    public void initData(){
        this.fire_duration = data.getDouble("fire_duration");
        this.damage = data.getDouble("damage");
        this.knockback_strength = data.getDouble("knockback_strength");
        this.radius = data.getDouble("effect_radius");
    }

    @Override
    public void onAxeRightClick(Trigger event){
        if(isSilenced()){return;};
        if(skillCheck()){return;}
        Fireball f = event.getSelf().launchProjectile(Fireball.class);
        f.setIsIncendiary(false);
        f.setYield(data.getDouble("yield").floatValue());

        // Register the fireball in the RpgProjectile system
        RpgProjectile.registerProjectileFireball(f, getRpgPlayer());
        RpgProjectile rpgFireball = RpgProjectile.getProjectileFireball(f);
        rpgFireball.addTag("FIREBLAST");
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 1);
        if (affect == AffectType.LEATHER) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1));
        }
        refreshCooldown();
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isAxe(getRpgPlayer().getPlayer().getItemInHand());
    }

    // Handle when the fireball hits an entity
    @Override
    public void onProjectileHitEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Fireball fireball)) {
            return;
        }
        
        // Check if the fireball is tagged as FIREBLAST
        RpgProjectile rpgFireball = RpgProjectile.getProjectileFireball(fireball);
        if (rpgFireball == null || !rpgFireball.hasTag("FIREBLAST")) {
            // System.out.println("Not fireblast");
            return;
        }
        // System.out.println("Fireblast hit entity");
        // Apply effects to the hit entity
        Entity shooter = fireball.getShooter() instanceof Entity ? (Entity) fireball.getShooter() : null;
        Entity hitEntity = event.getEntity();
        if (hitEntity instanceof LivingEntity livingEntity) {
            if (!hitEntity.equals(shooter)) {
                livingEntity.setFireTicks((int)fire_duration); // Set the entity on fire
                if (affect == AffectType.IRON) {
                    event.setDamage(event.getDamage() + damage + 2);
                }
                else {
                    event.setDamage(event.getDamage() + damage); // Apply damage
                }

                if (affect == AffectType.GOLD) {
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 3));
                }
                else if (affect == AffectType.CHAINMAIL) {
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                }
            }
            else {
                livingEntity.damage(damage/3, getRpgPlayer().getPlayer()); // reduced dmg to self 
                livingEntity.setFireTicks((int)fire_duration/3);
                if (affect == AffectType.NETHERITE) {
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 1));
                }
            }
            
            // System.out.println("Fireblast hit entity apply dmg");
            // Apply moderate knockback
            if (affect == AffectType.DIAMOND) {
                Vector knockback = livingEntity.getLocation().toVector().subtract(fireball.getLocation().toVector()).normalize().multiply(knockback_strength+0.3);
                livingEntity.setVelocity(knockback.setY(knockback.getY() + 0.5));
            } else if (affect == AffectType.ELYTRA) {
                Vector knockback = livingEntity.getLocation().toVector().subtract(fireball.getLocation().toVector()).normalize().multiply(knockback_strength+0.8);
                livingEntity.setVelocity(knockback.setY(knockback.getY() + 0.5));
                // livingEntity.setVelocity(new Vector(0, 0.8, 0))
            }
            else {
                Vector knockback = livingEntity.getLocation().toVector().subtract(fireball.getLocation().toVector()).normalize().multiply(knockback_strength);
                livingEntity.setVelocity(knockback.setY(knockback.getY() + 0.5));
            }   
            runParticles(fireball, fireball.getLocation());
        }
    }

    // Handle when the fireball hits a surface or ground
    @Override
    public void onProjectileHitGround(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) {
            return;
        }

        // Check if the fireball is tagged as FIREBLAST
        RpgProjectile rpgFireball = RpgProjectile.getProjectileFireball(fireball);
        if (rpgFireball == null || !rpgFireball.hasTag("FIREBLAST")) {
            System.out.println("Not fireblast");
            return;
        }
        System.out.println("Fireblast hit ground");
        // Create an explosion effect (visual only) and apply knockback to nearby entities
        Location explosionLocation = fireball.getLocation();
        explosionLocation.getWorld().createExplosion(explosionLocation, 0, false, false);

        // Apply knockback and fire effect to nearby entities
        List<Entity> nearbyEntities = explosionLocation.getWorld().getNearbyEntities(explosionLocation, radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .toList();
        Entity shooter = fireball.getShooter() instanceof Entity ? (Entity) fireball.getShooter() : null;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                // livingEntity.setFireTicks((int)fire_duration);
                // livingEntity.damage(damage);
                if (!entity.equals(shooter)) {
                    livingEntity.setFireTicks((int)fire_duration); // Set the entity on fire
                    livingEntity.damage(damage, getRpgPlayer().getPlayer()); // Apply damage
                }
                else {
                    livingEntity.damage(damage/3, getRpgPlayer().getPlayer()); // reduced dmg to self 
                    livingEntity.setFireTicks((int)fire_duration/3);
                }
                // System.out.println("Fireblast ground Effects");
                Vector knockback = livingEntity.getLocation().toVector().subtract(explosionLocation.toVector()).normalize();
                knockback.multiply(knockback_strength);
                livingEntity.setVelocity(knockback);
            }
        }
        runParticles(fireball, explosionLocation);

        // Remove the fireball from the world
        fireball.remove();
        rpgFireball.deregisterSelf();
    }

    private void runParticles(Fireball fireball, Location location) {
        fireball.getWorld().spawnParticle(Particle.FLAME, location, 30, radius/2, 0.5, radius/2, 0.01);
    }
}
