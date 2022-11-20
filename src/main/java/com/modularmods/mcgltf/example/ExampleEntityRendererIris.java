package com.modularmods.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.animation.InterpolatedChannel;
import com.modularmods.mcgltf.iris.IrisRenderingHook;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;

public class ExampleEntityRendererIris extends ExampleEntityRenderer {

	protected ExampleEntityRendererIris(EntityRendererProvider.Context p_174008_) {
		super(p_174008_);
	}

	@Override
	public void render(ExampleEntity entity, float yRotDelta, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		RenderType renderType = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
		vertexConsumers.getBuffer(renderType); //Put renderType into MultiBufferSource to ensure command submit to IrisRenderingHook will be run in Iris batched entity rendering.
		Matrix4f modelViewMatrix = matrices.last().pose().copy();
		
		if(MCglTF.getInstance().isShaderModActive()) {
			IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("ENTITIES", renderType, () -> {
				float time = (entity.level.getGameTime() + tickDelta) / 20;
				//Play every animation clips simultaneously
				for(List<InterpolatedChannel> animation : animations) {
					animation.parallelStream().forEach((channel) -> {
						float[] keys = channel.getKeys();
						channel.update(time % keys[keys.length - 1]);
					});
				}
				
				modelViewMatrix.multiply(new Quaternion(0.0F, Mth.rotLerp(tickDelta, entity.yBodyRotO, entity.yBodyRot), 0.0F, true));
				RenderedGltfModel.CURRENT_POSE = modelViewMatrix;
				
				boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_BLEND); //Since the renderType is entity solid, we need to turn on blending manually.
				GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 655360 & '\uffff', 655360 >> 16 & '\uffff');
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
				float time = (entity.level.getGameTime() + tickDelta) / 20;
				for(List<InterpolatedChannel> animation : animations) {
					animation.parallelStream().forEach((channel) -> {
						float[] keys = channel.getKeys();
						channel.update(time % keys[keys.length - 1]);
					});
				}
				
				Quaternion rotation = new Quaternion(0.0F, Mth.rotLerp(tickDelta, entity.yBodyRotO, entity.yBodyRot), 0.0F, true);
				modelViewMatrix.multiply(rotation);
				normalMatrix.mul(rotation);
				RenderedGltfModel.CURRENT_POSE = modelViewMatrix;
				RenderedGltfModel.CURRENT_NORMAL = normalMatrix;
				
				boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_BLEND);
				GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 655360 & '\uffff', 655360 >> 16 & '\uffff');
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, light & '\uffff', light >> 16 & '\uffff');
				
				renderedScene.renderForVanilla();
				
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
				GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
				
				if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
			});
		}
		checkAndRenderNameTag(entity, yRotDelta, tickDelta, matrices, vertexConsumers, light);
	}

}
