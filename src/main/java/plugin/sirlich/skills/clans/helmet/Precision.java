package plugin.sirlich.skills.clans.helmet;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.Skill;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.block.data.BlockData;
import org.bukkit.Particle;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
public class Precision extends Skill {
    /*
    bonus_damage: double
     */

    public Precision(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "Precision");
    }
    private AffectType affect = AffectType.NONE;

    @Override
    public void onBowFire(EntityShootBowEvent event){
        if(isSilenced()){return;};
        RpgProjectile rpgProjectile = RpgProjectile.getProjectile(event.getProjectile().getUniqueId());
        rpgProjectile.addTag("PRECISION");
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 0);
        Arrow arrow = (Arrow) event.getEntity();
        new BukkitRunnable() {
            
            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    this.cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(Particle.INSTANT_EFFECT, arrow.getLocation(), 1, 0, 0, 0, 0);

            }
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 2L, 2L);
    }

    @Override
    public void onArrowHitEntity(EntityDamageByEntityEvent event){
        RpgProjectile rpgProjectile = RpgProjectile.getProjectile(event.getDamager().getUniqueId());
        if(rpgProjectile.hasTag("PRECISION")){
            event.setDamage(event.getDamage() + data.getDouble("bonus_damage"));//ENTITY_EXPERIENCE_ORB_PICKUP
            Player player = getRpgPlayer().getPlayer();
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
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 2.0F);
        }
    }
}
