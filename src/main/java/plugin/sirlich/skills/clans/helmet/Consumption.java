package plugin.sirlich.skills.clans.helmet;

import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.Skill;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.skills.meta.AffectType;

public class Consumption extends Skill {

    private int duration; // ticks
    private int tiers; 
    private AffectType affect = AffectType.NONE;

    public Consumption(RpgPlayer rpgPlayer, int level) {
        super(rpgPlayer,level,"Consumption");
        this.duration = data.getInt("duration");
        this.tiers = data.getInt("tiers");
    }

    public void onMeleeAttackOther(EntityDamageByEntityEvent event) {
        if(isSilenced()){return;};
        // Check if the entity hurt has no health or another death metric
        LivingEntity victim = (LivingEntity) event.getEntity();
        Player attacker = (Player) event.getDamager();
        // Check if victim is dead after attack
        BlockData blockData = Bukkit.createBlockData(Material.CRIMSON_HYPHAE);
        victim.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, victim.getLocation(), 50, 0.69, 1, 0.69, 0, blockData);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!victim.isValid() || victim.isDead() || victim.getHealth() <= 0) {
                    applyStrengthEffect(attacker);
                }
            }
        }.runTaskLater(SkillScheme.getInstance(), 1);
        // If killed by player then check the player if they have strength 1 through tiers value
        // Whichever lowest strength tier they don't have apply that tier value for duration
    }

    public void onSwordMeleeAttackOther(EntityDamageByEntityEvent event) {
        onMeleeAttackOther(event);
    }
    public void onAxeMeleeAttackOther(EntityDamageByEntityEvent event) {
        onMeleeAttackOther(event);
    }
    public void onBowMeleeAttackOther(EntityDamageByEntityEvent event) {
        onMeleeAttackOther(event);
    }

    private void applyStrengthEffect(Player player) {
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 0);
        // Find the lowest missing Strength tier
        int currentStrengthLevel = 0;
        int curDuration = 0;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.STRENGTH)) {
                currentStrengthLevel = effect.getAmplifier() + 1;
                curDuration = effect.getDuration();
                break;
            }
        }
        // BlockData blockData = Bukkit.createBlockData(Material.COARSE_DIRT); // Gets the block type data
    
        // world.spawnParticle(Particle.BLOCK_CRUMBLE, stepLocation, 13, 0.69, 0.05, 0.69, 0, blockData);
        // Apply the lowest missing tier if below the max tier
        if (currentStrengthLevel < tiers) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                duration + curDuration, 
                currentStrengthLevel // Apply next available strength tier
            ));
        }
        
        if (affect == AffectType.NETHERITE) {
            BlockData blockData = Bukkit.createBlockData(Material.ANCIENT_DEBRIS);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        } else if (affect == AffectType.DIAMOND) {
            BlockData blockData = Bukkit.createBlockData(Material.GOLD_BLOCK);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        } else if (affect == AffectType.IRON) {
            BlockData blockData = Bukkit.createBlockData(Material.ANVIL);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        } else if (affect == AffectType.GOLD) {
            BlockData blockData = Bukkit.createBlockData(Material.RED_MUSHROOM_BLOCK);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        } else if (affect == AffectType.CHAINMAIL) {
            BlockData blockData = Bukkit.createBlockData(Material.COBWEB);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        } else if (affect == AffectType.LEATHER) {
            BlockData blockData = Bukkit.createBlockData(Material.HAY_BLOCK);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        } else if (affect == AffectType.TURTLE) {
            BlockData blockData = Bukkit.createBlockData(Material.TURTLE_EGG);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        } else {
            BlockData blockData = Bukkit.createBlockData(Material.PLAYER_HEAD);
            player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 0.69, 0.2, 0.69, 0, blockData);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0F, 1.0F);
    }
}
