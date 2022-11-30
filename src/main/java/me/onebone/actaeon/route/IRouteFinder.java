package me.onebone.actaeon.route;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import net.easecation.eccommons.promise.AsyncPromise;

import java.util.List;

public interface IRouteFinder {

    /**
     * 寻路的主要运算逻辑
     */
    AsyncPromise<List<Node>> search(Entity entity, Position start, Position destination);

    /**
     * @return 每次寻路开始到下次寻路的冷却时间
     */
    long getRouteFindCooldown();

}
