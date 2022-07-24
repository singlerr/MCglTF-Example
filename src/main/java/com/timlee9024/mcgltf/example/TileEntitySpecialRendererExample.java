package com.timlee9024.mcgltf.example;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.RenderedGltfModel;

import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TileEntitySpecialRendererExample extends TileEntitySpecialRenderer<TileEntityExample> implements IGltfModelReceiver {

	protected List<Runnable> commands;
	
	protected List<Animation> animations;
	
	@Override
	public ResourceLocation getModelLocation() {
		return new ResourceLocation("mcgltf", "models/block/boom_box.gltf");
	}

	@Override
	public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
		commands = renderedModel.sceneCommands.get(0);
		animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
	}

	@Override
	public void render(TileEntityExample te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if(commands != null) {
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glTranslated(x, y, z);
			World world = te.getWorld();
			if(world != null) {
				GL11.glTranslatef(0.5F, 0.0F, 0.5F); //Make sure it is in the center of the block
				switch(world.getBlockState(te.getPos()).getValue(BlockHorizontal.FACING)) {
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
			for(Animation animation : animations) {
				animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(world, partialTicks) % animation.getEndTimeS());
			}
			commands.forEach((command) -> command.run());
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}

}
