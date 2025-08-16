package plugin.sirlich.skills.meta;

import plugin.sirlich.skills.meta.AffectType;

public enum ClassType
{
    HEAD(true),
    BREAST(true),
    LEGS(true),
    FEET(true),
    UNDEFINED(true);

    private final boolean canUseBow;

    ClassType(boolean canUseBow) {
        this.canUseBow = canUseBow;
    }

    public boolean canUseBow(){
        return canUseBow;
    }

}
