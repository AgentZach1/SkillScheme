package plugin.sirlich.skills.clans.helmet;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.TickingSkill;
import plugin.sirlich.utilities.Color;
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
import org.bukkit.Sound;

public class SharpShooter extends TickingSkill {
    /*
    Config values:
    bonus_damage_per_charge: double
    max_charges: int
    base_millis: int
    per_level_millis: int

    xliff:

    sounds:
    on_miss
    on_hit
     */

    private int charges = 0;
    private long lastHit;

    public SharpShooter(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "SharpShooter");
    }
    private AffectType affect = AffectType.NONE;

    public void handleArrowHit(){
        if(charges < data.getInt("max_charges")){
            charges = charges + 1;
            getRpgPlayer().tell(Color.green + getName() + Color.dgray + " charges: " + Color.green + charges);
        }
        getRpgPlayer().getPlayer().playSound(getRpgPlayer().getPlayer().getLocation(), data.getSound("on_hit"),0.4f,2.0f * charges/data.getInt("max_charges"));
    }

    public void resetCharges(){
        if(charges != 0){
            getRpgPlayer().playSound(data.getSound("on_miss"));
            getRpgPlayer().tell(Color.red + getName() + Color.dgray + " charges has been reset.");
        }
        charges = 0;
    }

    @Override
    public void onBowFire(EntityShootBowEvent event){
        if(isSilenced()){return;};
        Arrow arrow = (Arrow) event.getProjectile();
        RpgProjectile.addTag(arrow.getUniqueId(),"SHARP_SHOOTER");
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 0);
        new BukkitRunnable() {
            
            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    this.cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, arrow.getLocation(), 1, 0, 0, 0, 0.1);

            }
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 2L, 2L);
    }

    @Override
    public void onArrowHitEntity(EntityDamageByEntityEvent event){
        lastHit = System.currentTimeMillis();
        Projectile projectile = (Projectile) event.getDamager();
        RpgProjectile rpgProjectile = RpgProjectile.getProjectile(projectile.getUniqueId());
        if(rpgProjectile.hasTag("SHARP_SHOOTER")){
            handleArrowHit();
            event.setDamage(event.getDamage() + charges * data.getDouble("bonus_damage_per_charge"));
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
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
        }
    }

    @Override
    public void onTick(){
        if (System.currentTimeMillis() >= lastHit + (data.getInt("base_millis") + data.getInt("per_level_millis"))) {
            resetCharges();
        }
    }
}
