package plugin.sirlich.skills.clans.chestplate;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.RageSkill;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.Particle;

public class Agility extends RageSkill {

    public Agility(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"Agility");
    }

    private int duration;
    private int amplifier;
    private AffectType affect = AffectType.NONE;

    @Override
    public void initData(){
        super.initData();
        this.duration = data.getInt("duration");
        this.amplifier = data.getInt("amplifier");
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isAxe(getRpgPlayer().getPlayer().getItemInHand());
    }

    @Override
    public void onEnrage(){
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 1);
        getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));
        if (affect == AffectType.LEATHER) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, amplifier));
            getRpgPlayer().getPlayer().spawnParticle(Particle.GLOW, getRpgPlayer().getPlayer().getLocation(), 12, 0.7, 0.3, 0.7, 0.1);
        } else if (affect == AffectType.IRON) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, duration, 0));
            getRpgPlayer().getPlayer().spawnParticle(Particle.CRIT, getRpgPlayer().getPlayer().getLocation(), 12, 0.7, 0.3, 0.7, 0.1);
        } else if (affect == AffectType.CHAINMAIL) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration, amplifier));
            getRpgPlayer().getPlayer().spawnParticle(Particle.SMOKE, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.GOLD) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 1));
            getRpgPlayer().getPlayer().spawnParticle(Particle.END_ROD, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.DIAMOND) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, amplifier));
            getRpgPlayer().getPlayer().spawnParticle(Particle.POOF, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.NETHERITE) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 1));
            getRpgPlayer().getPlayer().spawnParticle(Particle.FLAME, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.ELYTRA) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration, amplifier));
            getRpgPlayer().getPlayer().spawnParticle(Particle.CLOUD, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        }
    }

    @Override
    public void onRageExpire(){
        getRpgPlayer().getPlayer().removePotionEffect(PotionEffectType.SPEED);
        if (affect == AffectType.LEATHER) {
            getRpgPlayer().getPlayer().spawnParticle(Particle.GLOW, getRpgPlayer().getPlayer().getLocation(), 12, 0.7, 0.3, 0.7, 0.1);
        } else if (affect == AffectType.IRON) {
            getRpgPlayer().getPlayer().spawnParticle(Particle.CRIT, getRpgPlayer().getPlayer().getLocation(), 12, 0.7, 0.3, 0.7, 0.1);
        } else if (affect == AffectType.CHAINMAIL) {
            getRpgPlayer().getPlayer().spawnParticle(Particle.SMOKE, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.GOLD) {
            getRpgPlayer().getPlayer().spawnParticle(Particle.END_ROD, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.DIAMOND) {
            getRpgPlayer().getPlayer().spawnParticle(Particle.POOF, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.NETHERITE) {
            getRpgPlayer().getPlayer().spawnParticle(Particle.FLAME, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        } else if (affect == AffectType.ELYTRA) {
            getRpgPlayer().getPlayer().spawnParticle(Particle.CLOUD, getRpgPlayer().getPlayer().getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
        }
    }

    @Override
    public void onAxeRightClick(Trigger event){
        attemptRage();
    }

    @Override
    public void onLeftClick(Trigger event){
        endRageEarly();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        endRageEarly();
    }

    @Override
    public void onMeleeAttackSelf(EntityDamageByEntityEvent event){
        if(isEnraged() && getRpgPlayer().getPlayer().isSprinting()){
            if(RpgPlayer.isRpgPlayer(event.getDamager().getUniqueId())){
                RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(event.getDamager().getUniqueId());
                rpgPlayer.tell(data.xliff("that_player_is_using_agility"));
                rpgPlayer.playSound(data.getSound("that_player_is_using_agility"));
            }
            event.setCancelled(true);
        }
    }
}
