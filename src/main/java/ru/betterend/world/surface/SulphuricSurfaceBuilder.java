package ru.betterend.world.surface;

import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import ru.betterend.noise.OpenSimplexNoise;
import ru.betterend.util.MHelper;

public class SulphuricSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final OpenSimplexNoise NOISE = new OpenSimplexNoise(5123);
	
	public SulphuricSurfaceBuilder() {
		super(SurfaceBuilderBaseConfiguration.CODEC);
	}

	@Override
	public void apply(Random random, ChunkAccess chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, SurfaceBuilderBaseConfiguration surfaceBlocks) {
		double value = NOISE.eval(x * 0.03, z * 0.03) + NOISE.eval(x * 0.1, z * 0.1) * 0.3 + MHelper.randRange(-0.1, 0.1, MHelper.RANDOM);
		if (value < -0.6) {
			SurfaceBuilder.DEFAULT.apply(random, chunk, biome, x, z, height, noise, defaultBlock, defaultFluid, seaLevel, seed, SurfaceBuilders.DEFAULT_END_CONFIG);
		}
		else if (value < -0.3) {
			SurfaceBuilder.DEFAULT.apply(random, chunk, biome, x, z, height, noise, defaultBlock, defaultFluid, seaLevel, seed, SurfaceBuilders.FLAVOLITE_CONFIG);
		}
		else if (value < 0.5) {
			SurfaceBuilder.DEFAULT.apply(random, chunk, biome, x, z, height, noise, defaultBlock, defaultFluid, seaLevel, seed, SurfaceBuilders.SULFURIC_ROCK_CONFIG);
		}
		else {
			SurfaceBuilder.DEFAULT.apply(random, chunk, biome, x, z, height, noise, defaultBlock, defaultFluid, seaLevel, seed, SurfaceBuilders.BRIMSTONE_CONFIG);
		}
	}
	
	public static SulphuricSurfaceBuilder register(String name) {
		return Registry.register(Registry.SURFACE_BUILDER, name, new SulphuricSurfaceBuilder());
	}
}