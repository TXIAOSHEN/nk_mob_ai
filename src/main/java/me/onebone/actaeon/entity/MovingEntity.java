package me.onebone.actaeon.entity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.*;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.sound.SoundEnum;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import co.aikar.timings.Timing;
import co.aikar.timings.TimingsManager;
import me.onebone.actaeon.hook.MovingEntityHook;
import me.onebone.actaeon.inventory.EntityArmorInventory;
import me.onebone.actaeon.inventory.EntityEquipmentInventory;
import me.onebone.actaeon.route.Node;
import me.onebone.actaeon.route.Router;
import me.onebone.actaeon.target.TargetFinder;
import me.onebone.actaeon.task.MovingEntityTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

abstract public class MovingEntity extends EntityCreature implements IMovingEntity, InventoryHolder {

	public static final String TAG_MAINHAND = "Mainhand";
	public static final String TAG_OFFHAND = "Offhand";
	public static final String TAG_ARMOR = "Armor";

	private boolean isKnockback = false;
	private Router router;
	private TargetFinder targetFinder = null;
	private Vector3 target = null;
	private Entity hate = null;
	private String targetSetter = "";
	public boolean routeLeading = true;
	private final Map<String, MovingEntityHook> hooks = new HashMap<>();
	private MovingEntityTask task = null;
	private boolean lookAtFront = true;
	private boolean autoCollide = true;

	private EntityEquipmentInventory equipmentInventory;
	private EntityArmorInventory armorInventory;

	private final Timing movingEntityTiming;
	private final Timing movingEntityPart1Timing;
	private final Timing movingEntityPart2Timing;
	private final Timing movingEntityPart3Timing;
	private final Timing movingEntityPart4Timing;

	public MovingEntity(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);

		this.movingEntityTiming = TimingsManager.getTiming("MovingEntity<" + this.getClass().getSimpleName() + "> - onUpdate");
		this.movingEntityPart1Timing = TimingsManager.getTiming("MovingEntity<" + this.getClass().getSimpleName() + "> - onUpdate Part1");
		this.movingEntityPart2Timing = TimingsManager.getTiming("MovingEntity<" + this.getClass().getSimpleName() + "> - onUpdate Part2");
		this.movingEntityPart3Timing = TimingsManager.getTiming("MovingEntity<" + this.getClass().getSimpleName() + "> - onUpdate Part3");
		this.movingEntityPart4Timing = TimingsManager.getTiming("MovingEntity<" + this.getClass().getSimpleName() + "> - onUpdate Part4");

