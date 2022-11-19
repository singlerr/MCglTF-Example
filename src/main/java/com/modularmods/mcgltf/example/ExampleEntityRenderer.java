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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ExampleEntityRenderer extends EntityRenderer<ExampleEntity> implements IGltfModelReceiver {

	protected RenderedGltfScene renderedScene;
	
	protected List<List<InterpolatedChannel>> animations;
	
	protected ExampleEntityRenderer(EntityRenderDispatcher p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/entity/cesium_man.gltf");
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
	public ResourceLocation getTextureLocation(ExampleEntity p_110775_1_) {
		return null;
	}

	@Override
	public void render(ExampleEntity entity, float yRotDelta, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		float time = (entity.level.getGameTime() + tickDelta) / 20;
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
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		RenderSystem.multMatrix(matrices.last().pose());
		GL11.glRotatef(Mth.rotLerp(tickDelta, entity.yBodyRotO, entity.yBodyRot), 0.0F, 1.0F, 0.0F);
		
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
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();
		super.render(entity, yRotDelta, tickDelta, matrices, vertexConsumers, light);
	}
	
	protected void checkAndRenderNameTag(ExampleEntity entity, float yRotDelta, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(entity, yRotDelta, tickDelta, matrices, vertexConsumers, light);
	}

}
