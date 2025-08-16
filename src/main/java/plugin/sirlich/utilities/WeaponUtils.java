package plugin.sirlich.utilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.ClassType;
import plugin.sirlich.skills.meta.AffectType;

public class WeaponUtils {

    public static boolean isWeapon(ItemStack itemStack){
        return isWeapon(itemStack.getType());
    }

    public static boolean isWeapon(Material material){
        return isMeleeWeapon(material) || material.equals(Material.BOW);
    }

    public static boolean isWearingFullSet(Player player){
        return getClassTypeFromArmor(player) != ClassType.UNDEFINED;
    }

    public static boolean isWearingArmor(Player player) {
        int armor = 0;
        for (int z = 0; z<4; z++) {
            if (getSkillClassFromArmorSlot(player, z) != ClassType.UNDEFINED) {
                armor++;
            }
        }
        return armor > 0;
    }

    public static ClassType getClassTypeFromArmor(Player player){
        // PlayerInventory inventory = player.getInventory();
        // try{
        //     if(inventory.getChestplate().getType() == Material.DIAMOND_CHESTPLATE &&
        //     inventory.getLeggings().getType() == Material.DIAMOND_LEGGINGS &&
        //     inventory.getBoots().getType() == Material.DIAMOND_BOOTS &&
        //     inventory.getHelmet().getType() == Material.DIAMOND_HELMET){
        //         return ClassType.PALADIN;
        //     }else

        //     if(inventory.getChestplate().getType() == Material.GOLDEN_CHESTPLATE &&
        //             inventory.getLeggings().getType() == Material.GOLDEN_LEGGINGS &&
        //             inventory.getBoots().getType() == Material.GOLDEN_BOOTS &&
        //             inventory.getHelmet().getType() == Material.GOLDEN_HELMET){
        //         return ClassType.WARLOCK;
        //     } else

        //     if(inventory.getChestplate().getType() == Material.CHAINMAIL_CHESTPLATE &&
        //             inventory.getLeggings().getType() == Material.CHAINMAIL_LEGGINGS &&
        //             inventory.getBoots().getType() == Material.CHAINMAIL_BOOTS &&
        //             inventory.getHelmet().getType() == Material.CHAINMAIL_HELMET){
        //         return ClassType.RANGER;
        //     }else

        //     if(inventory.getChestplate().getType() == Material.LEATHER_CHESTPLATE &&
        //             inventory.getLeggings().getType() == Material.LEATHER_LEGGINGS &&
        //             inventory.getBoots().getType() == Material.LEATHER_BOOTS &&
        //             inventory.getHelmet().getType() == Material.LEATHER_HELMET){
        //         return ClassType.ROGUE;
        //     }else

        //     if(inventory.getChestplate().getType() == Material.IRON_CHESTPLATE &&
        //             inventory.getLeggings().getType() == Material.IRON_LEGGINGS &&
        //             inventory.getBoots().getType() == Material.IRON_BOOTS &&
        //             inventory.getHelmet().getType() == Material.IRON_HELMET){
        //         return ClassType.FIGHTER;
        //     } else {
            return ClassType.UNDEFINED;
            // }
        // }


        //Its ok to catch this -its just a null catch for null armor.
        // catch(Exception e){
        //     return ClassType.UNDEFINED;
        // }
    }

    // 0 helmet, 1 chestplate, 2 leggings, 3 boots
    public static ClassType getSkillClassFromArmorSlot(Player player, int position) {
        PlayerInventory inventory = player.getInventory();
        try {
            if (position == 0) {
                if(isArmor(inventory.getHelmet())){
                    return ClassType.HEAD;
                }
            }
            if (position == 1) {
                if (isArmor(inventory.getChestplate())) {
                    return ClassType.BREAST;
                }
            }
            if (position == 2) {
                if (isArmor(inventory.getLeggings())) {
                    return ClassType.LEGS;
                }
            }
            if (position == 3) {
                if (isArmor(inventory.getBoots())) {
                    return ClassType.FEET;
                }
            }
        }
        
        catch(Exception e){
            return ClassType.UNDEFINED;
        }
        return ClassType.UNDEFINED;
    }
    
