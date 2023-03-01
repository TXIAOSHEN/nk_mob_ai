package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityID;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.sound.SoundEnum;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.entity.Climbable;
import me.onebone.actaeon.entity.Fallable;
import me.onebone.actaeon.entity.monster.Monster;
import me.onebone.actaeon.target.AreaHaterTargetFinder;

import java.util.concurrent.ThreadLocalRandom;

public class EntityEvoker extends Monster implements EntityAgeable, Fallable, Climbable {
	public static final int NETWORK_ID = EntityID.EVOCATION_ILLAGER;

	public EntityEvoker(FullChunk chunk, CompoundTag nbt) {
		super(chunk, nbt);
        this.setTargetFinder(new AreaHaterTargetFinder(this, 500, 20000));
        this.addHook("attack", new EvokerAttackHook(this));
	}

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getLength() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    protected float getJumpPower() {
        return 0.42f;
    }

    @Override
    public Item[] getDrops() {
        return new Item[0];
    }

    @Override
    public float getDamage() {
        return super.getDamage() * 1.5f;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (source instanceof EntityDamageByChildEntityEvent && ((EntityDamageByChildEntityEvent) source).getChild() instanceof EntityEvocationFang) {
            return false;
        }
        return source.getCause() != EntityDamageEvent.DamageCause.FALL && super.attack(source);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (ThreadLocalRandom.current().nextInt(10) == 0) {
            this.level.addSound(this, SoundEnum.MOB_EVOCATION_ILLAGER_AMBIENT);
        }
        return super.onUpdate(currentTick);
    }
}
