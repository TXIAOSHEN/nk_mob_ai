package me.onebone.actaeon.route;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import net.easecation.eccommons.promise.AsyncPromise;

import java.util.ArrayList;
import java.util.List;

public class SimpleRouteFinder implements IRouteFinder {

	public long getRouteFindCooldown() {
		return 100;
	}

	@Override
	public AsyncPromise<List<Node>> search(Entity entity, Position start, Position destination) {
		List<Node> result = new ArrayList<>();
		result.add(new Node(destination));  // just go straight
		return AsyncPromise.success(result);
	}

}
