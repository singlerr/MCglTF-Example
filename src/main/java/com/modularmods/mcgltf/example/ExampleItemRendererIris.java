package com.modularmods.mcgltf.example;

import java.util.List;

import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.animation.InterpolatedChannel;
import com.modularmods.mcgltf.iris.IrisRenderingHook;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;

public abstract class ExampleItemRendererIris extends ExampleItemRenderer {

	@Override
	public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
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

					Quaternionf rotateAround = new Quaternionf(0.0F, 1.0F, 0.0F, 0.0F);
					RenderedGltfModel.setCurrentPose((new Matrix4f(RenderSystem.getModelViewMatrix()).rotate(rotateAround)));
					RenderedGltfModel.setCurrentNormal((new Matrix3f().rotate(rotateAround)));

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
				Matrix4f modelViewMatrix = new Matrix4f(matrices.last().pose());
				Matrix3f normalMatrix = new Matrix3f(matrices.last().normal());
				if(MCglTF.getInstance().isShaderModActive()) {
					IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("HAND_SOLID", renderType, () -> {
						for(List<InterpolatedChannel> animation : animations) {
							animation.parallelStream().forEach((channel) -> {
								float[] keys = channel.getKeys();
								channel.update(time % keys[keys.length - 1]);
							});
						}

						RenderedGltfModel.setCurrentPose(modelViewMatrix);
						RenderedGltfModel.setCurrentNormal(normalMatrix);

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
					IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("NONE", renderType, () -> {
						for(List<InterpolatedChannel> animation : animations) {
							animation.parallelStream().forEach((channel) -> {
								float[] keys = channel.getKeys();
								channel.update(time % keys[keys.length - 1]);
							});
						}

						RenderedGltfModel.setCurrentPose(modelViewMatrix);
						RenderedGltfModel.setCurrentNormal(normalMatrix);

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
				modelViewMatrix = new Matrix4f(matrices.last().pose());
				normalMatrix = new Matrix3f(matrices.last().normal());
				if(MCglTF.getInstance().isShaderModActive()) {
					IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("ENTITIES", renderType, () -> {
						for(List<InterpolatedChannel> animation : animations) {
							animation.parallelStream().forEach((channel) -> {
								float[] keys = channel.getKeys();
								channel.update(time % keys[keys.length - 1]);
							});
						}

						RenderedGltfModel.setCurrentPose(modelViewMatrix);
						RenderedGltfModel.setCurrentNormal(normalMatrix);

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
					IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("NONE", renderType, () -> {
						for(List<InterpolatedChannel> animation : animations) {
							animation.parallelStream().forEach((channel) -> {
								float[] keys = channel.getKeys();
								channel.update(time % keys[keys.length - 1]);
							});
						}

						RenderedGltfModel.setCurrentPose(modelViewMatrix);
						RenderedGltfModel.setCurrentNormal(normalMatrix);

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
