package me.onebone.actaeon.task;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.network.protocol.EntityEventPacket;
import me.onebone.actaeon.entity.IMovingEntity;

public class ShootArrowTask extends MovingEntityTask {

    private final int fullTicks;
    private int ticks;
    private final Entity target;
    private final double pitch;

    public ShootArrowTask(IMovingEntity entity, Entity target, double pitch) {
        this(entity, target, 40, pitch);
    }

    public ShootArrowTask(IMovingEntity entity, Entity target, int ticks, double pitch) {
        super(entity);
        this.target = target;
        this.fullTicks = ticks;
        this.ticks = ticks;
        this.pitch = pitch;
    }

    @Override
    public void onUpdate(int tick) {
        if (this.ticks == fullTicks) {
            if (this.fullTicks > 0 && this.target == null) {
                this.getEntity().updateBotTask(null);
                return;
            }
            this.getEntity().setLookAtFront(false);
            this.getEntity().getEntity().setSprinting(false);
            this.getEntity().getEntity().setSneaking(false);
            if (this.target != null) {
                double angle = Math.atan2(this.target.z - this.getEntity().getZ(), this.target.x - this.getEntity().getZ());
                double yaw = (float) ((angle * 180) / Math.PI) - 90;
                double distance = this.getEntity().distance(this.target);
                double pitch = -Math.toDegrees(Math.asin((this.target.y - this.getEntity().getY()) / distance));
                this.getEntity().getEntity().setRotation(yaw, pitch - (1 - 1 / distance) * this.pitch);
            }

            // 拉弓
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = this.getEntity().getEntity().getId();
            pk.event = EntityEventPacket.USE_ITEM;
            Server.broadcastPacket(this.getEntity().getEntity().getViewers().values(), pk);

            if (fullTicks == 0) forceStop();

            //this.getEntity().setMovementSpeed(0.08f);
            this.getEntity().getEntity().setImmobile();
        } else if (this.ticks <= 0) {
            // 射出弓箭
            EntityArrow arrow = new EntityArrow(
                    this.getEntity().getEntity().getChunk(),
                    Entity.getDefaultNBT(this.getEntity().getEntity().add(0, this.getEntity().getEntity().getEyeHeight(), 0), this.getEntity().getEntity().getDirectionVector().multiply(2)),
                    this.getEntity().getEntity()
            );
            arrow.spawnToAll();
            this.getEntity().getEntity().setImmobile(false);
            this.getEntity().updateBotTask(null);
        } else {
            this.getEntity().setLookAtFront(false);
            this.getEntity().getEntity().setSprinting(false);
            this.getEntity().getEntity().setSneaking(false);
            double angle = Math.atan2(this.target.z - this.getEntity().getZ(), this.target.x - this.getEntity().getX());
            double yaw = (float) ((angle * 180) / Math.PI) - 90;
            double distance = this.getEntity().distance(this.target);
            double pitch = -Math.toDegrees(Math.asin((this.target.y - this.getEntity().getY()) / distance));
            this.getEntity().getEntity().setRotation(yaw, pitch - (1 - 1 / distance) * 5);
            //this.getEntity().setMovementSpeed(0.08f);
            this.getEntity().getEntity().setImmobile();
        }
        this.ticks--;
    }

    @Override
    public void forceStop() {
        this.getEntity().getEntity().setSprinting(false);
        this.getEntity().getEntity().setSneaking(false);
        this.getEntity().getEntity().setImmobile(false);
        this.getEntity().setLookAtFront(true);
    }

}
