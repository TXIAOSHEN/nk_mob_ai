package me.onebone.actaeon.task;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.EntityEventPacket;
import me.onebone.actaeon.entity.MovingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * AttackTask
 * ===============
 * author: boybook
 * ===============
 */
public class AttackTask extends MovingEntityTask {

    private final Entity parentEntity;
    private final Entity target;
    private final float damage;
    private final double viewAngle;
    private final boolean effectual;
    private final List<AttackCallback> callbacks;

    public AttackTask(MovingEntity entity, Entity target, float damage, double viewAngle, boolean effectual) {
        this(entity, null, target, damage, viewAngle, effectual);
    }

    public AttackTask(MovingEntity entity, Entity parentEntity, Entity target, float damage, double viewAngle, boolean effectual) {
        this(entity, parentEntity, target, damage, viewAngle, effectual, new ArrayList<>());
    }

    public AttackTask(MovingEntity entity, Entity parentEntity, Entity target, float damage, double viewAngle, boolean effectual, List<AttackCallback> callbacks) {
        super(entity);
        this.parentEntity = parentEntity;
        this.target = target;
        this.damage = damage;
        this.viewAngle = viewAngle;
        this.effectual = effectual;
        this.callbacks = callbacks;
    }

    @Override
    public void onUpdate(int tick) {
        double angle = Math.atan2(this.target.z - this.entity.z, this.target.x - this.entity.x);
        double yaw = ((angle * 180) / Math.PI) - 90;
        double min = this.entity.yaw - this.viewAngle / 2;
        double max = this.entity.yaw + this.viewAngle / 2;
        boolean valid;
        if (min < 0) {
            valid = yaw > 360 + min || yaw < max;
        } else if (max > 360) {
            valid = yaw < max - 360 || yaw > min;
        } else {
            valid = yaw < max && yaw > min;
        }
        if (valid && this.effectual) {
            if (this.target.noDamageTicks <= 0) {
                EntityDamageByEntityEvent event;
                if (this.parentEntity != null) {
                    event = new EntityDamageByChildEntityEvent(this.parentEntity, this.getEntity(), this.target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, this.damage);
                } else {
                    event = new EntityDamageByEntityEvent(this.getEntity(), this.target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, this.damage);
                }
                this.callbacks.forEach(cb -> cb.callback(target, event));
                this.target.attack(event);
                this.target.noDamageTicks = 10;
            }
        }

        EntityEventPacket pk = new EntityEventPacket();
        pk.eid = this.entity.getId();
        pk.event = 4;
        Server.broadcastPacket(this.getEntity().getViewers().values(), pk);
        this.entity.updateBotTask(null);
    }

    @Override
    public void forceStop() {}

    @FunctionalInterface
    public interface AttackCallback {
        void callback(Entity target, EntityDamageByEntityEvent event);
    }
}