		this.router = new Router(this);
		this.setImmobile(false);
	}

	@Override
	protected void initEntity() {
		super.initEntity();

		// this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_NO_AI);
		this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_CAN_WALK);
		this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_GRAVITY);

		this.equipmentInventory = new EntityEquipmentInventory(this);
		this.armorInventory = new EntityArmorInventory(this);

		if (this.namedTag.contains(TAG_MAINHAND)) {
			this.equipmentInventory.setItemInHand(NBTIO.getItemHelper(this.namedTag.getCompound(TAG_MAINHAND)), true);
		}

		if (this.namedTag.contains(TAG_OFFHAND)) {
			this.equipmentInventory.setItemInOffhand(NBTIO.getItemHelper(this.namedTag.getCompound(TAG_OFFHAND)), true);
		}

		if (this.namedTag.contains(TAG_ARMOR)) {
			ListTag<CompoundTag> armorList = this.namedTag.getList(TAG_ARMOR, CompoundTag.class);
			for (CompoundTag armorTag : armorList.getAll()) {
				this.armorInventory.setItem(armorTag.getByte("Slot"), NBTIO.getItemHelper(armorTag));
			}
		}
	}

	@Override
	public EntityLiving getEntity() {
		return this;
	}

	public Map<String, MovingEntityHook> getHooks() {
		return hooks;
	}

	public void addHook(String key, MovingEntityHook hook) {
		this.hooks.put(key, hook);
	}

	@Override
	protected float getGravity() {
		return 0.092f;
	}

	public Entity getHate() {
		return hate;
	}

	public void setHate(Entity hate) {
		this.hate = hate;
	}

	public void jump() {
		if (this.onGround){
			this.motionY = 0.35;
		}
	}

	@Override
	public boolean onUpdate(int currentTick) {
		this.movingEntityTiming.startTiming();
		super.onUpdate(currentTick);
		this.movingEntityTiming.stopTiming();
		return true;
	}

	@Override
	public boolean entityBaseTick(int tickDiff){
		if(this.closed){
			return false;
		}

		this.movingEntityPart1Timing.startTiming();
		new ArrayList<>(this.hooks.values()).forEach(hook -> hook.onUpdate(Server.getInstance().getTick()));
		if (this.task != null) this.task.onUpdate(Server.getInstance().getTick());

		boolean hasUpdate = super.entityBaseTick(tickDiff);

		if (this.isKnockback) {                   // knockback 이 true 인 경우는 맞은 직후

		} else if(this.routeLeading && this.onGround) {
			this.motionX = this.motionZ = 0;
		}

		this.motionX *= (1 - this.getDrag());
		this.motionZ *= (1 - this.getDrag());
		if (this.motionX < 0.001 && this.motionX > -0.001) this.motionX = 0;
		if (this.motionZ < 0.001 && this.motionZ > -0.001) this.motionZ = 0;

		this.movingEntityPart1Timing.stopTiming();

		this.movingEntityPart2Timing.startTiming();

		if (this.targetFinder != null) this.targetFinder.onUpdate();

		// 如果没在寻路，但是设置了目标，达到了下次寻路计划的时间，就开始寻路
		if (this.routeLeading) {
			this.router.onTick();
			hasUpdate = true;
		}
		/*if (this.routeLeading && this.onGround
				&& this.hasSetTarget() &&
				!this.route.isSearching()
				&& System.currentTimeMillis() >= this.route.nextRouteFind
				&& (this.route.getDestination() == null || this.route.getDestination().distance(this.getTarget()) > 2))
		{ // 대상이 이동함

			if (RouteFinderSearchAsyncTask.getTaskSize() < 50) Server.getInstance().getScheduler().scheduleAsyncTask(new RouteFinderSearchAsyncTask(this.route, this.level, this, this.getTarget(), this.boundingBox));

			*//*if(this.route.isSearching()) this.route.research();
			else this.route.search();*//*

			hasUpdate = true;
		}*/

		this.movingEntityPart2Timing.stopTiming();

		if (!this.isImmobile()) {
			this.movingEntityPart3Timing.startTiming();
			// 如果未在寻路，并且有寻路路径，则控制实体前往下一个节点
			if (this.routeLeading && !this.isKnockback && !this.router.isSearching() && this.router.hasRoute()) { // entity has route to go
				hasUpdate = true;

				// 获取下一寻路的节点
				Node node = this.router.get();
				if (node != null) {
					//Server.broadcastPacket(level.getPlayers().values().stream().toArray(Player[]::new), new cn.nukkit.level.particle.RedstoneParticle(node.getVector3(), 2).encode()[0]);
					Vector3 vec = node.getVector3();
					double diffX = Math.pow(vec.x - this.x, 2);
					double diffZ = Math.pow(vec.z - this.z, 2);

					// 已经达到了节点
					if (diffX + diffZ == 0) {
						// 那么将节点调至下一个节点，如果没有下一个节点了，则到达目的地
						if (this.router.hasNext()) {
							this.router.next();
							//Server.getInstance().getLogger().warning(vec.toString());
						} else {
							this.router.arrived();
						}
					} else {
						int negX = vec.x - this.x < 0 ? -1 : 1;
						int negZ = vec.z - this.z < 0 ? -1 : 1;

						this.motionX = Math.min(Math.abs(vec.x - this.x), diffX / (diffX + diffZ) * this.getMovementSpeed()) * negX;
						this.motionZ = Math.min(Math.abs(vec.z - this.z), diffZ / (diffX + diffZ) * this.getMovementSpeed()) * negZ;
						if (this.lookAtFront) {
							double angle = Math.atan2(vec.z - this.z, vec.x - this.x);
							this.setRotation((angle * 180) / Math.PI - 90, 0);
						}
					}
				}
			}

			for (Entity entity: this.getLevel().getCollidingEntities(this.boundingBox)) {
				if (this.canCollide() && this.canCollideWith(entity)) {
					if (entity instanceof EntityHuman) this.onCollideWithPlayer((EntityHuman) entity);
					if (autoCollide) {
						Vector3 motion = this.subtract(entity);
						this.motionX += motion.x / 2;
						this.motionZ += motion.z / 2;
					}
				}
			}

			this.movingEntityPart3Timing.stopTiming();

			this.movingEntityPart4Timing.startTiming();

			if((this.motionX != 0 || this.motionZ != 0) && this.isCollidedHorizontally){
				this.jump();
			}
			this.move(this.motionX, this.motionY, this.motionZ);

			this.checkGround();
			if(!this.onGround){
				this.motionY -= this.getGravity();
				//Server.getInstance().getLogger().warning(this.getId() + ": 不在地面, 掉落 motionY=" + this.motionY);
				hasUpdate = true;
			} else {
				this.isKnockback = false;
			}
			this.movingEntityPart4Timing.stopTiming();
		}


		return hasUpdate;
	}

	public double getRange() {
		return 100.0;
	}

	public void setTarget(Vector3 vec, String identifier) {
		this.setTarget(vec, identifier, false);
	}

	public void setTarget(Vector3 vec, String identifier, boolean immediate) {
		if (identifier == null) return;

		if (vec == null || immediate || !this.hasSetTarget() || identifier.equals(this.targetSetter)) {
			this.target = vec;
			this.targetSetter = identifier;
		}

		// 如果设置了新的目标，则按需重新开始寻路ding
		// 这边可以直接把某个实体设为Target，会被无缝传入到寻路中，自动更新寻路目标坐标
		if (vec != null) {
			this.router.setDestination(vec instanceof Position ? (Position) vec : Position.fromObject(vec, this.level), immediate || !this.router.hasRoute());
		} else {
			this.router.setDestination(null, true);
		}
	}

    public Vector3 getRealTarget() {
        return this.target;
    }

	public Vector3 getTarget() {
		if (this.target == null) return null;
		return new Vector3(this.target.x, this.target.y, this.target.z);
	}


	/**
	 * Returns whether the entity has set its target
	 * The entity may not follow the target if there is following target and set target is different
	 * If following distance of target is too far to follow or cannot reach, set target will be the next following target
	 */
	public boolean hasSetTarget(){
		return this.target != null && this.distance(this.target) < this.getRange();
	}

	@Override
	protected void checkGroundState(double movX, double movY, double movZ, double dx, double dy, double dz) {
		this.isCollidedVertically = movY != dy;
		this.isCollidedHorizontally = (movX != dx || movZ != dz);
		this.isCollided = (this.isCollidedHorizontally || this.isCollidedVertically);

		// this.onGround = (movY != dy && movY < 0);
		// onGround 는 onUpdate 에서 확인
	}

	private void checkGround(){
		AxisAlignedBB[] list = this.level.getCollisionCubes(this, this.level.getTickRate() > 1 ? this.boundingBox.getOffsetBoundingBox(0, -1, 0) : this.boundingBox.addCoord(0, -1, 0), false);

		double maxY = 0;
		for(AxisAlignedBB bb : list){
			if(bb.getMaxY() > maxY){
				maxY = bb.getMaxY();
			}
		}

		this.onGround = (maxY == this.boundingBox.getMinY());
	}

	@Override
	public void setHealth(float health) {
		super.setHealth(health);
		UpdateAttributesPacket pk0 = new UpdateAttributesPacket();
		pk0.entityId = this.getId();
		pk0.entries = new Attribute[]{
				Attribute.getAttribute(Attribute.HEALTH).setMaxValue(this.getMaxHealth()).setValue(this.getHealth()),
		};
		this.getLevel().addChunkPacket(this.chunk.getX(), this.chunk.getZ(), pk0);
	}

	@Override
	public void setMaxHealth(int maxHealth) {
		super.setMaxHealth(maxHealth);
		if (this.getHealth() > maxHealth) this.health = maxHealth;
		UpdateAttributesPacket pk0 = new UpdateAttributesPacket();
		pk0.entityId = this.getId();
		pk0.entries = new Attribute[]{
				Attribute.getAttribute(Attribute.HEALTH).setMaxValue(this.getMaxHealth()).setValue(this.getHealth()),
		};
		this.getLevel().addChunkPacket(this.chunk.getX(), this.chunk.getZ(), pk0);
	}

	@Override
	public void spawnTo(Player player) {
		super.spawnTo(player);
		this.equipmentInventory.sendContents(player);
		this.armorInventory.sendContents(player);
	}

	@Override
	public void saveNBT() {
		super.saveNBT();
		this.namedTag.put(TAG_MAINHAND, NBTIO.putItemHelper(this.equipmentInventory.getItemInHand()));
		this.namedTag.put(TAG_OFFHAND, NBTIO.putItemHelper(this.equipmentInventory.getItemInOffhand()));

		if (this.armorInventory != null) {
			ListTag<CompoundTag> armorTag = new ListTag<>(TAG_ARMOR);
			for (int i = 0; i < 4; i++) {
				armorTag.add(NBTIO.putItemHelper(this.armorInventory.getItem(i), i));
			}
			this.namedTag.putList(armorTag);
		}
	}

	@Override
	public boolean attack(EntityDamageEvent source) {
		if (this.isClosed() || !this.isAlive()) {
			return false;
		}

		if (source.getCause() != EntityDamageEvent.DamageCause.VOID && source.getCause() != EntityDamageEvent.DamageCause.CUSTOM && source.getCause() != EntityDamageEvent.DamageCause.MAGIC && source.getCause() != EntityDamageEvent.DamageCause.HUNGER) {
			int armorPoints = 0;
			int epf = 0;
//            int toughness = 0;

			EntityArmorInventory armorInventory = this.getArmorInventory();
			for (Item armor : armorInventory.getContents().values()) {
				armorPoints += armor.getArmorPoints();
				epf += calculateEnchantmentProtectionFactor(armor, source);
				//toughness += armor.getToughness();
			}

			if (source.canBeReducedByArmor()) {
				source.setDamage(-source.getFinalDamage() * armorPoints * 0.04f, EntityDamageEvent.DamageModifier.ARMOR);
			}

			source.setDamage(-source.getFinalDamage() * Math.min(NukkitMath.ceilFloat(Math.min(epf, 25) * ((float) ThreadLocalRandom.current().nextInt(50, 100) / 100)), 20) * 0.04f,
					EntityDamageEvent.DamageModifier.ARMOR_ENCHANTMENTS);

			source.setDamage(-Math.min(this.getAbsorption(), source.getFinalDamage()), EntityDamageEvent.DamageModifier.ABSORPTION);
		}

		if (super.attack(source)) {
			Entity damager = null;

			if (source instanceof EntityDamageByEntityEvent) {
				damager = ((EntityDamageByEntityEvent) source).getDamager();
			}

			for (int slot = 0; slot < 4; slot++) {
				Item armor = damageArmor(armorInventory.getItem(slot), damager);
				armorInventory.setItem(slot, armor, armor.getId() != BlockID.AIR);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setOnFire(int seconds) {
		int level = 0;

		for (Item armor : this.getArmorInventory().getContents().values()) {
			Enchantment fireProtection = armor.getEnchantment(Enchantment.ID_FIRE_PROTECTION);

			if (fireProtection != null && fireProtection.getLevel() > 0) {
				level = Math.max(level, fireProtection.getLevel());
			}
		}

		seconds = (int) (seconds * (1 - level * 0.15));

		super.setOnFire(seconds);
	}

	protected double calculateEnchantmentProtectionFactor(Item item, EntityDamageEvent source) {
		if (!item.hasEnchantments()) {
			return 0;
		}

		double epf = 0;

		for (Enchantment ench : item.getEnchantments()) {
			epf += ench.getProtectionFactor(source);
		}

		return epf;
	}

	protected Item damageArmor(Item armor, Entity damager) {
		if (armor.hasEnchantments()) {
			if (damager != null) {
				for (Enchantment enchantment : armor.getEnchantments()) {
					enchantment.doPostAttack(damager, this);
				}
			}

			Enchantment durability = armor.getEnchantment(Enchantment.ID_UNBREAKING);
			if (durability != null
					&& durability.getLevel() > 0
					&& (100 / (durability.getLevel() + 1)) <= ThreadLocalRandom.current().nextInt(100)) {
				return armor;
			}
		}

		if (armor.isUnbreakable() || armor.getMaxDurability() < 0) {
			return armor;
		}

		armor.setDamage(armor.getDamage() + 1);

		if (armor.getDamage() >= armor.getMaxDurability()) {
			getLevel().addSound(this, SoundEnum.RANDOM_BREAK);
			return Item.get(BlockID.AIR, 0, 0);
		}

		return armor;
	}

	public EntityArmorInventory getArmorInventory() {
		return armorInventory;
	}

	public EntityEquipmentInventory getEquipmentInventory() {
		return equipmentInventory;
	}

	public EntityEquipmentInventory getInventory() {
		return equipmentInventory;
	}

	@Override
	public void knockBack(Entity attacker, double damage, double x, double z, double base){
		this.isKnockback = true;

		super.knockBack(attacker, damage, x, z, base / 2);
	}

    public Router getRouter() {
        return router;
    }

    public void setTargetFinder(TargetFinder targetFinder) {
        this.targetFinder = targetFinder;
    }

	@Override
	public TargetFinder getTargetFinder() {
		return targetFinder;
	}

	public void updateBotTask(MovingEntityTask task) {
		if (this.task != null) this.task.forceStop();
		this.task = task;
		if (task != null) this.task.onUpdate(Server.getInstance().getTick());
	}

	public MovingEntityTask getTask() {
		return task;
	}

	public boolean isLookAtFront() {
		return lookAtFront;
	}

	public void setLookAtFront(boolean lookAtFront) {
		this.lookAtFront = lookAtFront;
	}

	public boolean isAutoCollide() {
		return autoCollide;
	}

	public void setAutoCollide(boolean autoCollide) {
		this.autoCollide = autoCollide;
	}
}

