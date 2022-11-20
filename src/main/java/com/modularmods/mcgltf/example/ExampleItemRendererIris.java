package com.modularmods.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.animation.InterpolatedChannel;
import com.modularmods.mcgltf.iris.IrisRenderingHook;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

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
		Matrix4f modelViewMatrix = matrices.last().pose().copy();
		
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
				
				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				
				GL11.glShadeModel(GL11.GL_SMOOTH);
				
				RenderSystem.multMatrix(modelViewMatrix);
				
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glEnable(GL11.GL_BLEND); //Since the renderType is entity solid, we need to turn on blending manually.
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
				GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
				
				renderedScene.renderForVanilla();
				
				GL11.glPopAttrib();
				GL11.glPopMatrix();
			});
			break;
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			if(MCglTF.getInstance().isShaderModActive()) {
				IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("HAND_SOLID", renderType, () -> {
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
					
					GL11.glShadeModel(GL11.GL_SMOOTH);
					
					RenderSystem.multMatrix(modelViewMatrix);
					
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
					
					renderedScene.renderForShaderMod();
					
					GL11.glPopAttrib();
					GL11.glPopMatrix();
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
					
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
					
					GL11.glShadeModel(GL11.GL_SMOOTH);
					
					RenderSystem.multMatrix(modelViewMatrix);
					
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
					
					renderedScene.renderForVanilla();
					
					GL11.glPopAttrib();
					GL11.glPopMatrix();
				});
			}
			break;
		default:
			if(MCglTF.getInstance().isShaderModActive()) {
				IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("ENTITIES", renderType, () -> {
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
					
					GL11.glShadeModel(GL11.GL_SMOOTH);
					
					RenderSystem.multMatrix(modelViewMatrix);
					
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
					
					renderedScene.renderForShaderMod();
					
					GL11.glPopAttrib();
					GL11.glPopMatrix();
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
					
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
					
					GL11.glShadeModel(GL11.GL_SMOOTH);
					
					RenderSystem.multMatrix(modelViewMatrix);
					
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
					GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
					
					renderedScene.renderForVanilla();
					
					GL11.glPopAttrib();
					GL11.glPopMatrix();
				});
			}
		}
	}

}
