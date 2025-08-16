package plugin.sirlich.skills.clans.helmet;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.PrimedSkill;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.utilities.VelocityUtils;
import plugin.sirlich.utilities.WeaponUtils;

public class RopedArrow extends PrimedSkill {
    public RopedArrow(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level, "RopedArrow");
    }

    @Override
    public void onArrowHitEntity(EntityDamageByEntityEvent event){
        Projectile projectile = (Projectile) event.getDamager();
        Location location = projectile.getLocation();
        Vector power = projectile.getVelocity();
        RpgProjectile rpgProjectile = RpgProjectile.getProjectile(projectile.getUniqueId());
        if(rpgProjectile.hasTag("ROPED_ARROW")){
            handleRopedArrow(location, power, projectile);
        }
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isBow(getRpgPlayer().getPlayer().getItemInHand());
    }

    @Override
    public void onArrowHitGround(ProjectileHitEvent event){
        Projectile projectile = event.getEntity();
        Location location = projectile.getLocation();
        Vector power = projectile.getVelocity();
        RpgProjectile rpgProjectile = RpgProjectile.getProjectile(event.getEntity().getUniqueId());
        if(rpgProjectile.hasTag("ROPED_ARROW")){
            handleRopedArrow(location, power, projectile);
        }
    }

    public void handleRopedArrow(Location location, Vector power, Projectile projectile){
        Player player = getRpgPlayer().getPlayer();

        // Calculate trajectory vector (direction from player to arrow)
        Vector vec = VelocityUtils.getTrajectory(player.getLocation(), location);

        // Scale based on the directional vector's magnitude (fixed strength scaling)
        // double mult = vec.length(); // Proportional to the direction vector
        // double verticalBoost = Math.abs(location.getY() - player.getLocation().getY()) * 0.5D; // Vertical adjustment
        // player.sendMessage("Mult: " + mult); 
        // // Add the vertical boost to the trajectory
        // vec.setY(vec.getY() + verticalBoost);

        // // Normalize the vector for consistent direction
        // vec.normalize();

        // Apply velocity to the player
        VelocityUtils.velocity(player, vec,
                2.5D, false, 0.0D, 1.3D, 1.5D, true);

        // Play sound effect
        projectile.getWorld().playSound(projectile.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.5F, 2.0F);
    }

    @Override
    public void onBowLeftClick(Trigger event){
        attemptPrime();
    }

    @Override
    public void onBowFire(EntityShootBowEvent event){
        if(isSilenced()){return;};
        if(primed){
            Arrow arrow = (Arrow) event.getProjectile();
            primed = false;
            RpgProjectile.addTag(arrow.getUniqueId(),"ROPED_ARROW");
            refreshCooldown();
        }
    }

}
