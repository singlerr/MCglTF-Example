package com.modularmods.mcgltf.example;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public abstract class CommonProxy {

	protected final Item item;
	
	protected final BlockExample block;
	protected final ItemBlock itemBlock;
	
	public CommonProxy() {
		item = new Item();
		item.setRegistryName(new ResourceLocation("mcgltf", "example_item"));
		item.setTranslationKey("mcgltf.example_item");
		item.setCreativeTab(CreativeTabs.MISC);
		
		block = new BlockExample(Material.GROUND, MapColor.AIR);
		block.setRegistryName(new ResourceLocation("mcgltf", "example_block"));
		block.setTranslationKey("mcgltf.example_block");
		block.setCreativeTab(CreativeTabs.MISC);
		itemBlock = new ItemBlock(block);
		itemBlock.setRegistryName(block.getRegistryName());
	}
	
	public void preInit(FMLPreInitializationEvent event) {}
	
	@SubscribeEvent
	public void onBlockRegistryEvent(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(block);
	}
	
	@SubscribeEvent
	public void onItemRegistryEvent(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(item);
		
		event.getRegistry().register(itemBlock);
	}
	
	@SubscribeEvent
	public void onEntityRegistryEvent(RegistryEvent.Register<EntityEntry> event) {
		event.getRegistry().register(
			EntityEntryBuilder.create()
					.entity(EntityExample.class)
					.id(new ResourceLocation("mcgltf", "example_entity"), 0)
					.name("mcgltf.example_entity")
					.tracker(64, 3, true)
					.egg(12422002, 5651507)
					.build()
					);
	}

}
