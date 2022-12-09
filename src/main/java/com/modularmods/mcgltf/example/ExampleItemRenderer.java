package com.modularmods.mcgltf.example;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularmods.mcgltf.animation.GltfAnimationCreator;
import com.modularmods.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;

public abstract class ExampleItemRenderer implements IGltfModelReceiver {

	protected RenderedGltfScene renderedScene;
	
	protected List<List<InterpolatedChannel>> animations;
	
	@Override
	public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
		renderedScene = renderedModel.renderedGltfScenes.get(0);
		List<AnimationModel> animationModels = renderedModel.gltfModel.getAnimationModels();
		animations = new ArrayList<List<InterpolatedChannel>>(animationModels.size());
		for(AnimationModel animationModel : animationModels) {
			animations.add(GltfAnimationCreator.createGltfAnimation(animationModel));
		}
	}
	
	public void renderByItem(ItemTransforms.TransformType p_108831_, PoseStack p_108832_, MultiBufferSource p_108833_, int p_108834_, int p_108835_) {
		Minecraft mc = Minecraft.getInstance();
		float time = (mc.level.getGameTime() + mc.getPartialTick()) / 20;
		//Play every animation clips simultaneously
		for(List<InterpolatedChannel> animation : animations) {
			animation.parallelStream().forEach((channel) -> {
				float[] keys = channel.getKeys();
				channel.update(time % keys[keys.length - 1]);
			});
		}
		
		int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
		int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
		
		boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
		
		boolean currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		switch(p_108831_) {
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case HEAD:
			boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_BLEND);
			GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			RenderedGltfModel.CURRENT_POSE = p_108832_.last().pose();
			RenderedGltfModel.CURRENT_NORMAL = p_108832_.last().normal();
			
			GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, p_108835_ & '\uffff', p_108835_ >> 16 & '\uffff');
			GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, p_108834_ & '\uffff', p_108834_ >> 16 & '\uffff');
			
			if(MCglTF.getInstance().isShaderModActive()) {
				renderedScene.renderForShaderMod();
			}
			else {
				GL13.glActiveTexture(GL13.GL_TEXTURE2);
				int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getLightTexture().getId());
				
				GL13.glActiveTexture(GL13.GL_TEXTURE1);
				int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
				mc.gameRenderer.overlayTexture().setupOverlayColor();
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(1));
				mc.gameRenderer.overlayTexture().teardownOverlayColor();
				
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
			
			GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
			GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
			
			if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
			break;
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
		case GROUND:
		case FIXED:
			currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_BLEND);
			GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			RenderedGltfModel.CURRENT_POSE = p_108832_.last().pose();
			RenderedGltfModel.CURRENT_NORMAL = p_108832_.last().normal();
			
			GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, p_108834_ & '\uffff', p_108834_ >> 16 & '\uffff');
			
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
			
			if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
			break;
		case GUI:
			Quaternionf rotateAround = new Quaternionf(0.0F, 1.0F, 0.0F, 0.0F);
			RenderedGltfModel.CURRENT_POSE = new Matrix4f(RenderSystem.getModelViewMatrix());
			RenderedGltfModel.CURRENT_POSE.rotate(rotateAround);
			RenderedGltfModel.CURRENT_NORMAL = new Matrix3f();
			RenderedGltfModel.CURRENT_NORMAL.rotate(rotateAround);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getDefaultColorMap());
			
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
			break;
		default:
			break;
		}
		
		if(!currentDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		if(currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
		else GL11.glDisable(GL11.GL_CULL_FACE);
		
		GL30.glBindVertexArray(currentVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
	}

}
