package me.onebone.actaeon.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import me.onebone.actaeon.route.Router;

public interface IMovingEntity {

    Entity getEntity();

    Level getLevel();

    Position getPosition();

    double getX();
    double getY();
    double getZ();

    double distanceSquared(Vector3 pos);

    double distance(Vector3 pos);

    String getName();

    default void setTarget(Vector3 vec, String identifier) {
        this.setTarget(vec, identifier, true);
    }

    void setTarget(Vector3 vec, String identifier, boolean immediate);

    Vector3 getRealTarget();

    Vector3 getTarget();

    void setHate(Entity hate);

    Router getRouter();

}
