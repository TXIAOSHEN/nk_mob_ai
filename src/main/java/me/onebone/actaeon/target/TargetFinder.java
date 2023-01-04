package me.onebone.actaeon.target;

import me.onebone.actaeon.entity.IMovingEntity;

public abstract class TargetFinder {

	protected IMovingEntity entity;
	protected long nextFind = 0;
	protected long interval;

	public TargetFinder(IMovingEntity entity, long interval){
		if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
		this.entity = entity;
		this.interval = interval;
	}

	public IMovingEntity getEntity(){
		return this.entity;
	}

	public void onUpdate() {
		if (System.currentTimeMillis() >= nextFind) {
			this.find();
			this.nextFind = System.currentTimeMillis() + this.interval;
		}
	}

	public void forceFind() {
		this.find();
		this.nextFind = System.currentTimeMillis() + this.interval;
	}

	protected abstract void find();

}
