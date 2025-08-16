package plugin.sirlich.core;

// import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.sirlich.SkillScheme;
import plugin.sirlich.skills.meta.*;
import plugin.sirlich.utilities.WeaponUtils;
import plugin.sirlich.utilities.Xliff;
import plugin.sirlich.utilities.Color;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;

import java.lang.reflect.Constructor;
import java.util.*;

public class RpgPlayer
{

    /*
    RPGPLAYER LIST STUFF
     */
    public static HashMap<UUID, RpgPlayer> rpgPlayerHashMap = new HashMap<UUID, RpgPlayer>();
    public static HashMap<RpgPlayer, UUID> playerHashMap = new HashMap<RpgPlayer, UUID>();

    public static RpgPlayer getRpgPlayer(Player player) {
        return rpgPlayerHashMap.get(player.getUniqueId());
    }

    public static RpgPlayer getRpgPlayer(String name) {
        return rpgPlayerHashMap.get(Bukkit.getPlayer(name).getUniqueId());
    }

    public static RpgPlayer getRpgPlayer(UUID uuid) {
        return rpgPlayerHashMap.get(uuid);
    }

    public static Collection<RpgPlayer> getRpgPlayers() {
        return rpgPlayerHashMap.values();
    }

    public static Player getPlayer(RpgPlayer rpgPlayer) {
        return Bukkit.getPlayer(playerHashMap.get(rpgPlayer));
    }

    public static boolean isRpgPlayer(Player player) {
        return rpgPlayerHashMap.containsKey(player.getUniqueId());
    }

    public static boolean isRpgPlayer(UUID uuid) {
        return rpgPlayerHashMap.containsKey(uuid);
    }

    public static RpgPlayer addPlayer(Player player) {
        RpgPlayer rpgPlayer = new RpgPlayer(player);
        rpgPlayer.classType = ClassType.UNDEFINED;
        rpgPlayer.headType = ClassType.UNDEFINED;
        rpgPlayer.chestType = ClassType.UNDEFINED;
        rpgPlayer.legsType = ClassType.UNDEFINED;
        rpgPlayer.bootsType = ClassType.UNDEFINED;
        rpgPlayerHashMap.put(player.getUniqueId(), rpgPlayer);
        playerHashMap.put(rpgPlayer, player.getUniqueId());
        System.out.println("Adding new player to RpgPlayer: " + rpgPlayer.getName());
        return rpgPlayer;
    }

    public static void removePlayer(Player player) {
        RpgPlayer rpgPlayer = rpgPlayerHashMap.get(player.getUniqueId());
        rpgPlayer.clearActiveSkills(true, -1);
        rpgPlayer.setWalkSpeedModifier(0.2);
        playerHashMap.remove(rpgPlayer);
        rpgPlayerHashMap.remove(player.getUniqueId());
    }


    /*
    END RPGPLAYER LIST STUFF
     */

    private ClassType classType;
    private ClassType headType;
    private ClassType chestType;
    private ClassType legsType;
    private ClassType bootsType;


    private boolean drawingBow;

    private boolean silenced;

    private long startedDrawing;

    private boolean modifierActive = false;

    private boolean justAttacked = false;

    private UUID sessionToken;

    private Long lastDamaged = 0L;


    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public ClassType getClassTypeFour(int z) {
        switch(z) {
            case 0:
                return headType;
            case 1:
                return chestType;
            case 2:
                return legsType;
            case 3:
                return bootsType;
            default:
                return ClassType.UNDEFINED;
        }
    }

    public void setClassTypeFour(ClassType classType, int z) {
        switch(z) {
            case 0:
                this.headType = classType;
                break;
            case 1:
                this.chestType = classType;
                break;
            case 2:
                this.legsType = classType;
                break;
            case 3:
                this.bootsType = classType;
                break;
            default:
        }
    }

    //This method determines whether or not the player is using a mana skill. The player cannot get mana when he
    //is currently using a skill. Ie, he must toggle off to charge.
    public boolean isModifierActive(){
        return modifierActive;
    }

    public void setModifierActive(boolean a){
        modifierActive = a;
    }

    public boolean isDrawingBow() {
        return drawingBow;
    }

    public boolean isBowFullyCharged(){
        return startedDrawing + 1200 < System.currentTimeMillis();
    }

    public void setDrawingBow(boolean drawingBow) {
        if(drawingBow){
            startedDrawing = System.currentTimeMillis();
        }
        this.drawingBow = drawingBow;
    }

    public int getMana(){
        return Math.round(getPlayer().getExp() * 100);
    }

    public void addHealth(double health){
        if(this.getPlayer().getHealth() + health <= player.getMaxHealth()){
            getPlayer().setHealth(this.player.getHealth() + health);
        } else {
            this.getPlayer().setHealth(player.getMaxHealth());
        }
    }

