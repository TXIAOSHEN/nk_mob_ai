package me.onebone.actaeon.hook;

import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageEvent;
import me.onebone.actaeon.entity.IMovingEntity;

/**
 * ECPlayerBotHook
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public abstract class MovingEntityHook {

    protected final IMovingEntity entity;

    public MovingEntityHook(IMovingEntity entity) {
        this.entity = entity;
    }

    public IMovingEntity getEntity() {
        return entity;
    }

    public void onUpdate(int tick) {}

    public void onDamage(EntityDamageEvent event) {}

    public void onInteract(Player player) {}

}
