package me.onebone.actaeon.target;

import cn.nukkit.math.Vector3;
import me.onebone.actaeon.entity.MovingEntity;

import java.util.Random;

public class PanicTargetFinder extends TargetFinder {

	private final Random random = new Random();
	private final double range;

	public PanicTargetFinder(MovingEntity entity, double range) {
		super(entity, 1000 * (new Random().nextInt(1) + 1));
		this.range = range;
	}

	@Override
	protected void find() {
		this.entity.setSprinting(true);
		this.entity.setTarget(new Vector3(this.entity.getX() + random.nextDouble() * range * 2 - range, this.entity.getY(), this.entity.getZ() + random.nextDouble() * range * 2 - range), "Panic", true);
	}

}