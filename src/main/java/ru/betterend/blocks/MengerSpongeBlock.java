package ru.betterend.blocks;

import java.util.Queue;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import ru.betterend.blocks.basis.BlockBaseNotFull;
import ru.betterend.client.render.ERenderLayer;
import ru.betterend.interfaces.IRenderTypeable;
import ru.betterend.registry.EndBlocks;

public class MengerSpongeBlock extends BlockBaseNotFull implements IRenderTypeable {
	public MengerSpongeBlock() {
		super(FabricBlockSettings.copyOf(Blocks.SPONGE).noOcclusion());
	}
	
	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		if (absorbWater(world, pos)) {
			world.setBlockAndUpdate(pos, EndBlocks.MENGER_SPONGE_WET.defaultBlockState());
		}
	}
	
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (absorbWater(world, pos)) {
			return EndBlocks.MENGER_SPONGE_WET.defaultBlockState();
		}
		return state;
	}

	private boolean absorbWater(LevelAccessor world, BlockPos pos) {
		Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
		queue.add(new Tuple<BlockPos, Integer>(pos, 0));
		int i = 0;

		while (!queue.isEmpty()) {
			Tuple<BlockPos, Integer> pair = queue.poll();
			BlockPos blockPos = (BlockPos) pair.getA();
			int j = (Integer) pair.getB();
			Direction[] var8 = Direction.values();
			int var9 = var8.length;

			for (int var10 = 0; var10 < var9; ++var10) {
				Direction direction = var8[var10];
				BlockPos blockPos2 = blockPos.relative(direction);
				BlockState blockState = world.getBlockState(blockPos2);
				FluidState fluidState = world.getFluidState(blockPos2);
				Material material = blockState.getMaterial();
				if (fluidState.is(FluidTags.WATER)) {
					if (blockState.getBlock() instanceof BucketPickup && ((BucketPickup) blockState.getBlock()).takeLiquid(world, blockPos2, blockState) != Fluids.EMPTY) {
						++i;
						if (j < 6) {
							queue.add(new Tuple<BlockPos, Integer>(blockPos2, j + 1));
						}
					}
					else if (blockState.getBlock() instanceof LiquidBlock) {
						world.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
						++i;
						if (j < 6) {
							queue.add(new Tuple<BlockPos, Integer>(blockPos2, j + 1));
						}
					}
					else if (material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
						BlockEntity blockEntity = blockState.getBlock().isEntityBlock() ? world.getBlockEntity(blockPos2) : null;
						dropResources(blockState, world, blockPos2, blockEntity);
						world.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
						++i;
						if (j < 6) {
							queue.add(new Tuple<BlockPos, Integer>(blockPos2, j + 1));
						}
					}
				}
			}

			if (i > 64) {
				break;
			}
		}

		return i > 0;
	}
	
	@Override
	public ERenderLayer getRenderLayer() {
		return ERenderLayer.CUTOUT;
	}
}
