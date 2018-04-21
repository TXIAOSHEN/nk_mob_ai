package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.entity.Entity;
import me.onebone.actaeon.entity.MovingEntity;
import me.onebone.actaeon.hook.MovingEntityHook;

/**
 * AttackHook
 * ===============
 * author: boybook
 * ===============
 */
public class EvokerAttackHook extends MovingEntityHook {

    private long lastAttack = 0;
    private double attackDistance;
    private long coolDown;
    private float damage;

    public EvokerAttackHook(MovingEntity entity) {
        this(entity, 10, 4, 1000);
    }

    public EvokerAttackHook(MovingEntity bot, double attackDistance, float damage, long coolDown) {
        super(bot);
        this.attackDistance = attackDistance;
        this.damage = damage;
        this.coolDown = coolDown;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public long getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(long coolDown) {
        this.coolDown = coolDown;
    }

    public long getLastAttack() {
        return lastAttack;
    }

    @Override
    public void onUpdate(int tick) {
        if (this.entity.getHate() != null) {
            Entity hate = this.entity.getHate();
            if (this.entity.distance(hate) <= this.attackDistance) {
                if (System.currentTimeMillis() - this.lastAttack > this.coolDown) {
                    if (this.entity.getTask() == null) {
                        this.entity.updateBotTask(new EvokerAttackTask(this.entity, hate, this.damage));
                    }
                    this.lastAttack = System.currentTimeMillis();
                }
            }
        }
    }
}
