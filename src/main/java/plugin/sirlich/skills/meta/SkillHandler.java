package plugin.sirlich.skills.meta;

// import com.codingforcookies.armorequip.ArmorEquipEvent;
// import com.codingforcookies.armorequip.ArmorType;
// import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgProjectile;
import plugin.sirlich.core.RpgPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.sirlich.skills.triggers.Trigger;
import plugin.sirlich.utilities.Color;
import plugin.sirlich.utilities.WeaponUtils;
// import sun.security.util.ByteArrayLexOrder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static plugin.sirlich.utilities.WeaponUtils.*;

public class SkillHandler implements Listener
{
    public enum ArmorPiece {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS
    }
    /*
    Handles environmental damage
    - Fall damage
    - Explosion damage
     */
    @EventHandler
    public void onFallDamage(EntityDamageEvent event){

        //Is player
        if(event.getEntity() instanceof Player){

            //Get RpgPlayer
            Player player = (Player) event.getEntity();
            RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);

            //Fall Damage
            if(event.getCause() == EntityDamageEvent.DamageCause.FALL){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    skill.onFallDamageSelf(event);
                }
            }

            //Explosion Damage
            else if(event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    skill.onExplosionDamageSelf(event);
                }
            }
        }
    }

    //Stop players from interacting with their helmet slot
    // @EventHandler()
    // public void onClick(InventoryClickEvent event)
    // {
    //     InventoryType type = event.getInventory().getType();
    //     if(type == InventoryType.CRAFTING) {
    //         if (event.getSlot() == 39) {
    //             event.setCursor(null);
    //             event.setCancelled(true);
    //         }
    //     }

    //     //Test for shift clicks
    //     if(event.isShiftClick()){
    //         if(matchType(event.getCurrentItem()) == ArmorPiece.HELMET){
    //             event.setCancelled(true);
    //         }
    //     }
    // }
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // Check if the click involves the armor slot
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            RpgPlayer.getRpgPlayer(player).applySkillsFromArmor(player.getUniqueId());
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !WeaponUtils.isArmor(clickedItem)) {
            return; // Not an armor piece
        }
        // Check if shift-clicking the armor into the armor slot
        if (event.getClick().isShiftClick()) {
            // PlayerInventory inventory = event.getClickedInventory();
            if (event.getClickedInventory() instanceof PlayerInventory inventory) {
                if (inventory != null && inventory.getType() == InventoryType.PLAYER) {
                    // Determine the target armor slot based on the armor type
                    Material material = clickedItem.getType();
                    PlayerInventory playerInventory = player.getInventory();
                    ArmorPiece armorType = matchType(clickedItem);
                    if (armorType != null && playerInventory.getHelmet() == null) {
                        // Target is helmet slot
                        RpgPlayer.getRpgPlayer(player).applySkillsFromArmor(player.getUniqueId());
                    } else if (armorType != null && playerInventory.getChestplate() == null) {
                        // Target is chestplate slot
                        RpgPlayer.getRpgPlayer(player).applySkillsFromArmor(player.getUniqueId());
                    } else if (armorType != null && playerInventory.getLeggings() == null) {
                        // Target is leggings slot
                        RpgPlayer.getRpgPlayer(player).applySkillsFromArmor(player.getUniqueId());
                    } else if (armorType != null && playerInventory.getBoots() == null) {
                        // Target is boots slot
                        RpgPlayer.getRpgPlayer(player).applySkillsFromArmor(player.getUniqueId());
                    }
                }
            }
        }
    }

    //Handles armor equip and de-equip simple event
    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event){
        RpgPlayer.getRpgPlayer(event.getPlayer()).applySkillsFromArmor(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        ItemStack brokenItem = event.getBrokenItem();
        if (!WeaponUtils.isArmor(brokenItem)) {
            return;
        }
        RpgPlayer.getRpgPlayer(event.getPlayer()).applySkillsFromArmor(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        final UUID uuid = event.getPlayer().getUniqueId();
        ArmorPiece armorType = matchType(event.getPlayer().getInventory().getItemInMainHand());

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR))
        {
            // //Cancel if its a helmet
            // if (armorType == ArmorPiece.HELMET)
            // {
            //     event.setCancelled(true);
            // }

            //Delay apply armor if other armor
            if (armorType == ArmorPiece.HELMET || armorType == ArmorPiece.LEGGINGS || armorType == ArmorPiece.BOOTS || armorType == ArmorPiece.CHESTPLATE){
                RpgPlayer.getRpgPlayer(player).applySkillsFromArmor(uuid);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        RpgPlayer.getRpgPlayer(event.getPlayer()).applySkillsFromArmor(event.getPlayer().getUniqueId());
    }

    /*
    Handles item drop events
    - Swords
    - Axes
     */
    @EventHandler
    public void onInvDrop(PlayerDropItemEvent event){
        ItemStack itemStack = event.getItemDrop().getItemStack();
        RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(event.getPlayer());
        // If wearing boots apply skills cus that's how it works
        if (AffectType.NONE != getAffectFromArmorSlot(event.getPlayer(), 3)) {
            for(Skill skill : rpgPlayer.getActiveSkillList()){
                skill.onItemDrop(event);
                if(isAxe(itemStack)){
                    skill.onAxeDrop(event);
                    event.setCancelled(true);
                } else if (isSword(itemStack)){
                    skill.onSwordDrop(event);
                    event.setCancelled(true);
                } else if (isBow(itemStack)){
                    skill.onBowDrop(event);
                    event.setCancelled(true);
                }
            }
        }
    }

    //Handles dropped-item stuff.
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player  = event.getPlayer();
        RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);
        for(Skill skill : rpgPlayer.getActiveSkillList()){
            skill.onItemPickup(event);
        }

        for(RpgPlayer otherPlayer : RpgPlayer.getRpgPlayers()){
            if(otherPlayer.getPlayer().getUniqueId() != player.getUniqueId()){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    skill.onItemPickupOther(event);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player){
            RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(event.getEntity().getUniqueId());
            rpgPlayer.setLastDamaged(System.currentTimeMillis());
            for(Skill skill : rpgPlayer.getActiveSkillList()){
                skill.onDamageSelf(event);
            }
        }
    }

    /*
    HANDLES: Melee attack events for
        - Sword
        - Axe
        - Bow
     */
    @EventHandler
    public void onMeleeDamage(EntityDamageByEntityEvent event){
        //If YOU are a player
        if(event.getEntity() instanceof Player){

            //Get RpgPlayer
            Player player  = (Player) event.getEntity();
            RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);
            Material itemType  = player.getInventory().getItemInHand().getType();

            if(event.getDamager() instanceof Player){
                RpgPlayer.getRpgPlayer((Player)event.getDamager()).logPlayerAttack();
            }
            // rpgPlayer.tell("------ skill ");

            //Sword melee attack
            if(isSword(itemType)){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    // rpgPlayer.tell("Using skill " + skill.getName());
                    skill.onSwordMeleeAttackSelf(event);
                }
            }
            
            //Axe melee attack
            else if(isAxe(itemType)){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    if(skill instanceof CooldownSkill){
                        // rpgPlayer.tell("Using skill " + skill.getName());
                        skill.onAxeMeleeAttackSelf(event);
                    }
                }
            }
            // rpgPlayer.tell("------ skill ");

            //Called when an arrow hits you
            if(event.getDamager() instanceof Projectile){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    if(skill instanceof CooldownSkill){
                        // rpgPlayer.tell("Using skill " + skill.getName());
                        skill.onArrowHitSelf(event);
                    }
                }
            }
            // rpgPlayer.tell("------ skill ");

            if(event.getDamager() instanceof LivingEntity){
                //Called when you get hit with any melee attack
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    if(skill instanceof CooldownSkill){
                        // rpgPlayer.tell("Using skill " + skill.getName());
                        skill.onMeleeAttackSelf(event);
                    }
                }
            }
        }

        //If the person YOU HIT is a player (this needs to be removed somehow)
        if(event.getDamager() instanceof Player){

            //Get RpgPlayer
            Player player = (Player) event.getDamager();
            RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);
            Material itemType  = player.getInventory().getItemInHand().getType();

            // rpgPlayer.tell("------ skill ------");

            //Hit another person with a sword attack
            if(isSword(itemType)){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    // rpgPlayer.tell("Using skill " + skill.getName());
                    skill.onSwordMeleeAttackOther(event);
                }
            }

            //Hit another person with a axe attack
            else if(isAxe(itemType)){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    // rpgPlayer.tell("Using skill " + skill.getName());
                    skill.onAxeMeleeAttackOther(event);
                }
            }

            //Hit another person with a bow attack (melee)
            else if(itemType == Material.BOW){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    // rpgPlayer.tell("Using skill " + skill.getName());
                    skill.onBowMeleeAttack(event);
                }
            }

            //Hit something without a sword, axe, or bow
            else {
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    // rpgPlayer.tell("Using skill " + skill.getName());
                    skill.onMeleeAttackOther(event);
                }
            }
        }
    }


    //Cancel player charging the bow
    @EventHandler
    public void onChangeSlot(PlayerItemHeldEvent event)  {
        RpgPlayer.getRpgPlayer(event.getPlayer()).setDrawingBow(false);
    }

    //Handles: onBowFire
    @EventHandler
    public void onBowFire(EntityShootBowEvent event){
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);

            // //Some players cant use bows!
            // if(rpgPlayer.getClassType() == ClassType.UNDEFINED || ! rpgPlayer.getClassType().canUseBow()){
            //     rpgPlayer.tell("You can't use bows with your current class.");
            //     event.setCancelled(true);
            // }
            RpgProjectile.registerProjectile((Arrow)event.getProjectile(), RpgPlayer.getRpgPlayer((Player)event.getEntity()));
            for(Skill skill : rpgPlayer.getActiveSkillList()){
                skill.onBowFire(event);
            }
            rpgPlayer.setDrawingBow(false);
        }
    }

    @EventHandler
    public void onDraw(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getItem().getType() == Material.BOW) {
            if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RpgPlayer.getRpgPlayer(event.getPlayer()).setDrawingBow(true);
            }
        }
    }

    //Handles: onDeath
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(RpgPlayer.isRpgPlayer(event.getEntity().getUniqueId())){
            Player player = event.getEntity();
            RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);
            for(Skill skill : rpgPlayer.getActiveSkillList()){
                skill.onDeath(event);
            }
        }
    }

    //Handle arrow hits
    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Arrow){
            if(event.getEntity() != null && event.getEntity() instanceof LivingEntity) {
                Arrow arrow = (Arrow) event.getDamager();
                if(RpgProjectile.hasProjectile(arrow)){
                    RpgProjectile rpgProjectile = RpgProjectile.getProjectile(arrow);
                    RpgPlayer rpgPlayer = rpgProjectile.getShooter();
                    rpgPlayer.logPlayerAttack();
                    for(Skill skill : rpgPlayer.getActiveSkillList()){
                        // System.out.println("onArrowHit");
                        skill.onArrowHitEntity(event);
                    }
                    rpgProjectile.deregisterSelf();
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHitEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball) {
            if(event.getEntity() != null && event.getEntity() instanceof LivingEntity) {
                Fireball fireball = (Fireball) event.getDamager();
                if (RpgProjectile.hasProjectile(fireball)) {
                    RpgProjectile rpgProjectile = RpgProjectile.getProjectileFireball(fireball);
                    RpgPlayer rpgPlayer = rpgProjectile.getShooter();
                    rpgPlayer.logPlayerAttack();
                    for (Skill skill : rpgPlayer.getActiveSkillList()) {
                        skill.onProjectileHitEntity(event);
                    }
                    rpgProjectile.deregisterSelf();
                }
                
            }
        }
        if (event.getDamager() instanceof Item item) {
            if (RpgProjectile.hasProjectileItem(item)) {
                if(event.getEntity() != null && event.getEntity() instanceof LivingEntity) {
                    RpgProjectile rpgProjectile = RpgProjectile.getProjectileItem(item);
                    RpgPlayer rpgPlayer = rpgProjectile.getShooter();
                    rpgPlayer.logPlayerAttack();
                    for (Skill skill : rpgPlayer.getActiveSkillList()) {
                        skill.onProjectileHitEntity(event);
                    }
                    rpgProjectile.deregisterSelf();
                }
            }
        }
        // Could be for other projectiles
    }

    @EventHandler
    public void onArrow(ProjectileHitEvent event){
        if(event.getEntity() instanceof Arrow){
            Arrow arrow = (Arrow) event.getEntity();
            if(RpgProjectile.hasProjectile(arrow)){
                RpgProjectile rpgArrow = RpgProjectile.getProjectile(arrow);
                final RpgPlayer rpgPlayer = rpgArrow.getShooter();
                final ProjectileHitEvent projectileHitEvent = event;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!rpgPlayer.didJustAttack()){
                            for(Skill skill : rpgPlayer.getActiveSkillList()){
                                skill.onArrowHitGround(projectileHitEvent);
                            }
                        }
                    }

                }.runTaskLater(SkillScheme.getInstance(), 0);

                rpgArrow.deregisterSelf();
            }
        }
    }

    @EventHandler
    public void onProjectileHitGround(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getEntity();
            if (RpgProjectile.hasProjectile(fireball)) {
                RpgProjectile rpgProjectile = RpgProjectile.getProjectileFireball(fireball);
                final RpgPlayer rpgPlayer = rpgProjectile.getShooter();
                final ProjectileHitEvent projectileHitEvent = event;
                // rpgPlayer.logPlayerAttack();
                // for (Skill skill : rpgPlayer.getActiveSkillList()) {
                //     skill.onProjectileHitGround(event);
                // }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!rpgPlayer.didJustAttack()){
                            for(Skill skill : rpgPlayer.getActiveSkillList()){
                                skill.onProjectileHitGround(projectileHitEvent);
                            }
                        }
                    }

                }.runTaskLater(SkillScheme.getInstance(), 0);
                // rpgProjectile.remove();
                rpgProjectile.deregisterSelf();
            }
        }
        if (event.getEntity() instanceof Item item) {
            if (RpgProjectile.hasProjectileItem(item)) {
                RpgProjectile rpgProjectile = RpgProjectile.getProjectileItem(item);
                final RpgPlayer rpgPlayer = rpgProjectile.getShooter();
                final ProjectileHitEvent projectileHitEvent = event;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!rpgPlayer.didJustAttack()){
                            for(Skill skill : rpgPlayer.getActiveSkillList()){
                                skill.onProjectileHitGround(projectileHitEvent);
                            }
                        }
                    }

                }.runTaskLater(SkillScheme.getInstance(), 0);
                rpgProjectile.deregisterSelf();
            }
        }
        // More projectiles?
    }



    /*
    HANDLES: Right clicks for
        - Swords
        - Bows
        - Axes
     */
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);

        //Left click
        if(event.getMaterial() != Material.AIR &&
                (event.getAction() == Action.LEFT_CLICK_AIR ||
                        event.getAction() == Action.LEFT_CLICK_BLOCK )){

            for(Skill skill : rpgPlayer.getActiveSkillList()){
                skill.onLeftClick(new Trigger(event, player));
            }

            //Bow left click
            if(event.getMaterial().equals(Material.BOW)){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    skill.onBowLeftClick(new Trigger(event, player));
                }
            }

        }

        //Right click
        else if(event.getMaterial() != Material.AIR &&
                (event.getAction() == Action.RIGHT_CLICK_AIR ||
                        event.getAction() == Action.RIGHT_CLICK_BLOCK)){
            Material itemType = event.getMaterial();

            //Axe
            if(isAxe(itemType)){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    skill.onAxeRightClick(new Trigger(event, player));
                }
            }

            //Sword
            else if(isSword(itemType)){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    skill.onSwordRightClick(new Trigger(event, player));
                }
            }

            //Bow
            else if(itemType == Material.BOW){
                for(Skill skill : rpgPlayer.getActiveSkillList()){
                    skill.onBowRightClickEvent(new Trigger(event, player));
                }
            }
        }
    }

    @EventHandler
    public void playerItemHeldEvent​(PlayerItemHeldEvent event){
        // ActionBarAPI.sendActionBar(event.getPlayer(),"");
        event.getPlayer().sendTitle("", "", 1, 2, 1);
    }
    /*
    HANDLES: Swap events
     */
    @EventHandler
    public void onPlayerSwapItemEvent(PlayerSwapHandItemsEvent event){
        if(isMeleeWeapon(event.getOffHandItem().getType())){
            Player player = event.getPlayer();
            RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);
            for(Skill skill : rpgPlayer.getActiveSkillList()){
                skill.onSwap(event);
            }
        }
    }

    // Add this method to your SkillHandler class
    // Deals with no import
    public ArmorPiece matchType(ItemStack currentItem) {
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return null; // No item or invalid item
        }
        
        if (currentItem.getType() == Material.DIAMOND_HELMET ||
            currentItem.getType() == Material.IRON_HELMET ||
            currentItem.getType() == Material.GOLDEN_HELMET ||
            currentItem.getType() == Material.LEATHER_HELMET ||
            currentItem.getType() == Material.CHAINMAIL_HELMET ||
            currentItem.getType() == Material.NETHERITE_HELMET ||
            currentItem.getType() == Material.TURTLE_HELMET) {
            return ArmorPiece.HELMET;
        } else if (currentItem.getType() == Material.DIAMOND_CHESTPLATE ||
                currentItem.getType() == Material.IRON_CHESTPLATE ||
                currentItem.getType() == Material.GOLDEN_CHESTPLATE ||
                currentItem.getType() == Material.LEATHER_CHESTPLATE ||
                currentItem.getType() == Material.CHAINMAIL_CHESTPLATE ||
                currentItem.getType() == Material.NETHERITE_CHESTPLATE) {
            return ArmorPiece.CHESTPLATE;
        } else if (currentItem.getType() == Material.DIAMOND_LEGGINGS ||
                currentItem.getType() == Material.IRON_LEGGINGS ||
                currentItem.getType() == Material.GOLDEN_LEGGINGS ||
                currentItem.getType() == Material.LEATHER_LEGGINGS ||
                currentItem.getType() == Material.CHAINMAIL_LEGGINGS ||
                currentItem.getType() == Material.NETHERITE_LEGGINGS) {
            return ArmorPiece.LEGGINGS;
        } else if (currentItem.getType() == Material.DIAMOND_BOOTS ||
                currentItem.getType() == Material.IRON_BOOTS ||
                currentItem.getType() == Material.GOLDEN_BOOTS ||
                currentItem.getType() == Material.LEATHER_BOOTS ||
                currentItem.getType() == Material.CHAINMAIL_BOOTS ||
                currentItem.getType() == Material.NETHERITE_BOOTS) {
            return ArmorPiece.BOOTS;
        } else {
            return null; // Not a valid armor piece
        }
    }

}
