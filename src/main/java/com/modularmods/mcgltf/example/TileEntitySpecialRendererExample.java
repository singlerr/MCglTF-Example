package com.modularmods.mcgltf.example;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularmods.mcgltf.animation.GltfAnimationCreator;
import com.modularmods.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;

public class TileEntitySpecialRendererExample extends TileEntitySpecialRenderer<TileEntityExample> implements IGltfModelReceiver {

	protected RenderedGltfScene renderedScene;
	
	protected List<List<InterpolatedChannel>> animations;
	
	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/block/boom_box.gltf");
	}

	@Override
	public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
		renderedScene = renderedModel.renderedGltfScenes.get(0);
		List<AnimationModel> animationModels = renderedModel.gltfModel.getAnimationModels();
		animations = new ArrayList<List<InterpolatedChannel>>(animationModels.size());
		for(AnimationModel animationModel : animationModels) {
			animations.add(GltfAnimationCreator.createGltfAnimation(animationModel));
		}
	}

	@Override
	public void render(TileEntityExample te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glTranslated(x, y, z);
		World world = te.getWorld();
		if(world != null) {
			GL11.glTranslatef(0.5F, 0.0F, 0.5F); //Make sure it is in the center of the block
			switch(world.getBlockState(te.getPos()).getValue(BlockHorizontal.FACING)) {
			case DOWN:
				break;
			case UP:
				break;
			case NORTH:
				break;
			case SOUTH:
				GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
				break;
			case WEST:
				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
				break;
			case EAST:
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
				break;
			}
		}
		float time = Animation.getWorldTime(world, partialTicks);
		//Play every animation clips simultaneously
		for(List<InterpolatedChannel> animation : animations) {
			animation.parallelStream().forEach((channel) -> {
				float[] keys = channel.getKeys();
				channel.update(time % keys[keys.length - 1]);
			});
		}
		
		if(MCglTF.getInstance().isShaderModActive()) {
			renderedScene.renderForShaderMod();
		}
		else {
			renderedScene.renderForVanilla();
		}
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

}
