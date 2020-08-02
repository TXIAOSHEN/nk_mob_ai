package me.onebone.actaeon.hook;

import cn.nukkit.entity.Entity;
import me.onebone.actaeon.entity.MovingEntity;
import me.onebone.actaeon.task.AttackTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AttackHook
 * ===============
 * author: boybook
 * ===============
 */
public class AttackHook extends MovingEntityHook {

    private Entity parentEntity;
    private long lastAttack = 0;
    private double attackDistance;
    private long coolDown;
    private int effectual;  //攻击成功率 0~10
    private double viewAngle;  //机器人视野范围（攻击有效范围）
    private boolean jump;  //是否自动跳劈
    private float damage;
    private final List<AttackTask.AttackCallback> callbacks = new ArrayList<>();

    public AttackHook(MovingEntity entity) {
        this(entity, 2.6, 2, 250, 6, 75);
    }

    public AttackHook(MovingEntity bot, double attackDistance, float damage, long coolDown, int effectual, double viewAngle) {
        this(bot, null, attackDistance, damage, coolDown, effectual, viewAngle);
    }

    public AttackHook(MovingEntity bot, Entity parentEntity, double attackDistance, float damage, long coolDown, int effectual, double viewAngle) {
        super(bot);
        this.parentEntity = parentEntity;
        this.attackDistance = attackDistance;
        this.damage = damage;
        this.coolDown = coolDown;
        this.effectual = effectual;
        this.viewAngle = viewAngle;
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

    public boolean canJump() {
        return jump;
    }

    public AttackHook setJump(boolean jump) {
        this.jump = jump;
        return this;
    }

    public AttackHook addAttackCallback(AttackTask.AttackCallback callback) {
        this.callbacks.add(callback);
        return this;
    }

    @Override
    public void onUpdate(int tick) {
        if (this.entity.getHate() != null) {
            Entity hate = this.entity.getHate();
            if (this.entity.distance(hate) <= this.attackDistance) {
                if (System.currentTimeMillis() - this.lastAttack > this.coolDown) {
                    if (this.entity.getTask() == null) {
                        this.entity.updateBotTask(new AttackTask(this.entity, this.parentEntity, hate, this.damage, this.viewAngle, new Random().nextInt(10) < this.effectual, this.callbacks));
                    }
                    this.lastAttack = System.currentTimeMillis();
                    if (this.jump && new Random().nextBoolean()) this.entity.jump();
                }
            }
        }
    }
}
