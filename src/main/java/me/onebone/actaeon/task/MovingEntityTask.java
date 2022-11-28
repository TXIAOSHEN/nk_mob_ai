package me.onebone.actaeon.task;

import me.onebone.actaeon.entity.IMovingEntity;

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
}
