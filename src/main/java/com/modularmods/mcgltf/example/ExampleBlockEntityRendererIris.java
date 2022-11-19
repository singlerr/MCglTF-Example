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

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class ExampleBlockEntityRendererIris extends ExampleBlockEntityRenderer {

	public ExampleBlockEntityRendererIris(BlockEntityRenderDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(ExampleBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
		RenderType renderType = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
		vertexConsumers.getBuffer(renderType); //Put renderType into MultiBufferSource to ensure command submit to IrisRenderingHook will be run in Iris batched entity rendering.
		Matrix4f modelViewMatrix = matrices.last().pose().copy();
		
		if(MCglTF.getInstance().isShaderModActive()) {
			IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("BLOCK_ENTITIES", renderType, () -> {
				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				
				GL11.glShadeModel(GL11.GL_SMOOTH);
				
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glEnable(GL11.GL_BLEND); //Since the renderType is entity solid, we need to turn on blending manually.
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				RenderSystem.multMatrix(modelViewMatrix);
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
					
					GL11.glTranslatef(0.5F, 0.0F, 0.5F); //Make sure it is in the center of the block
					switch(level.getBlockState(blockEntity.getBlockPos()).getOptionalValue(HorizontalDirectionalBlock.FACING).orElse(Direction.NORTH)) {
					case DOWN:
						break;
					case UP:
						break;
					case NORTH:
						break;
					case SOUTH:
						GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
						break;
					case WEST:
						GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
						break;
					case EAST:
						GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
						break;
					}
				}
				
				GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
				GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
				
				renderedScene.renderForShaderMod();
				
				GL11.glPopAttrib();
				GL11.glPopMatrix();
			});
		}
		else {
			IrisRenderingHook.submitCommandForIrisRenderingByPhaseName("NONE", renderType, () -> {
				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				
				GL11.glShadeModel(GL11.GL_SMOOTH);
				
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				RenderSystem.multMatrix(modelViewMatrix);
				Level level = blockEntity.getLevel();
				if(level != null) {
					float time = (level.getGameTime() + tickDelta) / 20;
					for(List<InterpolatedChannel> animation : animations) {
						animation.parallelStream().forEach((channel) -> {
							float[] keys = channel.getKeys();
							channel.update(time % keys[keys.length - 1]);
						});
					}
					
					GL11.glTranslatef(0.5F, 0.0F, 0.5F);
					switch(level.getBlockState(blockEntity.getBlockPos()).getOptionalValue(HorizontalDirectionalBlock.FACING).orElse(Direction.NORTH)) {
					case DOWN:
						break;
					case UP:
						break;
					case NORTH:
						break;
					case SOUTH:
						GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
						break;
					case WEST:
						GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
						break;
					case EAST:
						GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
						break;
					}
				}
				
				GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(overlay & '\uffff'), (short)(overlay >> 16 & '\uffff'));
				GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(light & '\uffff'), (short)(light >> 16 & '\uffff'));
				
				renderedScene.renderForVanilla();
				
				GL11.glPopAttrib();
				GL11.glPopMatrix();
			});
		}
	}

}
