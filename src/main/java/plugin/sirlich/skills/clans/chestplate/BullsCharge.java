package plugin.sirlich.skills.clans.chestplate;

import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.RageSkill;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;

public class BullsCharge extends RageSkill {
    private static String id = "BullsCharge";
    private static List<Double> slownessPower = getYaml(id).getDoubleList("values.slownessPower");
    private static List<Integer> slownessDuration = getYaml(id).getIntegerList("values.slownessDuration");
    private static List<Double> speedPower = getYaml(id).getDoubleList("values.speedPower");
    private AffectType affect = AffectType.NONE;


    public BullsCharge(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"BullsCharge");
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isAxe(getRpgPlayer().getPlayer().getItemInHand());
    }

    @Override
    public void onEnrage(){
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 1);
        getRpgPlayer().editWalkSpeedModifier(speedPower.get(getLevel()));
    }

    @Override
    public void onRageExpire(){
        getRpgPlayer().editWalkSpeedModifier(-speedPower.get(getLevel()));
    }

    public void onAxeMeleeAttackOther(EntityDamageByEntityEvent event){
        if(isEnraged()){
            endRageEarly();
            UUID uuid = event.getEntity().getUniqueId();
            if(RpgPlayer.isRpgPlayer(uuid)){
                final RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(uuid);

                //Slow the target down!
                rpgPlayer.editWalkSpeedModifier(-slownessPower.get(getLevel()));
                
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        //Remove the slowdown effect
                        rpgPlayer.editWalkSpeedModifier(+slownessPower.get(getLevel()));
                    }
                }.runTaskLater(SkillScheme.getInstance(), slownessDuration.get(getLevel()));
            }
            if (event.getEntity() instanceof LivingEntity target) {
                if (target.equals(getRpgPlayer().getPlayer())) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDuration.get(getLevel()), (int)(1.3+slownessPower.get(getLevel())*2.2)));
                }
                if (affect == AffectType.LEATHER) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                    target.getWorld().spawnParticle(Particle.GLOW, target.getLocation(), 12, 0.7, 0.3, 0.7, 0.1);
                } else if (affect == AffectType.IRON) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                    target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 12, 0.7, 0.3, 0.7, 0.1);
                } else if (affect == AffectType.CHAINMAIL) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 0));
                    target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
                } else if (affect == AffectType.GOLD) {
                    List<Entity> nearbyEntities = target.getNearbyEntities(5.0, 5.0, 5.0);
                    for (Entity entity : nearbyEntities) {
                        // if (event.getEntity() != entity) {
                        if (entity instanceof LivingEntity adjacent) {
                            adjacent.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 20, 0));
                            adjacent.getWorld().spawnParticle(Particle.END_ROD, target.getLocation(), 22, 0.2, 2, 0.2, 0);
                            adjacent.getWorld().spawnParticle(Particle.ENCHANTED_HIT, target.getLocation(), 22, 0.2, 2, 0.2, 0);
                            adjacent.getWorld().spawnParticle(Particle.HEART, target.getLocation(), 5, 0.8, 0.4, 0.8, 0.1);
                            adjacent.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation(), 20, 0.8, 0.2, 0.7, 0.8);
                        }
                        // }
                    }
                    // target.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 30, 1));
                    target.getWorld().spawnParticle(Particle.CHERRY_LEAVES, target.getLocation(), 20, 2.5, 0.3, 2.5, 0.1);
                } else if (affect == AffectType.DIAMOND) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 1));
                    target.getWorld().spawnParticle(Particle.POOF, target.getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
                } else if (affect == AffectType.NETHERITE) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60, 0));
                    target.setFireTicks(30);
                    target.getWorld().spawnParticle(Particle.FLAME, target.getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
                } else if (affect == AffectType.ELYTRA) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 9));
                    target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 12, 0.3, 0.9, 0.7, 0.1);
                }       
            }
        }
    }

    @Override
    public void onAxeRightClick(Trigger event){
        attemptRage();
    }
}
