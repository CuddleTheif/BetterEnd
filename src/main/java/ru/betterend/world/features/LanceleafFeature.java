package ru.betterend.world.features;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import ru.betterend.blocks.basis.EndPlantWithAgeBlock;
import ru.betterend.registry.EndBlocks;

public class LanceleafFeature extends ScatterFeature {
	public LanceleafFeature() {
		super(7);
	}

	@Override
	public boolean canGenerate(WorldGenLevel world, Random random, BlockPos center, BlockPos blockPos, float radius) {
		return EndBlocks.LANCELEAF_SEED.canSurvive(AIR, world, blockPos);
	}

	@Override
	public void generate(WorldGenLevel world, Random random, BlockPos blockPos) {
		EndPlantWithAgeBlock seed = ((EndPlantWithAgeBlock) EndBlocks.LANCELEAF_SEED);
		seed.growAdult(world, random, blockPos);
	}
	
	@Override
	protected int getChance() {
		return 5;
	}
}
