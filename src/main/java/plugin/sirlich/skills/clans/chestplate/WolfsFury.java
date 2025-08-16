package plugin.sirlich.skills.clans.chestplate;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.RageSkill;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;


import java.util.Collection;


public class WolfsFury extends RageSkill {

    private int whiffNum = 0;
    private AffectType affect = AffectType.NONE;
    private int tickToRun = 20;
    private int curTick = 0;

    public WolfsFury(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"WolfsFury");
    }

    @Override
    public void onEnrage(){
        getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, data.getInt("duration"),data.getInt("amplifier")));
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 1);
    }

    public void onMeleeAttackOther(EntityDamageByEntityEvent event){
        if(isEnraged()){
            event.setCancelled(true);
            double damage = event.getDamage() / 2;
            Entity entity = event.getEntity();
            Player itsMe = getRpgPlayer().getPlayer();
            affect = WeaponUtils.getAffectFromArmorSlot(itsMe, 1);
            if(entity instanceof LivingEntity && !entity.equals(getRpgPlayer().getPlayer())){
                LivingEntity livingEntity = (LivingEntity) entity;
                event.setDamage(damage);
                getRpgPlayer().playSound(Sound.ENTITY_WOLF_HURT, 1.5F, 1.5F);
                
                if (affect == AffectType.LEATHER) {
                    // Give user haste and speed per hit
                    int effectTicks = 20;
                    int remainingTicks = 0;
                    int amp = 0;
                    Collection<PotionEffect> activeEffects = itsMe.getActivePotionEffects();
                    PotionEffect fireEffect = itsMe.getPotionEffect(PotionEffectType.SPEED);
                    if (fireEffect != null) {
                        remainingTicks = fireEffect.getDuration();
                        amp = fireEffect.getAmplifier();
                    }
                    itsMe.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectTicks+remainingTicks, 1+amp));
                    remainingTicks = 0;
                    amp = 0;
                    fireEffect = itsMe.getPotionEffect(PotionEffectType.HASTE);
                    if (fireEffect != null) {
                        remainingTicks = fireEffect.getDuration();
                        amp = fireEffect.getAmplifier();
                    }
                    itsMe.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, effectTicks+remainingTicks, 1+amp));
                } else if (affect == AffectType.GOLD) {
                    // Give enemy blindness and glowing
                    int darkTicks = 20;
                    int remainingTicks = 0;
                    int amp = 0;
                    Collection<PotionEffect> activeEffects = livingEntity.getActivePotionEffects();
                    PotionEffect darknessEffect = livingEntity.getPotionEffect(PotionEffectType.DARKNESS);
                    PotionEffect glowEffect = livingEntity.getPotionEffect(PotionEffectType.GLOWING);
                    if (darknessEffect != null) {
                        remainingTicks = darknessEffect.getDuration();
                        amp = darknessEffect.getAmplifier();
                    }
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, darkTicks+remainingTicks, 1+amp));
                    remainingTicks = 0;
                    amp = 0;
                    if (glowEffect != null) {
                        remainingTicks = glowEffect.getDuration();
                        amp = glowEffect.getAmplifier();
                    }
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, darkTicks+remainingTicks, 1+amp));
                } else if (affect == AffectType.CHAINMAIL) {
                    // Deals 0.1 damage every 2 ticks for 20 ticks
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (tickToRun < curTick) {
                                curTick = 0;
                                this.cancel();
                                return;
                            }
                            getRpgPlayer().tell("Dmg start");
                            event.setDamage(damage + curTick);
                            livingEntity.getWorld().spawnParticle(Particle.CRIT, livingEntity.getLocation(), 1, 0.2, 1, 0.2, 1);
                            getRpgPlayer().tell("Dmg end");
                            curTick+=2;
                            
                        }
                    }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 0L, 2L);
                } else if (affect == AffectType.IRON) {
                    // Deal extra damage
                    event.setDamage(damage + 1);
                    getRpgPlayer().playSound(Sound.ENTITY_IRON_GOLEM_STEP, 1.4F, 1.0F);
                } else if (affect == AffectType.DIAMOND) {
                    // Gain health like ur mauling them and eating at the same time
                    double selfHealth = itsMe.getHealth();
                    double maxHealth = itsMe.getMaxHealth();

                    if (selfHealth < maxHealth) {
                        selfHealth+=1;
                    } else {
                        itsMe.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20, 1));
                    }
                    
                } else if (affect == AffectType.NETHERITE) {
                    // Adds fire resistance seconds per hit
                    int resTicks = 20;
                    int remainingTicks = 0;
                    int amp = 0;
                    Collection<PotionEffect> activeEffects = itsMe.getActivePotionEffects();
                    PotionEffect fireEffect = itsMe.getPotionEffect(PotionEffectType.FIRE_RESISTANCE);
                    if (fireEffect != null) {
                        remainingTicks = fireEffect.getDuration();
                        amp = fireEffect.getAmplifier();
                    }
                    itsMe.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, resTicks+remainingTicks, 1+amp));
                } else if (affect == AffectType.ELYTRA) {
                    // Gives enemy slow falling per hit
                    int slowFallTicks = 20;
                    int remainingTicks = 0;
                    int amp = 0;
                    Collection<PotionEffect> activeEffects = livingEntity.getActivePotionEffects();
                    PotionEffect fireEffect = livingEntity.getPotionEffect(PotionEffectType.SLOW_FALLING);
                    if (fireEffect != null) {
                        remainingTicks = fireEffect.getDuration();
                        amp = fireEffect.getAmplifier();
                    }
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, slowFallTicks+remainingTicks, 1+amp));
                }





            }
            
        }
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isAxe(getRpgPlayer().getPlayer().getItemInHand());
    }


    //TODO: Add logic to cancel the rage early (see Agility) if the player whiffs (misses) two attacks in a row.
    @Override
    public void onLeftClick(Trigger event){
        if (isEnraged()) {
            if (whiffNum < 2) {
                whiffNum++;
                getRpgPlayer().playSound(Sound.ENTITY_COD_FLOP, 0.5F, 1.0F);
            } else {
                endRageEarly();
                whiffNum = 0;
                getRpgPlayer().playSound(Sound.ENTITY_DONKEY_ANGRY, 2.0F, 2.5F);
            }
        }
        
        
    }


    @Override
    public void onRageExpire(){
        getRpgPlayer().getPlayer().removePotionEffect(PotionEffectType.STRENGTH);
        getRpgPlayer().playSound(Sound.ENTITY_WOLF_DEATH, 1.0F, 1.5F);
    }

    @Override
    public void onAxeRightClick(Trigger event){
        attemptRage();
    }
}
