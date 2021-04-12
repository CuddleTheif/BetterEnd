package ru.betterend.entity;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import ru.betterend.registry.EndBiomes;
import ru.betterend.registry.EndItems;

public class CubozoaEntity extends AbstractSchoolingFish {
	public static final int VARIANTS = 2;
	private static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(CubozoaEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> SCALE = SynchedEntityData.defineId(CubozoaEntity.class, EntityDataSerializers.BYTE);

	public CubozoaEntity(EntityType<CubozoaEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, SpawnGroupData entityData, CompoundTag entityTag) {
		SpawnGroupData data = super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityTag);
		if (EndBiomes.getFromBiome(world.getBiome(blockPosition())) == EndBiomes.SULPHUR_SPRINGS) {
			this.entityData.set(VARIANT, (byte) 1);
		}
		this.refreshDimensions();
		return data;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(VARIANT, (byte) 0);
		this.entityData.define(SCALE, (byte) this.getRandom().nextInt(16));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putByte("Variant", (byte) getVariant());
		tag.putByte("Scale", (byte) getScale());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("Variant")) {
			this.entityData.set(VARIANT, tag.getByte("Variant"));
		}
		if (tag.contains("Scale")) {
			this.entityData.set(SCALE, tag.getByte("Scale"));
		}
	}

	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes()
				.add(Attributes.MAX_HEALTH, 2.0)
				.add(Attributes.FOLLOW_RANGE, 16.0)
				.add(Attributes.MOVEMENT_SPEED, 0.5);
	}

	public int getVariant() {
		return (int) this.entityData.get(VARIANT);
	}

	public float getScale() {
		return this.entityData.get(SCALE) / 32F + 0.75F;
	}

	public static boolean canSpawn(EntityType<CubozoaEntity> type, ServerLevelAccessor world, MobSpawnType spawnReason, BlockPos pos, Random random) {
		AABB box = new AABB(pos).inflate(16);
		List<CubozoaEntity> list = world.getEntitiesOfClass(CubozoaEntity.class, box, (entity) -> {
			return true;
		});
		return list.size() < 9;
	}

	protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
		return dimensions.height * 0.5F;
	}
	
	@Override
	protected void dropFromLootTable(DamageSource source, boolean causedByPlayer) {
		int count = random.nextInt(3);
		if (count > 0) {
			ItemEntity drop = new ItemEntity(level, getX(), getY(), getZ(), new ItemStack(EndItems.GELATINE, count));
			this.level.addFreshEntity(drop);
		}
	}

	@Override
	protected ItemStack getBucketItemStack() {
		return new ItemStack(Items.WATER_BUCKET);
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.SALMON_FLOP;
	}
	
	@Override
	public void playerTouch(Player player) {
		if (player instanceof ServerPlayer && player.hurt(DamageSource.mobAttack(this), 0.5F)) {
			if (!this.isSilent()) {
				((ServerPlayer) player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
			}
			if (random.nextBoolean()) {
				player.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0));
			}
		}
	}

	static class CubozoaMoveControl extends MoveControl {
		CubozoaMoveControl(CubozoaEntity owner) {
			super(owner);
		}

		public void tick() {
			if (this.mob.isEyeInFluid(FluidTags.WATER)) {
				this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
			}

			if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
				float f = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
				this.mob.setSpeed(Mth.lerp(0.125F, this.mob.getSpeed(), f));
				double d = this.wantedX - this.mob.getX();
				double e = this.wantedY - this.mob.getY();
				double g = this.wantedZ - this.mob.getZ();
				if (e != 0.0D) {
					double h = (double) Mth.sqrt(d * d + e * e + g * g);
					this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0D, (double) this.mob.getSpeed() * (e / h) * 0.1D, 0.0D));
				}

				if (d != 0.0D || g != 0.0D) {
					float i = (float) (Mth.atan2(g, d) * 57.2957763671875D) - 90.0F;
					this.mob.yRot = this.rotlerp(this.mob.yRot, i, 90.0F);
					this.mob.yBodyRot = this.mob.yRot;
				}

			}
			else {
				this.mob.setSpeed(0.0F);
			}
		}
	}
}
