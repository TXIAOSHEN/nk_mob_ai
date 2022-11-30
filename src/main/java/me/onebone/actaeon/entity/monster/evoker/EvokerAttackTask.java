package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import me.onebone.actaeon.entity.IMovingEntity;
import me.onebone.actaeon.task.MovingEntityTask;

/**
 * AttackTask
 * ===============
 * author: boybook
 * ===============
 */
public class EvokerAttackTask extends MovingEntityTask {

    private Vector3 target;
    private float damage;
    private int index = 1;

    public EvokerAttackTask(IMovingEntity entity, Entity target, float damage) {
        super(entity);
        this.target = target.getPosition();
        this.damage = damage;
        this.getEntity().getEntity().setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_EVOKER_SPELL, true);
    }

    @Override
    public void onUpdate(int tick) {
        if (index <= 12) {
            Vector2 v2 = new Vector2(this.target.x - this.entity.getX(), this.target.z - this.entity.getZ());
            v2 = v2.normalize();

            double x = this.getEntity().getX() + v2.multiply(index).getX();
            double z = this.getEntity().getZ() + v2.multiply(index).getY();
            double y = this.getEntity().getY();
            CompoundTag nbt = new CompoundTag()
                    .putList(new ListTag<DoubleTag>("Pos")
                            .add(new DoubleTag("", x))
                            .add(new DoubleTag("", y))
                            .add(new DoubleTag("", z)))
                    .putList(new ListTag<DoubleTag>("Motion")
                            .add(new DoubleTag("", 0))
                            .add(new DoubleTag("", 0))
                            .add(new DoubleTag("", 0)))
                    .putList(new ListTag<FloatTag>("Rotation")
                            .add(new FloatTag("", (float) 0))
                            .add(new FloatTag("", (float) 0))
                    );
            EntityEvocationFang fang = new EntityEvocationFang(this.getEntity().getLevel().getChunk((int)x >> 4, (int)z >> 4), nbt, this.getEntity().getEntity(), this.damage);
            fang.spawnToAll();
        }

        if (index++ > 50) this.entity.updateBotTask(null);
    }

    @Override
    public void forceStop() {
        this.getEntity().getEntity().setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_EVOKER_SPELL, false);
    }

}
