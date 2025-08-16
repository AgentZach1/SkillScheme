package plugin.sirlich.skills.clans.helmet;

import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.ChargeSkill;
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
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;

public class Overcharge extends ChargeSkill {
    /*
    damage_per_charge
     */
    public Overcharge(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "Overcharge", false, true);
    }
    private AffectType affect = AffectType.NONE;

    @Override
    public boolean isCharging(){
        return getRpgPlayer().isDrawingBow() && getRpgPlayer().isBowFullyCharged();
    }

    @Override
    public void onBowFire(EntityShootBowEvent event){
        if(isSilenced()){return;};
        if(isCharging()){
            affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 0);
            RpgProjectile rpgProjectile = RpgProjectile.getProjectile(event.getProjectile().getUniqueId());
            rpgProjectile.addTag("OVERCHARGE");
            rpgProjectile.setInt("OVERCHARGE_VALUE", getCharges());
            Arrow arrow = (Arrow) event.getProjectile();
            new BukkitRunnable() {
                Color dustColor = Color.fromRGB(255, 0, 0);
                DustOptions dust = new DustOptions(dustColor, 3.0F);
                    
                @Override
                public void run() {
                    if (arrow == null || arrow.isDead() || !arrow.isValid()) {
                        this.cancel();
                        return;
                    }
                    arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 2, 0, 0, 0, dust);

                }
            }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 2L, 2L);
        }
    }

    @Override
    public void onArrowHitEntity(EntityDamageByEntityEvent event){
        RpgProjectile rpgArrow = RpgProjectile.getProjectile((Arrow) event.getDamager());
        if(rpgArrow.hasTag("OVERCHARGE")){
            int charges = rpgArrow.getInt("OVERCHARGE_VALUE");
            // System.out.println("Overcharge: " + event.getDamage());
            event.setDamage(event.getDamage()  + (charges * data.getDouble("damage_per_charge")));
            LivingEntity hitEntity = (LivingEntity) event.getEntity();
            // System.out.println("Overcharge: new " + event.getDamage());
            Player player = getRpgPlayer().getPlayer();
            if (affect == AffectType.NETHERITE) {
                BlockData blockData = Bukkit.createBlockData(Material.ANCIENT_DEBRIS);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            } else if (affect == AffectType.DIAMOND) {
                BlockData blockData = Bukkit.createBlockData(Material.GOLD_BLOCK);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            } else if (affect == AffectType.IRON) {
                BlockData blockData = Bukkit.createBlockData(Material.ANVIL);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            } else if (affect == AffectType.GOLD) {
                BlockData blockData = Bukkit.createBlockData(Material.RED_MUSHROOM_BLOCK);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            } else if (affect == AffectType.CHAINMAIL) {
                BlockData blockData = Bukkit.createBlockData(Material.COBWEB);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            } else if (affect == AffectType.LEATHER) {
                BlockData blockData = Bukkit.createBlockData(Material.HAY_BLOCK);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            } else if (affect == AffectType.TURTLE) {
                BlockData blockData = Bukkit.createBlockData(Material.TURTLE_EGG);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            } else {
                BlockData blockData = Bukkit.createBlockData(Material.PLAYER_HEAD);
                player.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, player.getLocation(), 50, 2.5, 0.2, 2.5, 0, blockData);
            }
            for (LivingEntity entity : player.getWorld().getLivingEntities()) {
                if (entity.getLocation().distance(player.getLocation()) <= 5 && entity != player) {
                    if (affect == AffectType.NETHERITE) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 1));
                    } else if (affect == AffectType.DIAMOND) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
                    } else if (affect == AffectType.IRON) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));
                    } else if (affect == AffectType.GOLD) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                    } else if (affect == AffectType.CHAINMAIL) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                    } else if (affect == AffectType.LEATHER) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
                    } else if (affect == AffectType.TURTLE) {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1));
                    } else {
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100, 0));
                    }
                }
            }

            
            player.playSound(player.getLocation(), Sound.BLOCK_BEEHIVE_WORK, 3.0F, 1.0F);

        }
    }
}