    public boolean hasEnoughMana(int mana){
        return getMana() >= mana;
    }

    public void addMana(int mana){
        float newMana = getPlayer().getExp() + ((float)mana)/100;
        if(newMana < 0){
            newMana = 0;
        } else if(newMana > 0.95f){
            newMana = 0.95f;
        }
        getPlayer().setExp(newMana);
    }

    public RpgPlayer(Player player){
        RpgPlayer rpgPlayer = RpgPlayer.getRpgPlayer(player);

        this.sessionToken = UUID.randomUUID();
        this.player = player;
        this.team = "Default";
    }

    public boolean isSilenced() {
        return silenced;
    }

    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }



    public void refreshSessionToken(){
        this.sessionToken = UUID.randomUUID();
    }

    public boolean testSession(UUID token){
        return this.sessionToken.equals(token);
    }

    public boolean testSession(Skill skill){
        return this.sessionToken.equals(skill.getSessionToken());
    }

    public Long getLastDamaged() {
        return lastDamaged;
    }

    public void setLastDamaged(Long lastDamaged) {
        this.lastDamaged = lastDamaged;
    }

    public UUID getSessionToken(){
        return sessionToken;
    }
    private PlayerState playerState;
    private String team;
    private double walkSpeedModifier;

    public void refreshPassiveModifiers(){
        float walkSpeed = (float) walkSpeedModifier + 0.2f;
        if(walkSpeed <= 0){
            getPlayer().setWalkSpeed(0);
        } else if(walkSpeed >= 1){
            getPlayer().setWalkSpeed(1);
        } else {
            getPlayer().setWalkSpeed(walkSpeed);
        }
    }

    //The actual list of current skills that the player is using
    private ArrayList<Skill> activeSkillList = new ArrayList<Skill>();

    private ArrayList<Skill> activeSkillListHead = new ArrayList<Skill>();
    private ArrayList<Skill> activeSkillListChest = new ArrayList<Skill>();
    private ArrayList<Skill> activeSkillListLegs = new ArrayList<Skill>();
    private ArrayList<Skill> activeSkillListFeet = new ArrayList<Skill>();

    //The list of loadouts the player has access to. This is based on their past editing.
    private HashMap<ClassType, Loadout> loadouts = new HashMap<ClassType, Loadout>();

    public Loadout addLoadout(ClassType classType, Loadout loadout){
        loadouts.put(classType, loadout);
        return loadout;
    }

    public ArrayList<Skill> getActiveSkillList(){
        //return activeSkillList;
        ArrayList<Skill> combined = new ArrayList<Skill>();
        activeSkillListHead.forEach( (z) -> {combined.add(z);});
        activeSkillListChest.forEach( (z) -> {combined.add(z);});
        activeSkillListLegs.forEach( (z) -> {combined.add(z);});
        activeSkillListFeet.forEach( (z) -> {combined.add(z);});
        
        // .addAll(activeSkillListChest.addAll(activeSkillListLegs.addAll(activeSkillListFeet)));
        return combined;
    }

    public ArrayList<Skill> getFourActiveSkillList(int place) {
        switch(place) {
            case 0:
                return activeSkillListHead;
            case 1:
                return activeSkillListChest;
            case 2:
                return activeSkillListLegs;
            case 3:
                return activeSkillListFeet;
            default:
                return activeSkillList;
        }
    }

    public boolean hasSkill(SkillType skillType){
        //.addAll(activeSkillListLegs.addAll(activeSkillListFeet))).contains(skillType
        ArrayList<Skill> combined = activeSkillListHead;
        activeSkillListChest.forEach( (z) -> {combined.add(z);});
        activeSkillListLegs.forEach( (z) -> {combined.add(z);});
        activeSkillListFeet.forEach( (z) -> {combined.add(z);});
        return combined.contains(skillType);
    }

    public void addSkill(SimpleSkill simpleSkill, int armorSlot){
        System.out.println("Adding skill!");
        addSkill(simpleSkill.getSkillType(), simpleSkill.getLevel() - 1, armorSlot);
    }

    //Saves the edited loadout back into the player
    public void saveLoadout(ClassType classType, Loadout loadout){
        loadouts.put(classType, loadout);
    }

    //Apply skills for a specific class (usually based on armor)
    public void applySkills(ClassType classType, int z){
        refreshSessionToken();
        playSound(Sound.ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER);
        clearActiveSkills(false, z);
        setClassTypeFour(classType, z);

        //Place holder for the eventual addition of class helmets.
        // getPlayer().getInventory().setHelmet(new ItemStack(Material.GRAY_DYE));

        tell("Your skills for " + classType + " have been applied:");
        for(SimpleSkill simpleSkill : getLoadout(classType).getSimpleSkills()){
            tell(Color.aqua + simpleSkill.getSkillType().getSkill().getName() + ": Level " + Color.gray + simpleSkill.getLevel().toString());
            //Transfers the magic of reflection somewhere else.
            addSkill(simpleSkill.getSkillType(), simpleSkill.getLevel() - 1, z);
        }
    }

    //Apply skills from armor, delayed
    public void applySkillsFromArmor(UUID uuid){
        final UUID saved_uuid = uuid;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SkillScheme.getInstance(), new Runnable() {
            public void run() {
                try {
                    RpgPlayer.getRpgPlayer(Bukkit.getPlayer(saved_uuid)).applySkillsFromArmor();
                    // ClassType wearing = WeaponUtils.getClassTypeFromArmor(getPlayer());
                    //This is cause sometimes players will queue up multiple pieces of armor at once,
                    //and cause spam.
                    // if(wearing != classType){
                    //     RpgPlayer.getRpgPlayer(Bukkit.getPlayer(saved_uuid)).applySkillsFromArmor();
                    // }
                } catch (Exception e){
                    System.out.println("That player UUID is null!");
                }
            }
        }, 5);
    }

    //Apply skills based on player armor
    public void applySkillsFromArmor(){
        // if(WeaponUtils.isWearingFullSet(getPlayer())){
        //     ClassType wearing = WeaponUtils.getClassTypeFromArmor(getPlayer());
        //     RpgPlayer.getRpgPlayer(getPlayer()).applySkills(wearing, -1);
        // }
        // WIP for armor slots as classes
        for (int z = 0; z < 4; z++) {
            ClassType armorType = WeaponUtils.getSkillClassFromArmorSlot(getPlayer(), z);
            if (armorType != ClassType.UNDEFINED) {
                RpgPlayer.getRpgPlayer(getPlayer()).applySkills(armorType, z);
            }
            else if (getClassTypeFour(z) != ClassType.UNDEFINED) {
                tell("You unequipped your class " + getClassTypeFour(z) + ".");
                playSound(Sound.ENTITY_VILLAGER_NO);
                setClassTypeFour(ClassType.UNDEFINED, z);
                clearActiveSkills(false, z);
            }
        }

        //Players with a UNDEFINED class should'nt get spammed
        // else if(getClassType() != ClassType.UNDEFINED){
        //     tell("You unequipped your class.");
        //     playSound(Sound.ENTITY_VILLAGER_NO);
        //     setClassType(ClassType.UNDEFINED);
        //     clearActiveSkills();
        // }
    }

    //Actual method for adding a singular skill onto the players activeSkillList
    public void addSkill(SkillType skillType, int level, int armorSlot){
        try{
            // System.out.println(skillType);
            // System.out.println(level);
            Class clazz = skillType.getSkillClass();
            Constructor<Skill> constructor = clazz.getConstructor(RpgPlayer.class,int.class);
            Skill skill = (Skill) constructor.newInstance(this,level);
            skill.onEnable();
            getFourActiveSkillList(armorSlot).add(skill);
            // switch(armorSlot) {
            //     case 0:
            //         activeSkillListHead.add(skill);
            //         break;
            //     case 1:
            //         activeSkillListChest.add(skill);
            //         break;
            //     case 2:
            //         activeSkillListLegs.add(skill);
            //         break;
            //     case 3:
            //         activeSkillListFeet.add(skill);
            //         break;
            //     default:
            //         activeSkillList.add(skill);
            // }
            refreshPassiveModifiers();
        } catch (Exception e){
            System.out.println("WARNING! Something terrible has occurred in the reflection.");
        }
    }

    public void removeAllPotionEffects(){
        for (PotionEffect effect : player.getActivePotionEffects()){
            player.removePotionEffect(effect.getType());
        }
    }
    public void wipe(){
        getPlayer().getInventory().clear();
        getPlayer().setHealth(20);
        getPlayer().setExp(0);
        getPlayer().setFoodLevel(20);
        removeAllPotionEffects();
        clearActiveSkills(true, -1);
    }

    public String getName(){
        return getPlayer().getName();
    }

    public void teleport(Location location){
        location.setWorld(getPlayer().getLocation().getWorld());
        getPlayer().teleport(location);
    }
    // fully remove, partially remove and which one if partial
    public void clearActiveSkills(boolean fullRemove, int index){
        if (fullRemove) {
            for (int z = 0; z < 4; z++) {
                for (Skill skill : getFourActiveSkillList(z)) {
                    skill.onDisable();
                }
                getFourActiveSkillList(z).clear();
            }
        }
        else {
            if (index >= 0 || index < 4) {
                for (Skill skill : getFourActiveSkillList(index)) {
                    skill.onDisable();
                }
                getFourActiveSkillList(index).clear();
            }
        }
        // for(Skill skill : activeSkillList){
        //     skill.onDisable();
        // }
        refreshSessionToken();
        refreshPassiveModifiers();
        // activeSkillList.clear();
    }

    public boolean didJustAttack(){
        return justAttacked;
    }

    public void logPlayerAttack(){
        justAttacked = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                justAttacked = false;
            }

        }.runTaskLater(SkillScheme.getInstance(), 1);
    }

    public void playSoundX(String sound){
        playSound(Xliff.getSound(sound));
    }

    public void playWorldSoundX(String sound){
        playWorldSoundX(sound, 1, 1);
    }

    public void playWorldSoundX(String sound, float volume, float speed){
        playWorldSound(Xliff.getSound(sound), volume, speed);
    }

    public void playWorldSound(Sound sound){
        playWorldSound(sound, 1, 1);
    }

    public void playWorldSound(String sound, float volume, float speed){
        playWorldSound(Sound.valueOf(sound), volume, speed);
    }

    public void playWorldSound(String sound){
        playWorldSound(sound, 1, 1);
    }
    public void playWorldSound(Sound sound, float volume, float speed){
        getPlayer().getWorld().playSound(getPlayer().getLocation(), sound, volume, speed);
    }

    public void playSound(Sound sound, float speed){
        playWorldSound(sound, speed, 1);
    }

    public void playSound(Sound sound){
        playSound(sound, 1, 1);
    }

    public void playSound(Sound sound, float speed, float pitch){
        if(sound != null){
            getPlayer().playSound(getPlayer().getLocation(),sound,speed,pitch);
        }
    }

    public void setActionBar(String message){
        // public static void displaySubTitle(Player player, String text, int fadeIn, int stay, int fadeOut) {
        // if (player != null && player.isOnline()) {
        // getPlayer().sendTitle("", message, 1, 2, 1);
        getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        // } else {
        // Bukkit.getLogger().warning("[Legends] Cannot send subtitle. Player is null or offline.");
        // }
        // ActionBarAPI.sendActionBar(getPlayer(),message);
        // player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(displayText));
    }

    public Player getPlayer()
    {
        return player;
    }

    public void tell(String message){
        getPlayer().sendMessage(Color.green + message);
    }

    public void tellX(String message){
        tell(Xliff.getXliff(message));
    }

    public void giveKit(String kit) {
        // if(SkillData.kitExists(kit)){
        //     for(SimpleSkill simpleSkill : SkillData.getKit(kit)){
        //         this.addSkill(simpleSkill);
        //     }
        // } else {
            tellX("RpgPlayer.that_kit_does_not_exist");
        // }
    }

    //TODO Eventually add method here with Bucket.broadcastMessage()
    //public void say(String message){}

    private Player player;

    public double getWalkSpeedModifier()
    {
        return walkSpeedModifier;
    }

    public void setWalkSpeedModifier(double walkSpeedModifier)
    {
        this.walkSpeedModifier = walkSpeedModifier;
    }

    public void editWalkSpeedModifier(double change){
        this.walkSpeedModifier += change;
        refreshPassiveModifiers();
    }

    //Returns loadout based on current class type of the rpgPlayer
    public Loadout getLoadout()
    {
        return loadouts.get(this.classType);
    }

    public HashMap<ClassType, Loadout> getLoadouts() {
        return loadouts;
    }

    //Returns loadout based on the passed in class type
    public Loadout getLoadout(ClassType classType){
        return  loadouts.get(classType);
    }

    public PlayerState getPlayerState()
    {
        return playerState;
    }

    //Set player state, affecting various things as well
    public void setPlayerState(PlayerState playerState)
    {
        this.playerState = playerState;

        if(playerState == PlayerState.HUB){
            wipe();
        } else if(playerState == PlayerState.SPECTATOR){
            wipe();
        } else if(playerState == PlayerState.LOBBY){
            wipe();
        } else if(playerState == PlayerState.GAME || playerState == PlayerState.TESTING){
            getLoadout().giveArmorLoadout();
            applySkillsFromArmor();
        } else if(playerState == PlayerState.CLANS){
            applySkillsFromArmor();
        }
    }

    public static boolean isSameTeam(RpgPlayer a, RpgPlayer b){
        return a.getTeam().equals(b.getTeam());
    }

    public String getTeam()
    {
        return team;
    }


    public void setTeam(String team)
    {
        this.team = team;
    }

    public Arrow shootArrow(Vector velocity) {
        return shootArrow(velocity,null);
    }

    public Arrow shootArrow(Vector velocity, String tag){
        Arrow arrow = this.getPlayer().launchProjectile(Arrow.class);
        arrow.setVelocity(velocity);
        RpgProjectile rpgProjectile = RpgProjectile.registerProjectile(arrow,this);
        if(tag != null){
            rpgProjectile.addTag(tag);
        }
        return arrow;
    }

}
