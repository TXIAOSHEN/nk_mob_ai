package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.EntityEventPacket;

/**
 * net.easecation.ecgrave.entity
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public class EntityEvocationFang extends Entity {
    public static final int NETWORK_ID = 103;

    private Entity owner;
    private float damage;

    public EntityEvocationFang(FullChunk chunk, CompoundTag nbt, Entity owner, float damage) {
        super(chunk, nbt);
        this.owner = owner;
        this.damage = damage;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void spawnTo(Player player) {
        if (this.hasSpawned.containsKey(player.getLoaderId())) return;
        AddEntityPacket pk = new AddEntityPacket();
        pk.type = this.getNetworkId();
        pk.entityUniqueId = this.getId();
        pk.entityRuntimeId = this.getId();
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.speedX = (float) this.motionX;
        pk.speedY = (float) this.motionY;
        pk.speedZ = (float) this.motionZ;
        pk.metadata = this.dataProperties;
        player.dataPacket(pk);
        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return false; //无敌
    }

    @Override
    public boolean onUpdate(int currentTick) {
        super.onUpdate(currentTick);
        if (this.isAlive()) {
            this.setDataProperty(new ShortEntityData(80, this.age));
            //Server.getInstance().getLogger().info("EntityEvocationFang " + this.getId() + " age=" + this.age);
            if (this.age == 5) {
                //this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_EVOKER_SPELL, true);
            } else if (this.age == 20) {
                Entity[] entities = this.getLevel().getNearbyEntities(this.getBoundingBox().grow(0.6, 2, 0.6));
                for (Entity entity: entities) {
                    if (entity != this.owner) entity.attack(new EntityDamageByChildEntityEvent(this.owner, this, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, this.damage));
                }

                EntityEventPacket pk = new EntityEventPacket();
                pk.eid = this.getId();
                pk.event = 4;
                Server.broadcastPacket(this.hasSpawned.values().toArray(new Player[0]), pk);
            } else if (this.age >= 40) {
                this.kill();
                this.close();
            }
        }

        return this.isAlive();
    }
}
