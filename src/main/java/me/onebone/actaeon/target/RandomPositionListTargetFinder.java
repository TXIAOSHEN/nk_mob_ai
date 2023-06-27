package me.onebone.actaeon.target;

import cn.nukkit.math.Vector3;
import me.onebone.actaeon.entity.IMovingEntity;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomPositionListTargetFinder extends TargetFinder {

	private final List<Vector3> positionList;

	public RandomPositionListTargetFinder(IMovingEntity entity, long interval, List<Vector3> positionList) {
		super(entity, interval);
		this.positionList = positionList;
	}

	@Override
	protected void find() {
		Random random = ThreadLocalRandom.current();
		Vector3 pos = this.positionList.get(random.nextInt(this.positionList.size()));
		this.entity.setTarget(pos, "RandomPositionList", true);
	}

}
