package com.timlee9024.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.MCglTF;
import com.timlee9024.mcgltf.RenderedGltfModel;

import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ExampleEntityRenderer extends EntityRenderer<ExampleEntity> implements IGltfModelReceiver {

	protected List<Runnable> vanillaCommands;
	
	protected List<Runnable> shaderModCommands;
	
	protected List<Animation> animations;
	
	protected ExampleEntityRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/entity/cesium_man.gltf");
	}

	@Override
	public void onModelLoaded(RenderedGltfModel renderedModel) {
		vanillaCommands = renderedModel.vanillaSceneCommands.get(0);
		shaderModCommands = renderedModel.shaderModSceneCommands.get(0);
		animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
	}

	@Override
	public ResourceLocation getTextureLocation(ExampleEntity p_110775_1_) {
		return null;
	}

	@Override
	public void render(ExampleEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
		if(vanillaCommands != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			RenderSystem.multMatrix(p_225623_4_.last().pose());
			GL11.glRotatef(MathHelper.rotLerp(p_225623_3_, p_225623_1_.yBodyRotO, p_225623_1_.yBodyRot), 0.0F, 1.0F, 0.0F);
			
			Minecraft mc = Minecraft.getInstance();
			for(Animation animation : animations) {
				animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(mc.level, p_225623_3_) % animation.getEndTimeS());
			}
			
			GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(p_225623_6_ & '\uffff'), (short)(p_225623_6_ >> 16 & '\uffff'));
			
			MCglTF.CURRENT_PROGRAM = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			if(MCglTF.CURRENT_PROGRAM == 0) {
				GL13.glActiveTexture(GL13.GL_TEXTURE2);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				vanillaCommands.forEach((command) -> command.run());
			}
			else {
				shaderModCommands.forEach((command) -> command.run());
			}
			
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
		super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
	}

}
