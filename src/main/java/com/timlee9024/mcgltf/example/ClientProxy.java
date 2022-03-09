package com.timlee9024.mcgltf.example;

import com.timlee9024.mcgltf.ItemCameraTransformsHelper;
import com.timlee9024.mcgltf.MCglTF;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
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
		TileEntityItemStackRendererItemExample teisrItem = new TileEntityItemStackRendererItemExample();
		MCglTF.getInstance().addGltfModelReceiver(teisrItem);
		item.setTileEntityItemStackRenderer(teisrItem);
		
		TileEntitySpecialRendererExample tesr = new TileEntitySpecialRendererExample();
		MCglTF.getInstance().addGltfModelReceiver(tesr);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityExample.class, tesr);
		
		TileEntityItemStackRendererItemBlockExample teisrItemBlock = new TileEntityItemStackRendererItemBlockExample();
		MCglTF.getInstance().addGltfModelReceiver(teisrItemBlock);
		itemBlock.setTileEntityItemStackRenderer(teisrItemBlock);
	}
	
	@SubscribeEvent
	public void onEvent(ModelRegistryEvent event) {
		ItemCameraTransformsHelper.registerDummyModelToAccessCurrentTransformTypeForTEISR(item);
		
		//The regular way of register base model for TEISR, use this if you don't need perspective-wise rendering.
		ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation("mcgltf:example_item_block#inventory"));
	}

}
