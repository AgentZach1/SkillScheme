package plugin.sirlich.skills.clans.helmet;

import org.bukkit.event.entity.EntityShootBowEvent;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.PrimedSkill;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Arrow;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class IncendiaryShot extends PrimedSkill {
    public IncendiaryShot(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"IncendiaryShot");
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isBow(getRpgPlayer().getPlayer().getItemInHand());
    }

    @Override
    public void onBowLeftClick(Trigger event){
        attemptPrime();
    }

    @Override
    public void onBowFire(EntityShootBowEvent event){
        if(isSilenced()){return;};
        if(primed){
            primed = false;
            event.getProjectile().setFireTicks(data.getInt("burn_duration"));
            refreshCooldown();
            Arrow arrow = (Arrow) event.getProjectile();
            new BukkitRunnable() {
                
                @Override
                public void run() {
                    if (arrow == null || arrow.isDead() || !arrow.isValid() || arrow.isOnGround()) {
                        this.cancel();
                        return;
                    }
                    arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 2, 0, 0, 0, 0);
                    arrow.getWorld().playSound(arrow.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.0F, 2.0F);
                }
            }.runTaskTimer(plugin.sirlich.SkillScheme.getInstance(), 2L, 2L);
        }
    }
}
