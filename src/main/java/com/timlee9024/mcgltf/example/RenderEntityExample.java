package com.timlee9024.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.RenderedGltfModel;

import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderEntityExample extends Render<EntityExample> implements IGltfModelReceiver {

	protected List<Runnable> commands;
	
	protected List<Animation> animations;
	
	public RenderEntityExample(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/entity/cesium_man.gltf");
	}

	@Override
	public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
		commands = renderedModel.sceneCommands.get(0);
		animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
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
		if(commands != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glTranslated(x, y, z);
			GL11.glRotatef(interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), 0.0F, 1.0F, 0.0F);
			for(Animation animation : animations) {
				animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(entity.world, partialTicks) % animation.getEndTimeS());
			}
			commands.forEach((command) -> command.run());
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
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
