package com.modularmods.mcgltf.example;

import com.modularmods.mcgltf.ItemCameraTransformsHelper;
import com.modularmods.mcgltf.MCglTF;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		RenderingRegistry.registerEntityRenderingHandler(EntityExample.class, new IRenderFactory<EntityExample>() {

			@Override
			public Render<? super EntityExample> createRenderFor(RenderManager manager) {
				RenderEntityExample render = new RenderEntityExample(manager);
				MCglTF.getInstance().addGltfModelReceiver(render);
				return render;
			}
			
		});
	}
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		ItemRendererExample itemRenderer = new ItemRendererExample() {

			@Override
			public ResourceLocation getModelLocation() {
				return new ResourceLocation("mcgltf", "models/item/water_bottle.gltf");
			}
		};
		MCglTF.getInstance().addGltfModelReceiver(itemRenderer);
		
		ItemRendererExample blockItemRenderer = new ItemRendererExample() {

			@Override
			public ResourceLocation getModelLocation() {
				return new ResourceLocation("mcgltf", "models/block/boom_box.gltf");
			}
		};
		MCglTF.getInstance().addGltfModelReceiver(blockItemRenderer);
		
		//According to Forge Doc "Each mod should only have one instance of a custom TEISR/ISTER/BEWLR.", due to creating an instance will also initiate unused fields inside the class which waste a lots of memory.
		TileEntityItemStackRenderer teisr = new TileEntityItemStackRenderer() {

			@Override
			public void renderByItem(ItemStack itemStackIn) {
				Item currentItem = itemStackIn.getItem();
				if(currentItem == item) {
					itemRenderer.renderWithItemCameraTransformsHelper();
				}
				else if(currentItem == itemBlock) {
					blockItemRenderer.render();
				}
			}
			
		};
		
		item.setTileEntityItemStackRenderer(teisr);
		itemBlock.setTileEntityItemStackRenderer(teisr);
		
		TileEntitySpecialRendererExample tesr = new TileEntitySpecialRendererExample();
		MCglTF.getInstance().addGltfModelReceiver(tesr);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityExample.class, tesr);
	}
	
	@SubscribeEvent
	public void onEvent(ModelRegistryEvent event) {
		ItemCameraTransformsHelper.registerDummyModelToAccessCurrentTransformTypeForTEISR(item);
		
		//The regular way of register base model for TEISR, use this if you don't need perspective-wise rendering.
		ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation("mcgltf:example_item_block#inventory"));
	}

}
