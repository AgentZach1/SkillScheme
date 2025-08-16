package plugin.sirlich.skills.clans.chestplate;

import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.skills.meta.CooldownSkill;
import plugin.sirlich.utilities.BlockUtils;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.Location;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.World;
import java.util.Random;
// import java.util.Vector;
import org.bukkit.util.Vector;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitRunnable;

import plugin.sirlich.skills.meta.AffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import java.util.List;

import plugin.sirlich.skills.triggers.Trigger;

public class Placetto extends CooldownSkill 
{
    private int width;
    private AffectType affect = AffectType.NONE;
    // private final List<Material> blockOptions = List.of(
    //         Material.STONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
    //         Material.ANDESITE, Material.DIORITE, Material.GRANITE,
    //          Material.DIRT, Material.COARSE_DIRT, Material.COBBLESTONE, Material.COBBLESTONE,
    //         Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
    //         Material.OAK_PLANKS, Material.OAK_PLANKS, Material.JUNGLE_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS
    // );
    private final List<Material> poorOptions = List.of(
        Material.STONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.SLIME_BLOCK, Material.TARGET, Material.HAY_BLOCK,
        Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DIRT, Material.COARSE_DIRT, Material.PACKED_MUD
    );
    private final List<Material> woodOptions = List.of(
        Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG,
        Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
        Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS,
        Material.DARK_OAK_PLANKS
    );

    private final List<Material> stickyOptions = List.of(
        Material.COBWEB, Material.SOUL_SAND, Material.HONEY_BLOCK, Material.IRON_BARS
    );

    private final List<Material> richOptions = List.of(
        Material.OBSIDIAN, Material.ANCIENT_DEBRIS, Material.DEEPSLATE_BRICKS, Material.GILDED_BLACKSTONE, Material.CRYING_OBSIDIAN,
        Material.GLOWSTONE, Material.CHISELED_NETHER_BRICKS, Material.CHISELED_TUFF_BRICKS, Material.RAW_GOLD_BLOCK
    );

    private final List<Material> terracottaOptions = List.of(
        Material.BLACK_GLAZED_TERRACOTTA, Material.RED_GLAZED_TERRACOTTA, Material.GREEN_GLAZED_TERRACOTTA, 
        Material.BLUE_GLAZED_TERRACOTTA
    );

    private final List<Material> endOptions = List.of(
        Material.END_STONE, Material.END_STONE_BRICKS, Material.PURPUR_PILLAR, Material.MAGENTA_STAINED_GLASS
    );

    public Placetto(RpgPlayer rpgPlayer, int level) {
        super(rpgPlayer,level,"Placetto");
        this.width = data.getInt("width");
    }

