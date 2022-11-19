package com.modularmods.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.animation.InterpolatedChannel;
import com.modularmods.mcgltf.iris.IrisRenderingHook;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;

public abstract class ExampleItemRendererIris extends ExampleItemRenderer {

	@Override
	public void render(ItemStack stack, TransformType mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		RenderType renderType = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
		vertexConsumers.getBuffer(renderType); //Put renderType into MultiBufferSource to ensure command submit to IrisRenderingHook will be run in Iris batched entity rendering.
		
		Minecraft mc = Minecraft.getInstance();
		float time = (mc.level.getGameTime() + ExampleClient.INSTANCE.tickDelta) / 20;
		
		switch(mode) {
		case GUI:
			IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("NONE", renderType, () -> {
				//Play every animation clips simultaneously
				for(List<InterpolatedChannel> animation : animations) {
					animation.parallelStream().forEach((channel) -> {
						float[] keys = channel.getKeys();
						channel.update(time % keys[keys.length - 1]);
					});
				}
				
				Quaternion rotateAround = new Quaternion(0.0F, 1.0F, 0.0F, 0.0F);
				RenderedGltfModel.CURRENT_POSE = RenderSystem.getModelViewMatrix().copy();
				RenderedGltfModel.CURRENT_POSE.multiply(rotateAround);
				RenderedGltfModel.CURRENT_NORMAL = new Matrix3f();
				RenderedGltfModel.CURRENT_NORMAL.setIdentity();
				RenderedGltfModel.CURRENT_NORMAL.mul(rotateAround);
				
				boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_BLEND); //Since the renderType is entity solid, we need to turn on blending manually.
				GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, light & '\uffff', light >> 16 & '\uffff');
				
				renderedScene.renderForVanilla();
				
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
				
				if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
			});
			break;
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			Matrix4f modelViewMatrix = matrices.last().pose().copy();
			if(MCglTF.getInstance().isShaderModActive()) {
				IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("HAND_SOLID", renderType, () -> {
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					RenderedGltfModel.CURRENT_POSE = modelViewMatrix;
					
					boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_BLEND);
					GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, light & '\uffff', light >> 16 & '\uffff');
					
					renderedScene.renderForShaderMod();
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
					
					if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
				});
			}
			else {
				Matrix3f normalMatrix = matrices.last().normal().copy();
				IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("NONE", renderType, () -> {
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					RenderedGltfModel.CURRENT_POSE = modelViewMatrix;
					RenderedGltfModel.CURRENT_NORMAL = normalMatrix;
					
					boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_BLEND);
					GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, light & '\uffff', light >> 16 & '\uffff');
					
					renderedScene.renderForVanilla();
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
					
					if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
				});
			}
			break;
		default:
			modelViewMatrix = matrices.last().pose().copy();
			if(MCglTF.getInstance().isShaderModActive()) {
				IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("ENTITIES", renderType, () -> {
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					RenderedGltfModel.CURRENT_POSE = modelViewMatrix;
					
					boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_BLEND);
					GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, light & '\uffff', light >> 16 & '\uffff');
					
					renderedScene.renderForShaderMod();
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
					
					if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
				});
			}
			else {
				Matrix3f normalMatrix = matrices.last().normal().copy();
				IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("NONE", renderType, () -> {
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					RenderedGltfModel.CURRENT_POSE = modelViewMatrix;
					RenderedGltfModel.CURRENT_NORMAL = normalMatrix;
					
					boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_BLEND);
					GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, light & '\uffff', light >> 16 & '\uffff');
					
					renderedScene.renderForVanilla();
					
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
					GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
					
					if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
				});
			}
		}
	}

}
