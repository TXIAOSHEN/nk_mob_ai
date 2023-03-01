package me.onebone.actaeon.hook;

import cn.nukkit.Difficulty;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import me.onebone.actaeon.entity.IMovingEntity;
import me.onebone.actaeon.task.MovingEntityTask;
import me.onebone.actaeon.task.ShootArrowTask;

import java.util.Arrays;

/**
 * ECPlayerBotAttackHook
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public class ShootArrowHook extends MovingEntityHook {

    public interface ShootArrowTaskSupplier {
        MovingEntityTask get(IMovingEntity entity, Entity target, int ticks, double pitch, double pow, double uncertainty);

        static ShootArrowTaskSupplier ofDefault() {
            return ShootArrowTask::new;
        }
    }

    private long lastAttack = 0;
    private final double minDistance;
    private final double maxDistance;
    private long coolDown;
    private final int ticks;
    private final double pitch;
    private ShootArrowTaskSupplier shootArrowTaskSupplier = ShootArrowTaskSupplier.ofDefault();

    private double pow = 2;
    private double uncertainty = 1;

    public ShootArrowHook(IMovingEntity entity) {
        this(entity, 2, 30, 5000);
    }

    public ShootArrowHook(IMovingEntity entity, double minDistance, double maxDistance, long coolDown) {
        this(entity, minDistance, maxDistance, coolDown, 40);
    }

    public ShootArrowHook(IMovingEntity entity, double minDistance, double maxDistance, long coolDown, int ticks) {
        this(entity, minDistance, maxDistance, coolDown, ticks, 5);
    }

    public ShootArrowHook(IMovingEntity entity, double minDistance, double maxDistance, long coolDown, int ticks, double pitch) {
        super(entity);
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.coolDown = coolDown;
        this.ticks = ticks;
        this.pitch = pitch;
    }

    public ShootArrowHook setShootArrowTaskSupplier(ShootArrowTaskSupplier shootArrowTaskSupplier) {
        this.shootArrowTaskSupplier = shootArrowTaskSupplier;
        return this;
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
            double distance = this.entity.distance(hate);
            if (distance >= this.minDistance && distance <= this.maxDistance) {
                if (System.currentTimeMillis() - this.lastAttack > this.coolDown) {
                    try {
                        if (this.ticks == 0 || (this.entity.getEntity() != null && Arrays.stream(this.entity.getEntity().getLineOfSight((int) this.entity.distance(hate.getPosition().add(0, hate.getEyeHeight(), 0)))).noneMatch(Block::isSolid))) {  //可以直接看到目标
                            if (this.entity.getTask() == null) {
                                this.entity.updateBotTask(this.shootArrowTaskSupplier.get(this.entity, hate, this.ticks, this.pitch, pow, uncertainty));
                            }
                            this.lastAttack = System.currentTimeMillis();
                        }
                    } catch (Exception e) {
                        this.lastAttack = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    public double getPow() {
        return pow;
    }

    public ShootArrowHook setPow(double pow) {
        this.pow = pow;
        return this;
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public ShootArrowHook setUncertainty(double uncertainty) {
        this.uncertainty = uncertainty;
        return this;
    }

    public ShootArrowHook setUncertainty(Difficulty difficulty) {
        setUncertainty(getDifficultyUncertainty(difficulty));
        return this;
    }

    public static double getDifficultyUncertainty(Difficulty difficulty) {
        return 16 - difficulty.ordinal() * 4;
    }
}
