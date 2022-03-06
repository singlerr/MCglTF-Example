package com.timlee9024.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.RenderedGltfModel;

import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TileEntityRendererExample extends TileEntityRenderer<TileEntityExample> implements IGltfModelReceiver {

	protected List<Runnable> commands;
	
	protected List<Animation> animations;
	
	public TileEntityRendererExample(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/block/boom_box.gltf");
	}

	@Override
	public void onModelLoaded(RenderedGltfModel renderedModel) {
		commands = renderedModel.sceneCommands.get(0);
		animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
	}

	@Override
	public void render(TileEntityExample p_225616_1_, float p_225616_2_, MatrixStack p_225616_3_, IRenderTypeBuffer p_225616_4_, int p_225616_5_, int p_225616_6_) {
		if(commands != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(p_225616_6_ & '\uffff'), (short)(p_225616_6_ >> 16 & '\uffff'));
			GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(p_225616_5_ & '\uffff'), (short)(p_225616_5_ >> 16 & '\uffff'));
			
			RenderSystem.multMatrix(p_225616_3_.last().pose());
			World world = p_225616_1_.getLevel();
			if(world != null) {
				GL11.glTranslatef(0.5F, 0.0F, 0.5F); //Make sure it is in the center of the block
				switch(world.getBlockState(p_225616_1_.getBlockPos()).getOptionalValue(HorizontalBlock.FACING).orElse(Direction.NORTH)) {
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
			Minecraft mc = Minecraft.getInstance();
			for(Animation animation : animations) {
				animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(mc.level, p_225616_2_) % animation.getEndTimeS());
			}
			commands.forEach((command) -> command.run());
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}

}
