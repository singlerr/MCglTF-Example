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
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ExampleTileEntityRenderer extends TileEntityRenderer<ExampleTileEntity> implements IGltfModelReceiver {

	protected List<Runnable> vanillaCommands;
	
	protected List<Runnable> shaderModCommands;
	
	protected List<Animation> animations;
	
	public ExampleTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/block/boom_box.gltf");
	}

	@Override
	public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
		vanillaCommands = renderedModel.vanillaSceneCommands.get(0);
		shaderModCommands = renderedModel.shaderModSceneCommands.get(0);
		animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
	}

	/**
	 * Since you use custom ISTER for BlockItem instead of TER, the last parameters p_225616_6_ which control overlay color is almost unused.
	 */
	@Override
	public void render(ExampleTileEntity p_225616_1_, float p_225616_2_, MatrixStack p_225616_3_, IRenderTypeBuffer p_225616_4_, int p_225616_5_, int p_225616_6_) {
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
			
			GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(p_225616_5_ & '\uffff'), (short)(p_225616_5_ >> 16 & '\uffff'));
			
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
	}

}
