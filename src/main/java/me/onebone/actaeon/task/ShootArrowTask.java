package me.onebone.actaeon.task;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.EntityEventPacket;
import me.onebone.actaeon.entity.IMovingEntity;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ShootArrowTask extends MovingEntityTask {

    private final int fullTicks;
    private int ticks;
    private final Entity target;
    private final double pitch;

    private final double pow;
    private final double uncertainty;

    public ShootArrowTask(IMovingEntity entity, Entity target, double pitch) {
        this(entity, target, 40, pitch);
    }

    public ShootArrowTask(IMovingEntity entity, Entity target, int ticks, double pitch) {
        this(entity, target, ticks, pitch, 2);
    }

    public ShootArrowTask(IMovingEntity entity, Entity target, int ticks, double pitch, double pow) {
        this(entity, target, ticks, pitch, pow, 1);
    }

    public ShootArrowTask(IMovingEntity entity, Entity target, int ticks, double pitch, double pow, double uncertainty) {
        super(entity);
        this.target = target;
        this.fullTicks = ticks;
        this.ticks = ticks;
        this.pitch = pitch;
        this.pow = pow;
        this.uncertainty = uncertainty;
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
                double angle = Mth.atan2(this.target.z - this.getEntity().getZ(), this.target.x - this.getEntity().getZ());
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
            if (this.target != null) {
                this.getEntity().getEntity().setDataProperty(new LongEntityData(Entity.DATA_TARGET_EID, this.target.getId()));
            }

            if (fullTicks == 0) forceStop();

            this.getEntity().getEntity().setMovementSpeed(0f);
            // this.getEntity().getEntity().setImmobile();
        } else if (this.ticks <= 0) {
            Vector3 dir = this.getEntity().getEntity().getDirectionVector();
            if (uncertainty != 0) {
                Random random = ThreadLocalRandom.current();
                double offset = uncertainty * 0.0074999998;
                dir = dir.add(offset * random.nextGaussian(), offset * random.nextGaussian(), offset * random.nextGaussian());
            }
            // 射出弓箭
            EntityArrow arrow = new EntityArrow(
                    this.getEntity().getEntity().getChunk(),
                    Entity.getDefaultNBT(this.getEntity().getEntity().add(0, this.getEntity().getEntity().getEyeHeight(), 0), dir.multiply(pow),
                            (float) Mth.atan2(dir.x, dir.z) * Mth.RAD_TO_DEG,
                            (float) Mth.atan2(dir.y, dir.horizontalDistance()) * Mth.RAD_TO_DEG),
                    this.getEntity().getEntity()
            );
            arrow.setPickupMode(EntityArrow.PICKUP_NONE);
            arrow.spawnToAll();
            this.getEntity().getEntity().setMovementSpeed(0.1f);
            this.getEntity().getEntity().setDataProperty(new LongEntityData(Entity.DATA_TARGET_EID, 0));
            this.getEntity().updateBotTask(null);
        } else {
            this.getEntity().setLookAtFront(false);
            this.getEntity().getEntity().setSprinting(false);
            this.getEntity().getEntity().setSneaking(false);
            double angle = Mth.atan2(this.target.z - this.getEntity().getZ(), this.target.x - this.getEntity().getX());
            double yaw = (float) ((angle * 180) / Math.PI) - 90;
            double distance = this.getEntity().distance(this.target);
            double pitch = -Math.toDegrees(Math.asin((this.target.y - this.getEntity().getY()) / distance));
            this.getEntity().getEntity().setRotation(yaw, pitch - (1 - 1 / distance) * 5);
            this.getEntity().getEntity().setMovementSpeed(0f);
            // this.getEntity().getEntity().setImmobile();
        }
        this.ticks--;
    }

    @Override
    public void forceStop() {
        this.getEntity().getEntity().setSprinting(false);
        this.getEntity().getEntity().setSneaking(false);
        this.getEntity().getEntity().setMovementSpeed(0.1f);
        this.getEntity().setLookAtFront(true);
    }

}
