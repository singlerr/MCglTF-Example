package com.timlee9024.mcgltf.example;

import java.util.List;

import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.RenderedGltfModel;

import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.animation.Animation;

public abstract class AbstractItemGltfModelReceiver implements IGltfModelReceiver {

	public List<Runnable> commands;
	
	public List<Animation> animations;
	
	@Override
	public void onModelLoaded(RenderedGltfModel renderedModel) {
		commands = renderedModel.sceneCommands.get(0);
		animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
	}

}
