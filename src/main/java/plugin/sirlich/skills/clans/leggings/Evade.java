package plugin.sirlich.skills.clans.leggings;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.CooldownSkill;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.util.Vector;
import org.bukkit.World;
import plugin.sirlich.skills.meta.AffectType;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Evade extends CooldownSkill {
    private int schedularID;
    private int blockCount = 0;
    private boolean wasBlocking = false;
    private AffectType affect = AffectType.NONE;

    public Evade(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer, level,"Evade");
    }

    public Location calculateBehindLocation(Player player, LivingEntity target){
        // return target.getLocation().add(target.getLocation().getDirection().normalize().multiply(-1));
        // Get direction behind target (reverse their look direction)
        Vector behindDirection = target.getLocation().getDirection().normalize().multiply(-1);
        
        // Calculate position 1 block behind target at their current Y level
        Location behindLoc = target.getLocation().add(behindDirection);
        
        // Find safe Y position (either current level or first solid block below)
        World world = target.getWorld();
        double startY = target.getLocation().getY(); // Start at target's Y level
        
        // Check if current position is safe (not inside a block)
        if (!world.getBlockAt(behindLoc).getType().isSolid()) {
            // Search downward for first solid block
            for (int y = (int) Math.floor(startY); y > 0; y--) {
                behindLoc.setY(y);
                if (world.getBlockAt(behindLoc).getType().isSolid()) {
                    // Place player on top of this block
                    behindLoc.setY(y + 1);
                    break;
                }
            }
        } else {
            // If we're inside a block, search upward instead
            for (int y = (int) Math.ceil(startY); y < world.getMaxHeight(); y++) {
                behindLoc.setY(y);
                if (!world.getBlockAt(behindLoc).getType().isSolid()) {
                    // Found air, place player here
                    break;
                }
            }
        }
        
        // Calculate direction to face the target
        Vector faceDirection = target.getLocation().toVector().subtract(behindLoc.toVector()).normalize();
        Location lookAt = target.getLocation().clone();
        lookAt.setDirection(faceDirection);
        
        // Set player's rotation to face target
        behindLoc.setYaw(lookAt.getYaw());
        behindLoc.setPitch(lookAt.getPitch());
        
        return behindLoc;
    }

    public void onMeleeAttackSelf(EntityDamageByEntityEvent event){
        if(skillCheck()){return;}
        if(getRpgPlayer().getPlayer().isBlocking()){
            wasBlocking = false;
            blockCount = 0;
            affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 2);
            getRpgPlayer().teleport(calculateBehindLocation(getRpgPlayer().getPlayer(), (LivingEntity) event.getDamager()));
            getRpgPlayer().tell("Like a ninja!");
            event.setDamage(0);
            Player player = getRpgPlayer().getPlayer();
            if (affect == AffectType.NETHERITE) {
                // Self fire res
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 5, 1));
            } else if (affect == AffectType.DIAMOND) {
                // Self res
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 5, 1));
            } else if (affect == AffectType.GOLD) {
                // Self regen
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 1));
            } else if (affect == AffectType.CHAINMAIL) {
                // Self absorption
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 1));
            } else if (affect == AffectType.IRON) {
                // Self strength
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 5, 1));
            } else if (affect == AffectType.LEATHER) {
                // Self speed
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 1));
            }
        }
    }

    public void missEvade(){
        getRpgPlayer().tell("You missed Evade");
        wasBlocking = false;
        blockCount = 0;
        refreshCooldown();
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isSword(getRpgPlayer().getPlayer().getItemInHand());
    }

    @Override
    public void onEnable(){
        schedularID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SkillScheme.getInstance(), new Runnable() {
            public void run() {
                if(getRpgPlayer().getPlayer().isBlocking()){
                    if(skillCheck());
                    blockCount ++;
                    wasBlocking = true;
                    if(blockCount > 20){
                        missEvade();
                    }
                } else {
                    if(wasBlocking){
                        missEvade();
                    }
                }
            }
        }, 0L, 1);
    }

    @Override
    public void onDisable(){
        Bukkit.getServer().getScheduler().cancelTask(schedularID);
    }
}
