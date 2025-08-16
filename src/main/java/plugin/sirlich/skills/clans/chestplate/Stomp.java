package plugin.sirlich.skills.clans.chestplate;

import plugin.sirlich.SkillScheme;
import plugin.sirlich.skills.meta.CooldownSkill;
import plugin.sirlich.skills.meta.PrimedSkill;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.triggers.Trigger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import java.util.List;
import java.util.EnumSet;
import java.util.Set;
import javax.xml.stream.events.EntityDeclaration;

public class Stomp extends PrimedSkill
{
    private static List<Double> power = getYaml("Stomp").getDoubleList("values.power");
    private static List<Double> radius = getYaml("Stomp").getDoubleList("values.radius");
    private static List<Double> cooldown = getYaml("Stomp").getDoubleList("values.cooldown");
    private AffectType affect = AffectType.NONE;

    public Stomp(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"Stomp");
    }

    @Override
    public void onAxeRightClick(Trigger event){
        if(isSilenced()){return;};
        if(skillCheck()){return;}
        Player self = event.getSelf();
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 1);
        getRpgPlayer().playSound(Sound.ENTITY_ZOMBIE_HURT, 1.0F, 2.0F);
        double angleRad = Math.toRadians(69);
        Vector direction = self.getLocation().getDirection().normalize();
        
        double horizontalPower = Math.cos(angleRad) * power.get(getLevel());
        double verticalPower = power.get(getLevel());

        Vector leap = new Vector(direction.getX() * horizontalPower, verticalPower, direction.getZ() * horizontalPower);
        attemptPrime();

        // self.setVelocity(new Vector(self.getLocation().getDirection().multiply(power.get(getLevel())).getX(), 0.4, self.getLocation().getDirection().multiply(power.get(getLevel())).getZ()));
        self.setVelocity(leap);
        refreshCooldown();
        handleNoDmg();
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isAxe(getRpgPlayer().getPlayer().getItemInHand());
    }

    public void handleNoDmg() {
        int cooldownTicks = (int) Math.round(cooldown.get(getLevel()));
        // Schedule a task to execute when cooldown expires
        new BukkitRunnable() {
            @Override
            public void run() {
                if (primed) { // Only execute if still primed (wasn't triggered by damage)
                    primed = false;
                    doPound(null, getRpgPlayer().getPlayer().isOnGround());
                }
            }
        }.runTaskLater(SkillScheme.getInstance(), cooldownTicks);
    }

    public void onFallDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || primed == false) return;
        primed = false;
        doPound(event, getRpgPlayer().getPlayer().isOnGround());
    }

    public void onDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || primed == false) return;
        // List of damage causes that should NOT trigger the pound
        Set<EntityDamageEvent.DamageCause> ignoredCauses = EnumSet.of(
            EntityDamageEvent.DamageCause.POISON,
            EntityDamageEvent.DamageCause.WITHER,
            EntityDamageEvent.DamageCause.STARVATION,
            EntityDamageEvent.DamageCause.DROWNING,
            EntityDamageEvent.DamageCause.FREEZE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.SUFFOCATION,
            EntityDamageEvent.DamageCause.MAGIC,
            EntityDamageEvent.DamageCause.THORNS
        );
        
        // If the damage cause is in our ignored list, return without triggering
        if (ignoredCauses.contains(event.getCause())) {
            return;
        }
        primed = false;
        doPound(event, getRpgPlayer().getPlayer().isOnGround());
    }

    private void doPound(EntityDamageEvent event, boolean onGround) {
        Player self;
        if (event != null) {
            event.setCancelled(true);
            self = (Player) event.getEntity();
        }
        self = getRpgPlayer().getPlayer();
        // int radius = getYaml("Stomp").getInt("radius"); // Pulls radius from config
        Location center = self.getLocation();
        World world = self.getWorld();

        // Create dust particles at impact site
        if (onGround) { // or time has run out like the ability has reset its cooldown
            for (int i = 0; i < 40; i++) {
                double angle = 2 * Math.PI * i / 30; // Distribute evenly in a circle
                double x = Math.cos(angle) * radius.get(getLevel());
                double z = Math.sin(angle) * radius.get(getLevel());
                Location particleLoc = center.clone().add(x, 0.1, z);
    
                Block block = world.getBlockAt(particleLoc);
                Material material = block.getType();
                BlockData blockData = Bukkit.createBlockData(material); // Gets the block type data
                BlockData blockData2 = Bukkit.createBlockData(Material.COARSE_DIRT); // Gets the block type data
    
                world.spawnParticle(Particle.BLOCK_CRUMBLE, particleLoc, 10, 0.2, 0.1, 0.2, 0, blockData2);
    
                world.spawnParticle(Particle.BLOCK_CRUMBLE, particleLoc, 50, 0.2, 0.1, 0.2, 0, blockData);
                self.getWorld().playSound(center, Sound.BLOCK_DEEPSLATE_BRICKS_BREAK, 3.09F, 1.01F);
            }
        }
        else {
            for (int i = 0; i < 40; i++) {
                double angle = 2 * Math.PI * i / 30; // Distribute evenly in a circle
                double x = Math.cos(angle) * radius.get(getLevel());
                double z = Math.sin(angle) * radius.get(getLevel());
                Location particleLoc = center.clone().add(x, 0.1, z);
    
                // Block block = world.getBlockAt(particleLoc);
                // Material material = block.getType();
                BlockData blockData = Bukkit.createBlockData(Material.COARSE_DIRT); // Gets the block type data
    
                world.spawnParticle(Particle.BLOCK_CRUMBLE, particleLoc, 60, 0.2, 0.1, 0.2, 0, blockData);
                self.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 3.09F, 1.32F);
            }
        }

        for (int i = 0; i < 80; i++) { // Increase number for denser effect
            double r = Math.sqrt(Math.random()) * radius.get(getLevel()); // Random radius (√ for uniform density)
            double angle = Math.random() * 2 * Math.PI; // Random angle
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            Location stepLocation = center.clone().add(x, 0.05, z);
    
            world.spawnParticle(Particle.ASH, stepLocation, 13, 0.69, 0.05, 0.69, 0);
            BlockData blockData = Bukkit.createBlockData(Material.COARSE_DIRT); // Gets the block type data
    
            world.spawnParticle(Particle.BLOCK_CRUMBLE, stepLocation, 13, 0.69, 0.05, 0.69, 0, blockData);
        }
        

        // Knockback entities in radius
        for (Entity entity : world.getNearbyEntities(center, radius.get(getLevel()), 2, radius.get(getLevel()))) {
            if (entity instanceof LivingEntity && entity != self) {
                LivingEntity target = (LivingEntity) entity;

                Vector direction = target.getLocation().toVector().subtract(center.toVector());
                double distance = target.getLocation().distance(center); // Get distance from center
                double affectIntensity = 3.5;
                
                double intensity = Math.max(0.5, (1 - (distance / radius.get(getLevel())))) * affectIntensity; // Scale knockback
        
                double knockbackAngle = Math.toRadians(45);
                
                // if (affect == AffectType.DIAMOND) {
                //     Vector knockbackVelocity = new Vector(
                //         knockbackDirection.getX() * Math.cos(knockbackAngle) * intensity,
                //         (knockbackDirection.getY() + 2.1), // Dynamic upward launch based on proximity
                //         knockbackDirection.getZ() * Math.cos(knockbackAngle) * intensity
                //     );
                //     target.setVelocity(knockbackVelocity.multiply(power.get(getLevel()))); // Scale force by power
                // } else {
                //     Vector knockbackVelocity = new Vector(
                //         knockbackDirection.getX() * Math.cos(knockbackAngle) * intensity,
                //         (knockbackDirection.getY() + 1.3), // Dynamic upward launch based on proximity
                //         knockbackDirection.getZ() * Math.cos(knockbackAngle) * intensity
                //     );
                //     target.setVelocity(knockbackVelocity.multiply(power.get(getLevel()))); // Scale force by power
                // }

                double verticalBoost = affect == AffectType.DIAMOND ? 2.1 : 1.3;
                verticalBoost *= affectIntensity * affectIntensity;
                Vector knockback = new Vector(
                    direction.getX(),
                    verticalBoost, // Stronger, scaled vertical component
                    direction.getZ() 
                );
                
                target.setVelocity(knockback);
                
                if (affect == AffectType.CHAINMAIL) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                } else if (affect == AffectType.ELYTRA) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 3));
                } else if (affect == AffectType.GOLD) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 10));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
                }
                
                if (affect == AffectType.IRON) {
                    target.damage(intensity * 2, self);
                } else {
                    target.damage(intensity * 0.75, self); // Apply some damage
                }
                self.getWorld().playSound(center, Sound.ENTITY_IRON_GOLEM_ATTACK, 3.09F, 1.8F);
            }
        }
        if (affect == AffectType.LEATHER) {
            self.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        } else if (affect == AffectType.NETHERITE) {
            self.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 1));
        }
        
        
    }
}
