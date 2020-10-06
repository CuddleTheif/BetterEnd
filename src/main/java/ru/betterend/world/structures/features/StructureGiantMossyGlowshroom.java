package ru.betterend.world.structures.features;

import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.betterend.blocks.BlockMossyGlowshroomCap;
import ru.betterend.noise.OpenSimplexNoise;
import ru.betterend.registry.BlockRegistry;
import ru.betterend.util.MHelper;
import ru.betterend.util.SplineHelper;
import ru.betterend.util.sdf.SDF;
import ru.betterend.util.sdf.operator.SDFBinary;
import ru.betterend.util.sdf.operator.SDFCoordModify;
import ru.betterend.util.sdf.operator.SDFFlatWave;
import ru.betterend.util.sdf.operator.SDFRound;
import ru.betterend.util.sdf.operator.SDFScale;
import ru.betterend.util.sdf.operator.SDFScale3D;
import ru.betterend.util.sdf.operator.SDFSmoothUnion;
import ru.betterend.util.sdf.operator.SDFSubtraction;
import ru.betterend.util.sdf.operator.SDFTranslate;
import ru.betterend.util.sdf.operator.SDFUnion;
import ru.betterend.util.sdf.primitive.SDFCapedCone;
import ru.betterend.util.sdf.primitive.SDFPrimitive;
import ru.betterend.util.sdf.primitive.SDFSphere;

public class StructureGiantMossyGlowshroom extends SDFStructureFeature {
	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	
	@Override
	protected SDF getSDF(BlockPos center, Random random) {
		System.out.println(center);
		
		SDFCapedCone cone1 = new SDFCapedCone().setHeight(2.5F).setRadius1(1.5F).setRadius2(2.5F);
		SDFCapedCone cone2 = new SDFCapedCone().setHeight(3F).setRadius1(2.5F).setRadius2(13F);
		SDF posedCone2 = new SDFTranslate().setTranslate(0, 5, 0).setSource(cone2);
		SDF posedCone3 = new SDFTranslate().setTranslate(0, 12F, 0).setSource(new SDFScale().setScale(2).setSource(cone2));
		SDF upCone = new SDFSubtraction().setSourceA(posedCone2).setSourceB(posedCone3);
		SDF wave = new SDFFlatWave().setRaysCount(12).setIntensity(1.3F).setSource(upCone);
		SDF cones = new SDFSmoothUnion().setRadius(3).setSourceA(cone1).setSourceB(wave);
		
		SDF innerCone = new SDFTranslate().setTranslate(0, 1.25F, 0).setSource(upCone);
		innerCone = new SDFScale3D().setScale(1.2F, 1F, 1.2F).setSource(innerCone);
		cones = new SDFUnion().setSourceA(cones).setSourceB(innerCone);
		
		SDF glowCone = new SDFCapedCone().setHeight(3F).setRadius1(2F).setRadius2(12.5F);
		SDFPrimitive priGlowCone = (SDFPrimitive) glowCone;
		glowCone = new SDFTranslate().setTranslate(0, 4.25F, 0).setSource(glowCone);
		glowCone = new SDFSubtraction().setSourceA(glowCone).setSourceB(posedCone3);
		
		cones = new SDFUnion().setSourceA(cones).setSourceB(glowCone);
		
		OpenSimplexNoise noise = new OpenSimplexNoise(1234);
		cones = new SDFCoordModify().setFunction((pos) -> {
			float dist = MHelper.length(pos.getX(), pos.getZ());
			float y = pos.getY() + (float) noise.eval(pos.getX() * 0.1 + center.getX(), pos.getZ() * 0.1 + center.getZ()) * dist * 0.3F - dist * 0.15F;
			pos.set(pos.getX(), y, pos.getZ());
		}).setSource(cones);
		
		SDFTranslate HEAD_POS = (SDFTranslate) new SDFTranslate().setSource(new SDFTranslate().setTranslate(0, 2.5F, 0).setSource(cones));
		
		SDF roots = new SDFSphere().setRadius(4F);
		SDFPrimitive primRoots = (SDFPrimitive) roots;
		roots = new SDFScale3D().setScale(1, 0.7F, 1).setSource(roots);
		SDFFlatWave rotRoots = (SDFFlatWave) new SDFFlatWave().setRaysCount(5).setIntensity(1.5F).setSource(roots);
		
		SDFBinary function = new SDFSmoothUnion().setRadius(4).setSourceB(new SDFUnion().setSourceA(HEAD_POS).setSourceB(rotRoots));
		
		cone1.setBlock(BlockRegistry.MOSSY_GLOWSHROOM_CAP);
		cone2.setBlock(BlockRegistry.MOSSY_GLOWSHROOM_CAP);
		priGlowCone.setBlock(BlockRegistry.MOSSY_GLOWSHROOM_HYMENOPHORE);
		primRoots.setBlock(BlockRegistry.MOSSY_GLOWSHROOM.bark);
		
		float height = MHelper.randRange(10F, 25F, random);
		int count = MHelper.floor(height / 4);
		List<Vector3f> spline = SplineHelper.makeSpline(0, 0, 0, 0, height, 0, count);
		SplineHelper.offsetParts(spline, random, 1F, 0, 1F);
		SDF sdf = SplineHelper.buildSDF(spline, 2.1F, 1.5F, (pos) -> {
			return BlockRegistry.MOSSY_GLOWSHROOM.log.getDefaultState();
		});
		Vector3f pos = spline.get(spline.size() - 1);
		float scale = MHelper.randRange(2F, 3.5F, random);
		
		HEAD_POS.setTranslate(pos.getX(), pos.getY(), pos.getZ());
		rotRoots.setAngle(random.nextFloat() * MHelper.PI2);
		function.setSourceA(sdf);
		
		return new SDFRound().setRadius(1.5F).setSource(new SDFScale()
				.setScale(scale)
				.setSource(function)
				.setPostProcess((info) -> {
					if (BlockRegistry.MOSSY_GLOWSHROOM.isTreeLog(info.getState())) {
						if (!BlockRegistry.MOSSY_GLOWSHROOM.isTreeLog(info.getStateUp()) || !BlockRegistry.MOSSY_GLOWSHROOM.isTreeLog(info.getStateDown())) {
							return BlockRegistry.MOSSY_GLOWSHROOM.bark.getDefaultState();
						}
					}
					else if (info.getState().getBlock() == BlockRegistry.MOSSY_GLOWSHROOM_CAP) {
						if (BlockRegistry.MOSSY_GLOWSHROOM.isTreeLog(info.getStateDown())) {
							return info.getState().with(BlockMossyGlowshroomCap.TRANSITION, true);
						}
						
						int air = 0;
						for (Direction dir: Direction.values()) {
							if (info.getState(dir).isAir()) {
								air ++;
							}
						}
						if (air > 4) {
							info.setState(AIR);
							return AIR;
						}
					}
					return info.getState();
				}));
	}
}
