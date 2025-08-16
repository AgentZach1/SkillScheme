package plugin.sirlich.skills.clans.boots;

import org.bukkit.event.player.PlayerDropItemEvent;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.ManaSkill;
import plugin.sirlich.utilities.BlockUtils;
import plugin.sirlich.utilities.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.Particle;
import org.bukkit.Location;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.sirlich.skills.meta.AffectType;

public class ArcticArmor extends ManaSkill
{
    private static String id = "ArcticArmor";
    private List<Block> processingQueue = new ArrayList<>();
    private AffectType affect = AffectType.NONE;

    public ArcticArmor(RpgPlayer rpgPlayer, int level){
        super(rpgPlayer,level,"ArcticArmor");
    }

    @Override
    public void onTick(){
        if(isSilenced()){return;};
        /**
         * 
         * 
         * 
         * List<Block> nearbyBlocks = BlockUtils.getNearbyBlocks(getRpgPlayer().getPlayer().getLocation(), data.getInt("radius"))
            .stream()
            .filter(block -> block.getType() != Material.AIR && block.getType() != Material.ICE)
            .collect(Collectors.toList());

        for (Block block : nearbyBlocks) {
            processBlock(block);
        }
         */
        affect = WeaponUtils.getAffectFromArmorSlot(getRpgPlayer().getPlayer(), 3);
        if (affect == AffectType.CHAINMAIL || affect == AffectType.IRON) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 1));
        }
        else if (affect == AffectType.DIAMOND || affect == AffectType.NETHERITE) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20, 0));
        }
        else if (affect == AffectType.GOLD) {
            if (getRpgPlayer().getPlayer().getPotionEffect(PotionEffectType.REGENERATION) == null) {
                getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1));
            }
        }
        else if (affect == AffectType.LEATHER) {
            getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
        }
        
        
        if (processingQueue.isEmpty()) {
            processingQueue = BlockUtils.getNearbyBlocks(getRpgPlayer().getPlayer().getLocation(), data.getInt("radius"))
            .stream()
            .filter(block -> block.getType() != Material.AIR)
            .filter(block -> block.getY() <= getRpgPlayer().getPlayer().getLocation().getY() + 0.5)
            .collect(Collectors.toList());
        }

        // Process a fixed number of blocks per tick
        int batchSize = 450; // Number of blocks to process per tick
        for (int i = 0; i < batchSize && !processingQueue.isEmpty(); i++) {
            // Block block = ;
            processBlock(processingQueue.remove(0));
        }
        // for(Block block : BlockUtils.getNearbyBlocks(getRpgPlayer().getPlayer().getLocation(), data.getInt("radius"))){
        //     // Material blockType = block.getType();
        //     // if (blockType == Material.AIR || blockType == Material.ICE || block.getLightLevel() < 0 ) {
        //     //     continue;
        //     // }
        
        //     // if (block.getType() == Material.WATER) {
        //     //     if (block.getBlockData() instanceof Levelled levelledWater) {
        //     //         // Level 0 indicates a source block
        //     //         if (levelledWater.getLevel() == 0) {
        //     //             // Temporarily replace with frosted ice
        //     //             BlockUtils.tempPlaceBlock(Material.FROSTED_ICE, block.getLocation(), data.getInt("ice_duration"));
        //     //         }
        //     //     }
        //     // }
        //     // else {
        //     //     Location blockAbove = block.getLocation().add(0, 1, 0);
        //     //     if (blockAbove.getBlock().getType() == Material.AIR && block.getType() != Material.SNOW) {
        //     //         BlockData snowData = Material.SNOW.createBlockData();
        //     //         if (snowData.isSupported(blockAbove)) {
        //     //             BlockUtils.tempPlaceBlock(Material.SNOW, blockAbove, data.getInt("ice_duration"));
        //     //         }
                    
        //     //     }
        //     // }
        //     // if (block.getType() == Material.ICE || block.getType() == Material.FROSTED_ICE || block.getType() == Material.BLUE_ICE 
        //     //     || block.getType() == Material.PACKED_ICE || block.getType() == Material.SNOW || block.getType() == Material.POWDER_SNOW 
        //     //     || block.getType() == Material.SNOW_BLOCK) {
        //     //     getRpgPlayer().getPlayer().getWorld().spawnParticle(Particle.SNOWFLAKE, block.getLocation(), 1, 0.0F, 1.0F, 0.00F, 0.1);
        //     // }
            
        // }
    }

    @Override
    public void onSwordDrop(PlayerDropItemEvent entityEvent){
        toggleStatus();
    }

    @Override
    public void onAxeDrop(PlayerDropItemEvent entityEvent){
        toggleStatus();
    }

    private void processBlock(Block block) {
        Material blockType = block.getType();
        if (blockType == Material.AIR || blockType == Material.ICE || block.getLightLevel() < 0) {
            return;
        }
        

        if (block.getType() == Material.WATER) {
            if (block.getBlockData() instanceof Levelled levelledWater && levelledWater.getLevel() == 0) {
                BlockUtils.tempPlaceBlock(Material.FROSTED_ICE, block.getLocation(), data.getInt("ice_duration"));
            }
        } 
        else if (block.getType() == Material.LAVA && affect == AffectType.NETHERITE) {
            // make basalt
            if (block.getBlockData() instanceof Levelled levelledLava && levelledLava.getLevel() == 0) {
                BlockUtils.tempPlaceBlock(Material.BASALT, block.getLocation(), data.getInt("ice_duration"));
            }
        }
        else {
            Location blockAbove = block.getLocation().add(0, 1, 0);
            if (blockAbove.getBlock().getType() == Material.AIR && block.getType() != Material.SNOW && block.getType() != Material.FROSTED_ICE) {
                BlockData snowData = Material.SNOW.createBlockData();
                if (snowData.isSupported(blockAbove)) {
                    BlockUtils.tempPlaceBlock(Material.SNOW, blockAbove, data.getInt("ice_duration"));
                }
            }
        }

        if (block.getType() == Material.ICE || block.getType() == Material.FROSTED_ICE || block.getType() == Material.BLUE_ICE
                || block.getType() == Material.PACKED_ICE || block.getType() == Material.SNOW || block.getType() == Material.POWDER_SNOW
                || block.getType() == Material.SNOW_BLOCK) {
            getRpgPlayer().getPlayer().getWorld().spawnParticle(Particle.SNOWFLAKE, block.getLocation(), 1, 0.0F, 1.0F, 0.00F, 0.1);
        } else if (block.getType() == Material.BASALT) {
            getRpgPlayer().getPlayer().getWorld().spawnParticle(Particle.LAVA, block.getLocation(), 1, 0.0F, 1.0F, 0.00F, 0.1);
        }
    }
}
