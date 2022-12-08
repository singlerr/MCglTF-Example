package com.modularmods.mcgltf.example;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularmods.mcgltf.animation.GltfAnimationCreator;
import com.modularmods.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class ExampleBlockEntityRenderer implements IGltfModelReceiver, BlockEntityRenderer<ExampleBlockEntity> {

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

	/**
	 * Since you use custom BEWLR(DynamicItemRenderer) for BlockItem instead of BER to render item form of block,
	 * the last parameters p_112312_ which control overlay color is almost never used.
	 */
	@Override
	public void render(ExampleBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
		int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
		
		boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
		
		boolean currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		matrices.pushPose();
		Level level = blockEntity.getLevel();
		if(level != null) {
			float time = (level.getGameTime() + tickDelta) / 20;
			//Play every animation clips simultaneously
			for(List<InterpolatedChannel> animation : animations) {
				animation.parallelStream().forEach((channel) -> {
					float[] keys = channel.getKeys();
					channel.update(time % keys[keys.length - 1]);
				});
			}
			
			matrices.translate(0.5, 0.5, 0.5); //Make sure it is in the center of the block
			switch(level.getBlockState(blockEntity.getBlockPos()).getOptionalValue(HorizontalDirectionalBlock.FACING).orElse(Direction.NORTH)) {
			case DOWN:
				break;
			case UP:
				break;
			case NORTH:
				break;
			case SOUTH:
				matrices.mulPose(new Quaternionf(0.0F, 1.0F, 0.0F, 0.0F));
				break;
			case WEST:
				matrices.mulPose(new Quaternionf(0.0F, 0.7071068F, 0.0F, 0.7071068F));
				break;
			case EAST:
				matrices.mulPose(new Quaternionf(0.0F, -0.7071068F, 0.0F, 0.7071068F));
				break;
			}
		}
		
		RenderedGltfModel.CURRENT_POSE = matrices.last().pose();
		RenderedGltfModel.CURRENT_NORMAL = matrices.last().normal();
		matrices.popPose();
		
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, light & '\uffff', light >> 16 & '\uffff');
		
		if(MCglTF.getInstance().isShaderModActive()) {
			renderedScene.renderForShaderMod();
		}
		else {
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getLightTexture().getId());
			
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
			
			renderedScene.renderForVanilla();
			
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2);
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
		}
		
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
		
		if(!currentDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
		if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
		
		if(currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
		else GL11.glDisable(GL11.GL_CULL_FACE);
		
		GL30.glBindVertexArray(currentVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
	}

}
