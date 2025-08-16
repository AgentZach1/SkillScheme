package plugin.sirlich.skills.clans.leggings;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.ChargeSkill;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.skills.meta.AffectType;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Color;

public class WolfPounce extends ChargeSkill {
    public WolfPounce(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "WolfPounce", true, true);
    }

    private double base_power;
    private double power_per_charge;
    private double y_velocity_bias;
    private AffectType affect = AffectType.NONE;

    @Override
    public void initData(){
        this.base_power = data.getDouble("base_power");
        this.power_per_charge = data.getDouble("power_per_charge");
        this.y_velocity_bias = data.getDouble("y_velocity_bias");
    }

    @Override
    public boolean isCharging(){
        return getRpgPlayer().getPlayer().isBlocking() && getRpgPlayer().getPlayer().isOnGround();
    }

    public void onSwordRightClick(Trigger event){
        if(isSilenced()){return;};
        if(getCharges() == 0 && isCooldownNoMedia()){
            playCooldownMedia();
        }
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isSword(getRpgPlayer().getPlayer().getItemInHand());
    }

    @Override
    public void onReleaseCharge(int charges, boolean isFullyCharged){
        if(isSilenced()){return;};
        Player player = getRpgPlayer().getPlayer();

        Vector vel = player.getLocation().getDirection().normalize();
        double power = base_power + (power_per_charge * charges);
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 2);
        if (affect == AffectType.DIAMOND) {
            power = power + 0.6;
        } else if (affect == AffectType.NETHERITE) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 1));
        } else if (affect == AffectType.IRON) {
            Color dustColor = Color.fromRGB(159, 173, 172);
            DustOptions dust = new DustOptions(dustColor, 3.0F);
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 50, 1.5, 0.4, 1.5, 0, dust);
            for (LivingEntity entity : player.getWorld().getLivingEntities()) {
                if (entity.getLocation().distance(player.getLocation()) <= 3 && entity != player) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                    dustColor = Color.fromRGB(225, 224, 48);
                    entity.getWorld().spawnParticle(Particle.ENTITY_EFFECT, entity.getLocation(), 30, 0.2, 1, 0.2, 0, dustColor);
                }
            }
        } else if (affect == AffectType.GOLD) {
            player.getWorld().spawnParticle(Particle.CHERRY_LEAVES, player.getLocation(), 50, 3, 1, 3, 0);
            for (LivingEntity entity : player.getWorld().getLivingEntities()) {
                if (entity.getLocation().distance(player.getLocation()) <= 6 && entity != player) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                    entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 5, 0.2, 1, 0.2, 0);
                }
            }
        } else if (affect == AffectType.CHAINMAIL) {
            player.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, player.getLocation(), 50, 1.5, 0.4, 1.5, 0);
            for (LivingEntity entity : player.getWorld().getLivingEntities()) {
                if (entity.getLocation().distance(player.getLocation()) <= 3 && entity != player) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 1));
                    entity.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, entity.getLocation(), 30, 0.2, 1, 0.2, 0);
                }
            }
        } else if (affect == AffectType.LEATHER) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        }
        player.setVelocity(vel.multiply(power).multiply(new Vector(1.0, y_velocity_bias, 1.0)));
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 2.0F, 1.5F);
        
    }
}
