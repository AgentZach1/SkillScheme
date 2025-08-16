package plugin.sirlich.skills.clans.helmet;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.TickingSkill;
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

public class HuntersThrill extends TickingSkill {

    /*
    duration_on_hit: int
    max_charges: int
    minus_charges: int
    base_millis: int
    per_level_millis: int

    Xliff:
    skill_expired

    Sounds:
    skill_expired
     */
    private int charges;
    private long lastHit;

    public HuntersThrill(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "HuntersThrill");
    }
    private AffectType affect = AffectType.NONE;


    @Override
    public void onArrowHitEntity(EntityDamageByEntityEvent event){
        LivingEntity hitEntity = (LivingEntity) event.getEntity();
        RpgProjectile rpgArrow = RpgProjectile.getProjectile((Arrow) event.getDamager());
        if(rpgArrow.hasTag("HUNTERS_THRILL")){
            lastHit = System.currentTimeMillis();
            if(charges < data.getInt("max_charges")){
                charges++;
            }

            getRpgPlayer().getPlayer().removePotionEffect(PotionEffectType.SPEED);
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, data.getInt("duration_on_hit"), charges - data.getInt("minus_charges")));
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
            player.playSound(player.getLocation(), Sound.ENTITY_RABBIT_DEATH, 2.0F, 1.0F);
        }
    }

    @Override
    public void onTick(){
        if (charges != 0 && System.currentTimeMillis() >= lastHit + (data.getInt("base_millis") + data.getInt("per_level_millis"))) {
            charges = 0;
            getRpgPlayer().getPlayer().removePotionEffect(PotionEffectType.SPEED);
        }
    }

    @Override
    public void onBowFire(EntityShootBowEvent event){
        if(isSilenced()){return;};
        Arrow arrow = (Arrow) event.getProjectile();
        RpgProjectile.addTag(arrow.getUniqueId(),"HUNTERS_THRILL");
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 0);
        new BukkitRunnable() {
            
            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    this.cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(Particle.COMPOSTER, arrow.getLocation(), 1, 0, 0, 0, 1);

            }
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 2L, 2L);
    }

}
