package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BossEventPacket;
import cn.nukkit.utils.TextFormat;
import me.onebone.actaeon.entity.Climbable;
import me.onebone.actaeon.entity.Fallable;
import me.onebone.actaeon.entity.monster.Monster;
import me.onebone.actaeon.target.AreaHaterTargetFinder;

public class GraveEvokerBoss extends Monster implements EntityAgeable, Fallable, Climbable {
	public static final int NETWORK_ID = 104;

	public GraveEvokerBoss(FullChunk chunk, CompoundTag nbt) {
		super(chunk, nbt);
        this.setTargetFinder(new AreaHaterTargetFinder(this, 500, 20000));
        this.setScale(1.2f);
        this.setNameTag(TextFormat.BOLD.toString() + TextFormat.RED.toString() + "Evoker Boss");
        this.setNameTagVisible();
        this.setNameTagAlwaysVisible();
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
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.ENCHANTED_GOLDEN_APPLE)};
    }

    @Override
    public float getDamage() {
        return super.getDamage() * 1.5f;
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);
        BossEventPacket pk = new BossEventPacket();
        //pk.bossEid = this.getId();
        //pk.type = BossEventPacket.TYPE_SHOW;
        //pk.title = this.getNameTag();
        player.dataPacket(pk);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return source.getCause() != EntityDamageEvent.DamageCause.FALL && super.attack(source);
    }
}
