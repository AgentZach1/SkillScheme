package plugin.sirlich.skills.clans.boots;

import plugin.sirlich.SkillScheme;
import plugin.sirlich.core.RpgPlayer;
import plugin.sirlich.skills.meta.TickingSkill;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Vigilance extends TickingSkill{

    private int strength;
    
    public Vigilance(RpgPlayer rpgPlayer, int level) {
        super(rpgPlayer,level,"Vigilance");
        this.strength = data.getInt("strength");
    }

    public void onTick() {
        if(isSilenced()){return;};
        getRpgPlayer().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, strength));
    }
}
