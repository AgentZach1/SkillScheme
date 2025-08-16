package plugin.sirlich.skills.clans.boots;

import org.bukkit.event.player.PlayerDropItemEvent;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.ManaSkill;
import plugin.sirlich.utilities.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.Particle;
import org.bukkit.Location;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import plugin.sirlich.skills.meta.AffectType;

public class FlamingGrieves extends ManaSkill
{
    private static String id = "FlamingGrieves";
    private final List<Item> blazePowders = new ArrayList<>();
    private AffectType affect = AffectType.NONE;

    public FlamingGrieves(RpgPlayer rpgPlayer, int level) {
        super(rpgPlayer, level, "FlamingGrieves");
    }

    @Override
    public void onTick() {
        if(isSilenced()){return;};
        RpgPlayer player = getRpgPlayer();
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 3);
        
        if (affect == AffectType.DIAMOND || affect == AffectType.NETHERITE) {
            player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 0));
        }
        else if (affect == AffectType.GOLD) {
            if (getRpgPlayer().getPlayer().getPotionEffect(PotionEffectType.REGENERATION) == null) {
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1));
            }
        }
        else if (affect == AffectType.LEATHER) {
            player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
        }

        player.getPlayer().getWorld().spawnParticle(Particle.FLAME, player.getPlayer().getLocation(), 1, 0.0F, 1.0F, 0.00F, 0.1);
        player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 1));
        getRpgPlayer().getPlayer().setFireTicks(0);
        player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), data.getSound("live_sound"), 0.8F, 0F);
        // Check if the player is in lava
        if (player.getPlayer().getLocation().getBlock().getType() == Material.LAVA) {
            enhanceSwimmingInLava(player);
        }
        
        // Drop 3 blaze powder items
        for (int i = 0; i < 3; i++) {
            ItemStack blazePowderStack = new ItemStack(Material.BLAZE_POWDER);
            Item blazePowder = player.getPlayer().getWorld().dropItem(player.getPlayer().getLocation(), blazePowderStack);
            blazePowder.setPickupDelay(Integer.MAX_VALUE); // Prevent pickup by players
            Vector randomDirection = generateRandomDirection().normalize().multiply(0.2);
            blazePowder.setVelocity(randomDirection); // Add a slight velocity
            blazePowders.add(blazePowder);

            // Schedule the removal of the blaze powder after 5 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (blazePowder.isValid()) {
                        blazePowder.remove();
                        blazePowders.remove(blazePowder);
                    }
                }
            }.runTaskLater(plugin.sirlich.SkillScheme.getInstance(), 100L); // 5 seconds (100 ticks)
        }

        // Check for collisions between blaze powder and nearby entities
        handleBlazePowderCollisions();
    
    }

    private void handleBlazePowderCollisions() {
        World world = getRpgPlayer().getPlayer().getWorld();

        for (Item blazePowder : new ArrayList<>(blazePowders)) {
            if (!blazePowder.isValid()) continue;

            // Check for nearby entities
            List<Entity> nearbyEntities = blazePowder.getNearbyEntities(1.0, 1.0, 1.0);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity target && !entity.equals(getRpgPlayer().getPlayer())) {
                    // Apply fire ticks to the target
                    target.setFireTicks(60); // 3 seconds of fire
                    if (affect == AffectType.CHAINMAIL) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));
                    }
                    else if (affect == AffectType.IRON) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20, 0));
                    }

                    // Remove the blaze powder
                    blazePowder.remove();
                    blazePowders.remove(blazePowder);

                    // Optional: Play particle effect on the target
                    world.spawnParticle(Particle.FLAME, target.getLocation(), 10, 0.7, 0.9, 0.7, 0);
                    break;
                }
            }
        }
    }

     @Override
    public void onSwordDrop(PlayerDropItemEvent entityEvent){
        toggleStatus();
    }

    @Override
    public void onAxeDrop(PlayerDropItemEvent entityEvent){
        toggleStatus();
    }

    /**
     * Generates a random direction vector around the player.
     *
     * @return A randomly generated Vector with a uniform distribution.
     */
    private Vector generateRandomDirection() {
        double x = -1 + (2 * Math.random()); // Random value between -1 and 1
        double y = -1 + (2 * Math.random()); // Random value between -1 and 1
        double z = -1 + (2 * Math.random()); // Random value between -1 and 1
        return new Vector(x, y, z); // Unnormalized vector
    }

    /**
     * Enhances swimming speed in lava by applying velocity to the player.
     * 
     * @param player The RpgPlayer whose swimming speed is being enhanced.
     */
    private void enhanceSwimmingInLava(RpgPlayer player) {
        Vector direction = player.getPlayer().getLocation().getDirection().normalize();
        Vector lavaBoost = direction.multiply(0.1); // Adjust speed boost as necessary
        lavaBoost.setY(Math.min(lavaBoost.getY() + 0.1, 0.3)); // Prevent excessive upward speed
        player.getPlayer().setVelocity(player.getPlayer().getVelocity().add(lavaBoost));
    }
}