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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class ExampleBlockEntityRendererIris extends ExampleBlockEntityRenderer {

	@Override
	public void render(ExampleBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		RenderType renderType = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
		vertexConsumers.getBuffer(renderType); //Put renderType into MultiBufferSource to ensure command submit to IrisRenderingHook will be run in Iris batched entity rendering.
		Matrix4f modelViewMatrix = matrices.last().pose().copy();
		Matrix3f normalMatrix = matrices.last().normal().copy();
		
		if(MCglTF.getInstance().isShaderModActive()) {
			IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("BLOCK_ENTITIES", renderType, () -> {
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
					
					modelViewMatrix.multiplyWithTranslation(0.5F, 0.5F, 0.5F); //Make sure it is in the center of the block
					switch(level.getBlockState(blockEntity.getBlockPos()).getOptionalValue(HorizontalDirectionalBlock.FACING).orElse(Direction.NORTH)) {
					case DOWN:
						break;
					case UP:
						break;
					case NORTH:
						break;
					case SOUTH:
						Quaternion rotation = new Quaternion(0.0F, 1.0F, 0.0F, 0.0F);
						modelViewMatrix.multiply(rotation);
						normalMatrix.mul(rotation);
						break;
					case WEST:
						rotation = new Quaternion(0.0F, 0.7071068F, 0.0F, 0.7071068F);
						modelViewMatrix.multiply(rotation);
						normalMatrix.mul(rotation);
						break;
					case EAST:
						rotation = new Quaternion(0.0F, -0.7071068F, 0.0F, 0.7071068F);
						modelViewMatrix.multiply(rotation);
						normalMatrix.mul(rotation);
						break;
					}
				}
				
				RenderedGltfModel.CURRENT_POSE = modelViewMatrix;
				RenderedGltfModel.CURRENT_NORMAL = normalMatrix;
				
				boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_BLEND); //Since the renderType is entity solid, we need to turn on blending manually.
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
				Level level = blockEntity.getLevel();
				if(level != null) {
					float time = (level.getGameTime() + tickDelta) / 20;
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					modelViewMatrix.multiplyWithTranslation(0.5F, 0.5F, 0.5F);
					switch(level.getBlockState(blockEntity.getBlockPos()).getOptionalValue(HorizontalDirectionalBlock.FACING).orElse(Direction.NORTH)) {
					case DOWN:
						break;
					case UP:
						break;
					case NORTH:
						break;
					case SOUTH:
						Quaternion rotation = new Quaternion(0.0F, 1.0F, 0.0F, 0.0F);
						modelViewMatrix.multiply(rotation);
						normalMatrix.mul(rotation);
						break;
					case WEST:
						rotation = new Quaternion(0.0F, 0.7071068F, 0.0F, 0.7071068F);
						modelViewMatrix.multiply(rotation);
						normalMatrix.mul(rotation);
						break;
					case EAST:
						rotation = new Quaternion(0.0F, -0.7071068F, 0.0F, 0.7071068F);
						modelViewMatrix.multiply(rotation);
						normalMatrix.mul(rotation);
						break;
					}
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
