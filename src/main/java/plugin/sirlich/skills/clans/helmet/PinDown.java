package plugin.sirlich.skills.clans.helmet;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.CooldownSkill;
import plugin.sirlich.skills.triggers.Trigger;
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

public class PinDown extends CooldownSkill
{
    /*
    arrow_velocity: double
    slowness_duration: int
    slowness_amplifier: int
     */
    public PinDown(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level, "PinDown");
    }

    private AffectType affect = AffectType.NONE;


    @Override
    public void onArrowHitGround(ProjectileHitEvent event){
        RpgProjectile rpgArrow = RpgProjectile.getProjectile(event.getEntity().getUniqueId());
        if(rpgArrow.hasTag("PIN_DOWN")){
            event.getEntity().remove();
        }
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isBow(getRpgPlayer().getPlayer().getItemInHand());
    }

    @Override
    public void onArrowHitEntity(EntityDamageByEntityEvent event){
        // System.out.println("inside PinDown");
        Entity hitEntity = event.getEntity();
        RpgProjectile rpgArrow = RpgProjectile.getProjectile((Arrow) event.getDamager());
        if(hitEntity instanceof LivingEntity && rpgArrow.hasTag("PIN_DOWN")){
            LivingEntity livingEntity = (LivingEntity) hitEntity;
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, data.getInt("slowness_duration"),data.getInt("slowness_amplifier")),true);

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
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_SCULK_SPREAD, 5.0F, 1.0F);
        }
    }

    @Override
    public void onBowLeftClick(Trigger event){
        if(skillCheck()){return;}
        Arrow arrow = event.getSelf().launchProjectile(Arrow.class);
        arrow.setVelocity(arrow.getVelocity().multiply(data.getDouble("arrow_velocity")));
        RpgProjectile.registerProjectile(arrow,RpgPlayer.getRpgPlayer(event.getSelf()));
        RpgProjectile rpgArrow = RpgProjectile.getProjectile(arrow.getUniqueId());
        rpgArrow.addTag("PIN_DOWN");
        refreshCooldown();
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 0);
        new BukkitRunnable() {
            
            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                    this.cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(Particle.WHITE_SMOKE, arrow.getLocation(), 1, 0, 0, 0, 0);

            }
        }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 2L, 2L);
    }
}