    @Override
    public void onAxeRightClick(Trigger event) {
        if(isSilenced()){return;};
        if(skillCheck()){return;}
        Player player = getRpgPlayer().getPlayer();
        affect = WeaponUtils.getAffectFromArmorSlot(player, 1);
        World world = player.getWorld();
        Location baseLocation = player.getLocation();
        Vector direction = player.isSneaking() ? getSideDirection(player) : baseLocation.getDirection();

        placeBlockWithDelay(baseLocation, direction, 0, 0); // Feet level instantly
        placeBlockWithDelay(baseLocation, direction, 3, 1); // Head level after 10 ticks
        placeBlockWithDelay(baseLocation, direction, 6, 2); // Above head level after 20 ticks
        placeBlockWithDelay(baseLocation, direction, 9, 3);
        if (affect == AffectType.LEATHER) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
        }
        refreshCooldown();
    }

    @Override
    public boolean showActionBar(){
        return WeaponUtils.isMeleeWeapon(getRpgPlayer().getPlayer().getItemInHand());
    }

    private void placeBlockWithDelay(Location base, Vector dir, int delay, int yOffset) {
        new BukkitRunnable() {
            @Override
            public void run() {
                placeBlocks(base, dir, yOffset);
            }
        }.runTaskLater(SkillScheme.getInstance(), delay);
    }

    private void placeBlocks(Location base, Vector dir, int yOffset) {
        Vector perpendicular = new Vector(-dir.getZ(), 0, dir.getX()); // Perpendicular to placement direction
        Location centerFront = base.clone().add(dir.clone().multiply(2)).add(0, yOffset, 0);
        Location centerBack = base.clone().subtract(dir.clone().multiply(2)).add(0, yOffset, 0);
        int placeWidth = width - 1;
        for (int i = -placeWidth; i <= placeWidth; i++) {
            Location blockLocationFront = centerFront.clone().add(perpendicular.clone().multiply(i));
            Location blockLocationBack = centerBack.clone().add(perpendicular.clone().multiply(i));
            placeBlock(blockLocationFront);
            placeBlock(blockLocationBack);
        }
        
        // Location left = base.clone().add(dir.clone().multiply(width)).add(0, yOffset, 0);
        // Location right = base.clone().add(dir.clone().multiply(-width)).add(0, yOffset, 0);
        // placeBlock(left);
        // placeBlock(right);
        // if (BlockUtils.canPlaceBlock(left)) placeBlock(left);
        // if (BlockUtils.canPlaceBlock(right)) placeBlock(right);
    }

    private void placeBlock(Location location) {
        Material blockType = getRandomBlockType(affect);
        Block block = location.getBlock();
        if (block.isEmpty() || block.isLiquid()) {
            BlockUtils.tempPlaceBlock(blockType, location, 25);
            boostEntities(location);
            location.getWorld().playSound(location, blockType.createBlockData().getSoundGroup().getPlaceSound(), 1F, 1F);
        }        
        // "Loops through all blocks in this location lol so it runs once because only 1 block can be at a location"
        // BlockUtils.getNearbyBlocks(location, 1).stream()
        //     .filter(block -> block.getType() == Material.AIR  
        //         || (block.getType() == Material.WATER 
        //         && block.getBlockData() instanceof Levelled levelledWater 
        //         && levelledWater.getLevel() == 0)
        //         || (block.getType() == Material.LAVA 
        //         && block.getBlockData() instanceof Levelled levelledLava 
        //         && levelledLava.getLevel() == 0))
        //     .forEach(block -> {
        //         BlockUtils.tempPlaceBlock(blockType, location, 25);
        //     });
        // Spawn smoke effect
        location.getWorld().spawnParticle(Particle.SMOKE, location.clone().add(0.5, 0, 0.5), 10, 0.2, 0, 0.2, 0.01);
        
    }

    private Material getRandomBlockType(AffectType aff) {
        if (aff == AffectType.LEATHER) {
            return poorOptions.get(new Random().nextInt(poorOptions.size()));
        } else if (aff == AffectType.CHAINMAIL) {
            return stickyOptions.get(new Random().nextInt(stickyOptions.size()));
        } else if (aff == AffectType.IRON) {
            return woodOptions.get(new Random().nextInt(woodOptions.size()));
        } else if (aff == AffectType.GOLD) {
            return woodOptions.get(new Random().nextInt(woodOptions.size()));
        } else if (aff == AffectType.DIAMOND) {
            return terracottaOptions.get(new Random().nextInt(terracottaOptions.size()));
        } else if (aff == AffectType.NETHERITE) {
            return richOptions.get(new Random().nextInt(richOptions.size()));
        } else if (aff == AffectType.ELYTRA) {
            return endOptions.get(new Random().nextInt(endOptions.size()));
        } else {
            return poorOptions.get(new Random().nextInt(poorOptions.size()));
        }
        
    }

    private Vector getSideDirection(Player player) {
        Vector direction = player.getLocation().getDirection();
        return new Vector(-direction.getZ(), 0, direction.getX()); // Rotates properly for left/right placement
    }


    private void boostEntities(Location location) {
        // Outline the 1x1x1 block at the location
        
        
        // Get nearby entities and process them
        location.getWorld().getNearbyEntities(location, 0.5, 1, 0.5).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .forEach(entity -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    
                    // Draw a line from the location to the entity
                    drawLine(location, livingEntity.getLocation());
                    outlineBlock(location);
                    // Apply velocity and damage
                    Vector velocity = livingEntity.getVelocity();
                    
                    if (affect == AffectType.DIAMOND || affect == AffectType.ELYTRA) {
                        velocity.setY(velocity.getY() + 0.447);
                    } else {
                        velocity.setY(velocity.getY() + 0.334);
                    }
                    livingEntity.setVelocity(velocity);
                    if (affect == AffectType.IRON) {
                        livingEntity.damage(4, getRpgPlayer().getPlayer());
                    } else {
                        livingEntity.damage(2, getRpgPlayer().getPlayer());
                    }
                    
                    livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PILLAGER_HURT, 1F, 2.0F);
                    livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GOAT_RAM_IMPACT, 0.5F, 1.0F);
                    livingEntity.getWorld().spawnParticle(Particle.CRIT, livingEntity.getLocation(), 10, 0.1, 1, 0.1, 1);
                });
    }
    // private void boostEntities(Location location) {
    //     location.getWorld().getNearbyEntities(location, 0.5, 1, 0.5).stream()
    //             .filter(entity -> entity instanceof LivingEntity)
    //             .forEach(entity -> {
    //                 LivingEntity livingEntity = (LivingEntity) entity;
    //                 Vector velocity = livingEntity.getVelocity();
    //                 velocity.setY(velocity.getY() + 0.334);
    //                 livingEntity.setVelocity(velocity);
    //                 livingEntity.damage(2);
    //             });
    // }

    private void outlineBlock(Location location) {
        World world = location.getWorld();
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();
        // World world = location.getWorld();
        // Get the block's exact coordinates (integer values)
        // int blockX = location.();
        // int blockY = location.();
        // int blockZ = location.();
        
        // Use REDSTONE particles for the outline (you can change this to any particle you prefer)
        Particle outlineParticle = Particle.DUST;
        
        // Create a dust option for colored particles (red in this case)
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.YELLOW, 1);
        
        // Draw edges of the cube
        for (double i = 0; i <= 1; i += 0.1) {
            // Bottom edges
            world.spawnParticle(outlineParticle, x + i, y, z, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x, y, z + i, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x + i, y, z + 1, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x + 1, y, z + i, 1, 0, 0, 0, 0, dustOptions);
            
            // Top edges
            world.spawnParticle(outlineParticle, x + i, y + 1, z, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x, y + 1, z + i, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x + i, y + 1, z + 1, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x + 1, y + 1, z + i, 1, 0, 0, 0, 0, dustOptions);
            
            // Vertical edges
            world.spawnParticle(outlineParticle, x, y + i, z, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x + 1, y + i, z, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x, y + i, z + 1, 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(outlineParticle, x + 1, y + i, z + 1, 1, 0, 0, 0, 0, dustOptions);
        }
    }

    private void drawLine(Location from, Location to) {
        World world = from.getWorld();
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();
        
        // Use END_ROD particles for the line
        for (double d = 0; d <= distance; d += 0.2) {
            Vector v = direction.clone().multiply(d);
            Location loc = from.clone().add(v);
            
            // Spawn the end rod particle with zero speed
            world.spawnParticle(Particle.END_ROD, loc, 2, 0, 0, 0, 0);
        }
    }
}
