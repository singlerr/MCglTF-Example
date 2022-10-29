package com.modularmods.mcgltf.example;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularmods.mcgltf.animation.GltfAnimationCreator;
import com.modularmods.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class ExampleBlockEntityRenderer extends BlockEntityRenderer<ExampleBlockEntity> implements IGltfModelReceiver {

	protected RenderedGltfScene renderedScene;
	
	protected List<List<InterpolatedChannel>> animations;
	
	public ExampleBlockEntityRenderer(BlockEntityRenderDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

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

	/**
	 * Since you use custom BEWLR(DynamicItemRenderer) for BlockItem instead of BER to render item form of block,
	 * the last parameters p_225616_6_ which control overlay color is almost never used.
	 */
	@Override
	public void render(ExampleBlockEntity p_225616_1_, float p_225616_2_, PoseStack p_225616_3_, MultiBufferSource p_225616_4_, int p_225616_5_, int p_225616_6_) {
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		RenderSystem.multMatrix(p_225616_3_.last().pose());
		Level level = p_225616_1_.getLevel();
		if(level != null) {
			float time = (level.getGameTime() + p_225616_2_) / 20;
			//Play every animation clips simultaneously
			for(List<InterpolatedChannel> animation : animations) {
				animation.parallelStream().forEach((channel) -> {
					float[] keys = channel.getKeys();
					channel.update(time % keys[keys.length - 1]);
				});
			}
			
			GL11.glTranslatef(0.5F, 0.0F, 0.5F); //Make sure it is in the center of the block
			switch(level.getBlockState(p_225616_1_.getBlockPos()).getOptionalValue(HorizontalDirectionalBlock.FACING).orElse(Direction.NORTH)) {
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
		
		GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(p_225616_5_ & '\uffff'), (short)(p_225616_5_ >> 16 & '\uffff'));
		
		if(MCglTF.getInstance().isShaderModActive()) {
			renderedScene.renderForShaderMod();
		}
		else {
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			renderedScene.renderForVanilla();
		}
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

}