    public static AffectType getAffectFromArmorSlot(Player player, int pos) {
        PlayerInventory inventory = player.getInventory();
        try {
            Material mat = Material.DIRT;
            if (pos == 0) {
                mat = inventory.getHelmet().getType();
                if(mat == Material.DIAMOND_HELMET){
                    return AffectType.DIAMOND;
                } else if (mat == Material.GOLDEN_HELMET) {
                    return AffectType.GOLD;
                } else if (mat == Material.IRON_HELMET) {
                    return AffectType.IRON;
                } else if (mat == Material.CHAINMAIL_HELMET) {
                    return AffectType.CHAINMAIL;
                } else if (mat == Material.NETHERITE_HELMET) {
                    return AffectType.NETHERITE;
                } else if (mat == Material.TURTLE_HELMET) {
                    return AffectType.TURTLE;
                } else if (mat == Material.LEATHER_HELMET) {
                    return AffectType.LEATHER;
                }
            }
            if (pos == 1) {
                mat = inventory.getChestplate().getType();
                if(mat == Material.DIAMOND_CHESTPLATE){
                    return AffectType.DIAMOND;
                } else if (mat == Material.GOLDEN_CHESTPLATE) {
                    return AffectType.GOLD;
                } else if (mat == Material.IRON_CHESTPLATE) {
                    return AffectType.IRON;
                } else if (mat == Material.CHAINMAIL_CHESTPLATE) {
                    return AffectType.CHAINMAIL;
                } else if (mat == Material.NETHERITE_CHESTPLATE) {
                    return AffectType.NETHERITE;
                } else if (mat == Material.ELYTRA) {
                    return AffectType.ELYTRA;
                } else if (mat == Material.LEATHER_CHESTPLATE) {
                    return AffectType.LEATHER;
                }
            }
            if (pos == 2) {
                mat = inventory.getLeggings().getType();
                if(mat == Material.DIAMOND_LEGGINGS){
                    return AffectType.DIAMOND;
                } else if (mat == Material.GOLDEN_LEGGINGS) {
                    return AffectType.GOLD;
                } else if (mat == Material.IRON_LEGGINGS) {
                    return AffectType.IRON;
                } else if (mat == Material.CHAINMAIL_LEGGINGS) {
                    return AffectType.CHAINMAIL;
                } else if (mat == Material.NETHERITE_LEGGINGS) {
                    return AffectType.NETHERITE;
                } else if (mat == Material.LEATHER_LEGGINGS) {
                    return AffectType.LEATHER;
                }
            }
            if (pos == 3) {
                mat = inventory.getBoots().getType();
                if(mat == Material.DIAMOND_BOOTS){
                    return AffectType.DIAMOND;
                } else if (mat == Material.GOLDEN_BOOTS) {
                    return AffectType.GOLD;
                } else if (mat == Material.IRON_BOOTS) {
                    return AffectType.IRON;
                } else if (mat == Material.CHAINMAIL_BOOTS) {
                    return AffectType.CHAINMAIL;
                } else if (mat == Material.NETHERITE_BOOTS) {
                    return AffectType.NETHERITE;
                } else if (mat == Material.LEATHER_BOOTS) {
                    return AffectType.LEATHER;
                }
            }
        }
        
        catch(Exception e){
            return AffectType.NONE;
        }
        return AffectType.NONE;
    }

    public static boolean isMeleeWeapon(ItemStack itemStack){
        return isMeleeWeapon(itemStack.getType());
    }

    public static boolean isMeleeWeapon(Material material){
        return isSword(material) || isAxe(material);
    }

    public static boolean isBow(ItemStack itemStack){
        return isBow(itemStack.getType());
    }

    public static boolean isBow(Material material){
        return material == Material.BOW;
    }

    public static boolean isSword(ItemStack itemStack){
        return isSword(itemStack.getType());
    }

    public static boolean isSword(Material material){
        return material.equals(Material.WOODEN_SWORD) ||
                material.equals(Material.STONE_SWORD) ||
                material.equals(Material.IRON_SWORD) ||
                material.equals(Material.DIAMOND_SWORD) ||
                material.equals(Material.GOLDEN_SWORD) ||
                material.equals(Material.NETHERITE_SWORD);
    }

    public static boolean isAxe(ItemStack itemStack){
        return isAxe(itemStack.getType());
    }

    public static boolean isAxe(Material material){
        return material.equals(Material.WOODEN_AXE) ||
                material.equals(Material.STONE_AXE) ||
                material.equals(Material.IRON_AXE) ||
                material.equals(Material.DIAMOND_AXE) ||
                material.equals(Material.GOLDEN_AXE) ||
                material.equals(Material.NETHERITE_AXE);
    }

    public static boolean isShield(ItemStack itemStack) {
        return itemStack.getType().equals(Material.SHIELD);
    }

    public static void giveLoadout(RpgPlayer rpgPlayer, ClassType classType){
        // Player player = rpgPlayer.getPlayer();
        // if(classType == ClassType.PALADIN){
        //     player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        //     player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        //     player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_AXE));
        // } else if(classType == ClassType.FIGHTER){
        //     player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        //     player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        //     player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_AXE));
        // }  else if(classType == ClassType.RANGER){
        //     player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        //     player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        //     player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_AXE));
        //     player.getInventory().addItem(new ItemStack(Material.BOW));
        //     player.getInventory().addItem(new ItemStack(Material.ARROW,64));

        // } else if(classType == ClassType.ROGUE){
        //     player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        //     player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        //     player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_AXE));
        //     player.getInventory().addItem(new ItemStack(Material.BOW));
        //     player.getInventory().addItem(new ItemStack(Material.ARROW,64));
        // } else if(classType == ClassType.WARLOCK){
        //     player.getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        //     player.getInventory().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        //     player.getInventory().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        //     player.getInventory().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        //     player.getInventory().addItem(new ItemStack(Material.IRON_AXE));
        // }
        
    }

    public static boolean isArmor(ItemStack itemStack) {
    return itemStack != null && isArmor(itemStack.getType());
}

    public static boolean isArmor(Material material) {
        return material == Material.DIAMOND_HELMET || material == Material.DIAMOND_CHESTPLATE || 
            material == Material.DIAMOND_LEGGINGS || material == Material.DIAMOND_BOOTS ||
            material == Material.GOLDEN_HELMET || material == Material.GOLDEN_CHESTPLATE || 
            material == Material.GOLDEN_LEGGINGS || material == Material.GOLDEN_BOOTS ||
            material == Material.IRON_HELMET || material == Material.IRON_CHESTPLATE || 
            material == Material.IRON_LEGGINGS || material == Material.IRON_BOOTS ||
            material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE || 
            material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS ||
            material == Material.CHAINMAIL_HELMET || material == Material.CHAINMAIL_CHESTPLATE || 
            material == Material.CHAINMAIL_LEGGINGS || material == Material.CHAINMAIL_BOOTS ||
            material == Material.NETHERITE_HELMET || material == Material.NETHERITE_CHESTPLATE || 
            material == Material.NETHERITE_LEGGINGS || material == Material.NETHERITE_BOOTS ||
            material == Material.TURTLE_HELMET || material == Material.ELYTRA;
    }



}
