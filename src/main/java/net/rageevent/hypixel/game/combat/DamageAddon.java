package net.rageevent.hypixel.game.combat;

import dev.prism.api.entity.DamageTypes;

public record DamageAddon(OperationType operationType, DamageType damageType, double value) {

    public static DamageAddon NONE = new DamageAddon(OperationType.FLAT,DamageType.DEFAULT,0);

    public static DamageAddon normal(OperationType operationType, double value){
        return new DamageAddon(operationType,DamageType.DEFAULT,value);
    }
    public static DamageAddon piercing(OperationType operationType, double value){
        return new DamageAddon(operationType,DamageType.PIERCING,value);
    }

}
