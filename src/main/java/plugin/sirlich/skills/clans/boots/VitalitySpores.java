package plugin.sirlich.skills.clans.boots;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.TickingSkill;
import plugin.sirlich.utilities.Color;
import plugin.sirlich.skills.meta.AffectType;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.Particle;

public class VitalitySpores extends TickingSkill {
    /*
    regeneration_duration: int, ticks
    regeneration_amplifier: int, ticks
    safe_duration: int, ticks

    Sounds:
    now_healing

     */
    private boolean healing = false;
    private AffectType affect = AffectType.NONE;

    public VitalitySpores(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "VitalitySpores");
    }

    @Override
    public void onTick(){
        //Player hasn't been damaged in a good long while.
        final int TICKS_TO_MILLIS = 50;
        if(isSilenced()){return;};
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 3);

        if(!healing && System.currentTimeMillis() > (data.getDouble("safe_duration") * TICKS_TO_MILLIS) + getRpgPlayer().getLastDamaged()){
            healing = true;
            getRpgPlayer().tell(Color.green + getName() + Color.dgray + " are now active.");
            getRpgPlayer().playSound(data.getSound("now_healing"), 0.5f);
            getRpgPlayer().getPlayer().spawnParticle(Particle.CHERRY_LEAVES, getRpgPlayer().getPlayer().getLocation(), 2, 0.2, 0.1, 0.2, 0.1);
        }

        if(healing){
            getRpgPlayer().getPlayer().spawnParticle(Particle.HEART, getRpgPlayer().getPlayer().getLocation(), 1, 0.2, 1, 0.2, 0.1);
            getRpgPlayer().getPlayer().spawnParticle(Particle.CHERRY_LEAVES, getRpgPlayer().getPlayer().getLocation(), 2, 0.2, 0.1, 0.2, 0.5);
            if (getRpgPlayer().getPlayer().getPotionEffect(PotionEffectType.REGENERATION) == null) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, data.getInt("regeneration_duration"),data.getInt("regeneration_amplifier")));
            }
                
            if (affect == AffectType.CHAINMAIL) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 120, 1));
            }
            else if (affect == AffectType.IRON) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 0));
            }
            else if (affect == AffectType.DIAMOND) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20, 1));
            }
            else if (affect == AffectType.NETHERITE) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0));
            }
            else if (affect == AffectType.GOLD) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 40, 1));
            }
            else if (affect == AffectType.LEATHER) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
            }
        
        }
        else {
            getRpgPlayer().getPlayer().spawnParticle(Particle.SPORE_BLOSSOM_AIR, getRpgPlayer().getPlayer().getLocation(), 2, 0.7, 0.9, 0.7, 0);
        }
    }

    @Override
    public void onDamageSelf(EntityDamageEvent event){
        if(healing){
            healing = false;
            getRpgPlayer().getPlayer().removePotionEffect(PotionEffectType.REGENERATION);
            getRpgPlayer().tell(Color.red + getName() + Color.dgray + " is no longer active.");
        }
    }
}
