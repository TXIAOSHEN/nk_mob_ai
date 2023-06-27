package me.onebone.actaeon.task;

import me.onebone.actaeon.entity.IMovingEntity;

import java.util.function.Consumer;

/**
 * MovingEntityTask
 * ===============
 * author: boybook
 * ===============
 */
public abstract class MovingEntityTask {

    protected IMovingEntity entity;

    public MovingEntityTask(IMovingEntity entity) {
        this.entity = entity;
    }

    public abstract void onUpdate(int tick);

    public abstract void forceStop();

    public IMovingEntity getEntity() {
        return entity;
    }

    public static MovingEntityTask ofSimple(IMovingEntity entity, int ticks, Consumer<IMovingEntity> stopCallback) {
        return new MovingEntityTask(entity) {
            private int ticksLeft = ticks;
            @Override
            public void onUpdate(int tick) {
                if (ticksLeft-- <= 0) {
                    this.getEntity().updateBotTask(null);
                }
            }
            @Override
            public void forceStop() {
                stopCallback.accept(this.getEntity());
            }
        };
    }
}
