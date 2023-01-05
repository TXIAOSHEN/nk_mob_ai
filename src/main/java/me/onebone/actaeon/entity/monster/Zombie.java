package me.onebone.actaeon.entity.monster;

import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.sound.SoundEnum;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.entity.Fallable;
import me.onebone.actaeon.hook.AttackHook;
import me.onebone.actaeon.target.AreaHaterTargetFinder;

import java.util.concurrent.ThreadLocalRandom;

public class Zombie extends Monster implements EntityAgeable, Fallable {
	public static final int NETWORK_ID = 32;

	public Zombie(FullChunk chunk, CompoundTag nbt) {
		super(chunk, nbt);
		this.setTargetFinder(new AreaHaterTargetFinder(this, 500, 20000));
		this.addHook("attack", new AttackHook(this, this.getAttackDistance(), this::getDamage, 1000, 10, 180));
	}

	@Override
	public float getWidth(){
		return 0.6f;
	}

	@Override
	public float getLength() {
		return 0.6f;
	}

	@Override
	protected float getGravity() {
		return 0.05f;
	}

	@Override
	public float getHeight(){
		if (isBaby()){
			return 0.8f;
		}
		return 1.8f;
	}

	@Override
	public float getEyeHeight(){
		if (isBaby()){
			return 0.51f;
		}
		return 1.62f;
	}

	@Override
	public Item[] getDrops(){
		return new Item[]{Item.get(Item.ROTTEN_FLESH)};
	}

	public double getAttackDistance() {
		return 1;
	}

	@Override
	public int getNetworkId(){
		return NETWORK_ID;
	}

	@Override
	protected void initEntity(){
		super.initEntity();
		setMaxHealth(20);
	}

	@Override
	public boolean isBaby(){
		return false;
	}

	@Override
	public boolean onUpdate(int currentTick) {
		boolean update = super.onUpdate(currentTick);
		if (ThreadLocalRandom.current().nextInt(200) == 0) {
			this.getLevel().addSound(this, SoundEnum.MOB_ZOMBIE_SAY);
		}
		return update;
	}
}
