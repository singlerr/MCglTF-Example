package com.timlee9024.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.MCglTF;
import com.timlee9024.mcgltf.RenderedGltfModel;

import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ExampleEntityRenderer extends EntityRenderer<ExampleEntity> implements IGltfModelReceiver {

	protected Runnable vanillaSkinningCommands;
	
	protected List<Runnable> vanillaRenderCommands;
	
	protected List<Runnable> shaderModCommands;
	
	protected List<Animation> animations;
	
	protected ExampleEntityRenderer(Context p_174008_) {
		super(p_174008_);
	}

	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/entity/cesium_man.gltf");
	}

	@Override
	public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
		vanillaSkinningCommands = renderedModel.vanillaSceneSkinningCommands.get(0);
		vanillaRenderCommands = renderedModel.vanillaSceneRenderCommands.get(0);
		shaderModCommands = renderedModel.shaderModSceneCommands.get(0);
		animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
	}

	@Override
	public ResourceLocation getTextureLocation(ExampleEntity p_114482_) {
		return null;
	}

	@Override
	public void render(ExampleEntity p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
		Minecraft mc = Minecraft.getInstance();
		for(Animation animation : animations) {
			animation.update((mc.level.getGameTime() + p_114487_) / 20 % animation.getEndTimeS());
		}
		
		int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
		int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
		int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
		
		boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
		
		boolean currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		p_114488_.pushPose();
		p_114488_.mulPose(new Quaternion(0.0F, Mth.rotLerp(p_114487_, p_114485_.yBodyRotO, p_114485_.yBodyRot), 0.0F, true));
		MCglTF.CURRENT_POSE = p_114488_.last().pose();
		MCglTF.CURRENT_NORMAL = p_114488_.last().normal();
		p_114488_.popPose();
		
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, p_114490_ & '\uffff', p_114490_ >> 16 & '\uffff');
		
		MCglTF.CURRENT_PROGRAM = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		if(MCglTF.CURRENT_PROGRAM == 0) {
			renderWithVanillaCommands();
			GL20.glUseProgram(0);
		}
		else {
			MCglTF.MODEL_VIEW_MATRIX = GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "modelViewMatrix");
			if(MCglTF.MODEL_VIEW_MATRIX == -1) {
				int currentProgram = MCglTF.CURRENT_PROGRAM;
				renderWithVanillaCommands();
				GL20.glUseProgram(currentProgram);
			}
			else {
				MCglTF.MODEL_VIEW_MATRIX_INVERSE = GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "modelViewMatrixInverse");
				MCglTF.NORMAL_MATRIX = GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "normalMatrix");
				
				RenderSystem.getProjectionMatrix().store(MCglTF.BUF_FLOAT_16);
				GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "projectionMatrix"), false, MCglTF.BUF_FLOAT_16);
				Matrix4f projectionMatrixInverse = RenderSystem.getProjectionMatrix().copy();
				projectionMatrixInverse.invert();
				projectionMatrixInverse.store(MCglTF.BUF_FLOAT_16);
				GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "projectionMatrixInverse"), false, MCglTF.BUF_FLOAT_16);
				
				GL13.glActiveTexture(GL13.GL_TEXTURE3);
				int currentTexture3 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE1);
				int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
				
				shaderModCommands.forEach((command) -> command.run());
				
				GL13.glActiveTexture(GL13.GL_TEXTURE3);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture3);
				GL13.glActiveTexture(GL13.GL_TEXTURE1);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
			}
		}
		
		GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
		
		if(!currentDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
		if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
		
		if(currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
		else GL11.glDisable(GL11.GL_CULL_FACE);
		
		GL30.glBindVertexArray(currentVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
		super.render(p_114485_, p_114486_, p_114487_, p_114488_, p_114489_, p_114490_);
	}
	
	private void renderWithVanillaCommands() {
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getLightTexture().getId());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		
		vanillaSkinningCommands.run();
		
		MCglTF.CURRENT_SHADER_INSTANCE = GameRenderer.getRendertypeEntitySolidShader();
		MCglTF.CURRENT_PROGRAM = MCglTF.CURRENT_SHADER_INSTANCE.getId();
		GL20.glUseProgram(MCglTF.CURRENT_PROGRAM);
		
		MCglTF.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
		MCglTF.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.upload();
		
		MCglTF.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
		MCglTF.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.upload();
		
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_START.set(RenderSystem.getShaderFogStart());
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_START.upload();
		
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_END.set(RenderSystem.getShaderFogEnd());
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_END.upload();
		
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_COLOR.upload();
		
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		MCglTF.CURRENT_SHADER_INSTANCE.FOG_SHAPE.upload();
		
		GL20.glUniform1i(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "Sampler0"), 0);
		GL20.glUniform1i(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "Sampler1"), 1);
		GL20.glUniform1i(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "Sampler2"), 2);
		
		RenderSystem.setupShaderLights(MCglTF.CURRENT_SHADER_INSTANCE);
		MCglTF.LIGHT0_DIRECTION = new Vector3f(MCglTF.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(0), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(1), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(2));
		MCglTF.LIGHT1_DIRECTION = new Vector3f(MCglTF.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(0), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(1), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(2));
		
		vanillaRenderCommands.forEach((command) -> command.run());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
	}

}
