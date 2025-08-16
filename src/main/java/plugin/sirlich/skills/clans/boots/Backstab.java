package plugin.sirlich.skills.clans.boots;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.ClassType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import plugin.sirlich.skills.meta.Skill;
import plugin.sirlich.skills.meta.AffectType;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Item;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;
import org.bukkit.Particle;
import org.bukkit.Material;

public class Backstab extends Skill {
    public Backstab(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "Backstab");
    }

    private int acceptable_angle;
    private double damage_modifier;
    private double rogue_damage_reduction; //like 0.8 to do 80% normal damage.
    private Sound backstab_sound;
    private AffectType affect = AffectType.NONE;
    private final List<Item> blazePowders = new ArrayList<>();

    public void initData(){
        super.initData();
        this.acceptable_angle = data.getInt("acceptable_angle");
        this.damage_modifier = data.getDouble("damage_modifier");
        this.backstab_sound = data.getSound("backstab_sound");
        this.rogue_damage_reduction = data.getDouble("rogue_damage_reduction");
        if (acceptable_angle < 0 || acceptable_angle > 180 || acceptable_angle < 1) {
            System.out.println("Not acceptable angle");
        }
    }


    public void onSwordMeleeAttackOther(EntityDamageByEntityEvent event){
        if(isSilenced()){return;};
        Player player = getRpgPlayer().getPlayer();
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        if (!(victim instanceof LivingEntity)) {
            return;
        }
        LivingEntity targetEntity = (LivingEntity) victim;
        // Calculate vectors
        // Attacker's direction (normalized)
        Vector attackerDirection = player.getLocation().getDirection().normalize();

        // Vector from the target to the attacker (normalized)
        Vector targetToAttacker = player.getLocation().toVector()
                .subtract(targetEntity.getLocation().toVector())
                .normalize();

        // Target's back direction (normalized)
        Vector targetBackDirection = targetEntity.getLocation().getDirection().normalize().multiply(-1);

        // Check if the attacker is within the acceptable angle
        Vector attackerDirection2D = attackerDirection.clone().setY(0).normalize();
        Vector targetBackDirection2D = targetBackDirection.clone().setY(0).normalize();
        double angleToBack = attackerDirection2D.angle(targetBackDirection2D); // Angle between attacker and target's back
        double angleToTarget = attackerDirection.angle(targetToAttacker); // Angle between attacker and target itself

        // Convert acceptable angle to radians for comparison
        double acceptableAngleRadians = Math.toRadians(acceptable_angle);
        //Behind Player
        // player.sendMessage("Angle to back: "+angleToBack+" and acceptableAngleRadians: "+acceptableAngleRadians);
        if (angleToBack >= acceptableAngleRadians) {
            // Apply extra damage if within the angle
            event.setDamage(event.getDamage() + damage_modifier);
            
            // Apply affect
            affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 3);
            if (affect == AffectType.CHAINMAIL) {
                // glowing
                targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 1));
            }
            else if (affect == AffectType.IRON) {
                // Increase damage by half heart
                event.setDamage(event.getDamage() + 2);
            }
            else if (affect == AffectType.DIAMOND) {
                targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 0));
            }
            else if (affect == AffectType.NETHERITE) {
                // Spawn fire items
                handleNetherite(targetEntity);
            }
            else if (affect == AffectType.GOLD) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 20, 0));
            }
            else if (affect == AffectType.LEATHER) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
            }
            
            getRpgPlayer().playWorldSound(backstab_sound);

            // Additional reduction for Rogues
            if (targetEntity instanceof Player attackedPlayer) {
                RpgPlayer rpgDamaged = RpgPlayer.getRpgPlayer(targetEntity.getUniqueId());
                if (rpgDamaged.getClassType() == ClassType.BREAST) {
                    event.setDamage(event.getDamage() * rogue_damage_reduction);
                }
            }
        }
    }

    private void handleNetherite(LivingEntity victim) {
        ItemStack blazePowderStack = new ItemStack(Material.BLAZE_POWDER);
        Item blazePowder = getRpgPlayer().getPlayer().getWorld().dropItem(victim.getLocation(), blazePowderStack);
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
    
        // Check for collisions between blaze powder and nearby entities
        handleBlazePowderCollisions();
    }

    private void handleBlazePowderCollisions() {
        World world = getRpgPlayer().getPlayer().getWorld();

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
