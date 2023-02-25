package com.modularmods.mcgltf.example;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularmods.mcgltf.animation.GltfAnimationCreator;
import com.modularmods.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.animation.Animation;

public class RenderEntityExample extends Render<EntityExample> implements IGltfModelReceiver {

	protected RenderedGltfScene renderedScene;
	
	protected List<List<InterpolatedChannel>> animations;
	
	public RenderEntityExample(RenderManager renderManager) {
		super(renderManager);
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
	protected ResourceLocation getEntityTexture(EntityExample entity) {
		return entity.getProfessionForge().getSkin();
	}

	@Override
	public boolean shouldRender(EntityExample livingEntity, ICamera camera, double camX, double camY, double camZ) {
		if (super.shouldRender(livingEntity, camera, camX, camY, camZ))
		{
			return true;
		}
		else if (livingEntity.getLeashed() && livingEntity.getLeashHolder() != null)
		{
			Entity entity = livingEntity.getLeashHolder();
			return camera.isBoundingBoxInFrustum(entity.getRenderBoundingBox());
		}
		else
		{
			return false;
		}
	}

	@Override
	public void doRender(EntityExample entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTranslated(x, y, z);
		GL11.glRotatef(interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), 0.0F, 1.0F, 0.0F);
		
		float time = Animation.getWorldTime(entity.world, partialTicks);
		//Play every animation clips simultaneously
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
		GL11.glPopMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks)
	{
		float f;
		
		for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F);
		
		while (f >= 180.0F) f -= 360.0F;
		
		return prevYawOffset + partialTicks * f;
	}

}
