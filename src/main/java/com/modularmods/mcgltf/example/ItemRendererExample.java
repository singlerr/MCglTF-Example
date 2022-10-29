package com.modularmods.mcgltf.example;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.ItemCameraTransformsHelper;
import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularmods.mcgltf.animation.GltfAnimationCreator;
import com.modularmods.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.model.animation.Animation;

public abstract class ItemRendererExample implements IGltfModelReceiver {

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
	
	public void render() {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		float time = Animation.getWorldTime(Minecraft.getMinecraft().world, Animation.getPartialTickTime());
		for(List<InterpolatedChannel> animation : animations) {
			animation.parallelStream().forEach((channel) -> {
				float[] keys = channel.getKeys();
				channel.update(time % keys[keys.length - 1]);
			});
		}
		
		if(MCglTF.getInstance().isShaderModActive()) {
			renderedScene.renderForShaderMod();
		}
		else {
			renderedScene.renderForVanilla();
		}
		
		GL11.glPopAttrib();
	}
	
	/**
	 * Require {@link ItemCameraTransformsHelper#registerDummyModelToAccessCurrentTransformTypeForTEISR(net.minecraft.item.Item) ItemCameraTransformsHelper#registerDummyModelToAccessCurrentTransformTypeForTEISR(yourItem)} during
	 * {@link net.minecraftforge.client.event.ModelRegistryEvent ModelRegistryEvent} to make {@link ItemCameraTransformsHelper#getCurrentTransformType()} work
	 */
	public void renderWithItemCameraTransformsHelper() {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		float time = Animation.getWorldTime(Minecraft.getMinecraft().world, Animation.getPartialTickTime());
		//Play every animation clips simultaneously
		for(List<InterpolatedChannel> animation : animations) {
			animation.parallelStream().forEach((channel) -> {
				float[] keys = channel.getKeys();
				channel.update(time % keys[keys.length - 1]);
			});
		}
		
		switch(ItemCameraTransformsHelper.getCurrentTransformType()) {
		case GUI:
			GL11.glEnable(GL11.GL_LIGHTING);
			renderedScene.renderForVanilla();
			break;
		default:
			if(MCglTF.getInstance().isShaderModActive()) {
				renderedScene.renderForShaderMod();
			}
			else {
				renderedScene.renderForVanilla();
			}
			break;
		}
		
		GL11.glPopAttrib();
	}

}
