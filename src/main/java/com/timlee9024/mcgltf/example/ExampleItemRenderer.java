package com.timlee9024.mcgltf.example;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.MCglTF;
import com.timlee9024.mcgltf.RenderedGltfModel;
import com.timlee9024.mcgltf.RenderedGltfScene;
import com.timlee9024.mcgltf.animation.GltfAnimationCreator;
import com.timlee9024.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

public abstract class ExampleItemRenderer implements IGltfModelReceiver, BuiltinItemRendererRegistry.DynamicItemRenderer {

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

	@Override
	public void render(ItemStack stack, ItemTransforms.TransformType mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		float time = (mc.level.getGameTime() + ExampleClient.INSTANCE.tickDelta) / 20;
		//Play every animation clips simultaneously
		for(List<InterpolatedChannel> animation : animations) {
			animation.parallelStream().forEach((channel) -> {
				float[] keys = channel.getKeys();
				channel.update(time % keys[keys.length - 1]);
			});
		}
		
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		
		RenderSystem.multMatrix(matrices.last().pose());
		
		switch(mode) {
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:
		case HEAD:
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
			GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
			
			if(MCglTF.getInstance().isShaderModActive()) {
				renderedScene.renderForShaderMod();
			}
			else {
				GL13.glActiveTexture(GL13.GL_TEXTURE2);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE1);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				renderedScene.renderForVanilla();
			}
			break;
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
		case GROUND:
		case FIXED:
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
			
			if(MCglTF.getInstance().isShaderModActive()) {
				renderedScene.renderForShaderMod();
			}
			else {
				GL13.glActiveTexture(GL13.GL_TEXTURE2);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				renderedScene.renderForVanilla();
			}
			break;
		case GUI:
			renderedScene.renderForVanilla();
			break;
		default:
			break;
		}
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		RenderedGltfModel.nodeGlobalTransformLookup.clear();
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

}
