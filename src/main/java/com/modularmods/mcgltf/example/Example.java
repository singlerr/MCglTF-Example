package com.modularmods.mcgltf.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Example implements ModInitializer {

	public static Example INSTANCE;
	
	public ExampleBlock exampleBlock;
	public BlockEntityType<ExampleBlockEntity> exampleBlockEntityType;
	public EntityType<ExampleEntity> exampleEntityType;
	public Item item;
	public BlockItem blockItem;
	
	@Override
	public void onInitialize() {
		INSTANCE = this;
		
		exampleBlock = new ExampleBlock(BlockBehaviour.Properties.of().strength(0.3F)
				.sound(SoundType.STONE)
				.noOcclusion()
				.isValidSpawn((a, b, c, d) -> false)
				.isRedstoneConductor((a, b, c) -> false)
				.isSuffocating((a, b, c) -> false)
				.isViewBlocking((a, b, c) -> false));
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("mcgltf", "example_block"), exampleBlock);
		
		exampleEntityType = EntityType.Builder.of(ExampleEntity::new, MobCategory.MISC)
				.sized(0.6F, 1.95F)
				.clientTrackingRange(10)
				.build("mcgltf:example_entity");
		Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation("mcgltf", "example_entity"), exampleEntityType);
		FabricDefaultAttributeRegistry.register(exampleEntityType, ExampleEntity.createAttributes());
		
		exampleBlockEntityType = FabricBlockEntityTypeBuilder.create(ExampleBlockEntity::new, exampleBlock).build();
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation("mcgltf", "example_blockentity"), exampleBlockEntityType);
		
		item = new Item(new Item.Properties());
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("mcgltf", "example_item"), item);
		
		blockItem = new BlockItem(exampleBlock, new Item.Properties());
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("mcgltf", "example_block"), blockItem);
		
		SpawnEggItem spawnEggItem = new SpawnEggItem(exampleEntityType, 12422002, 5651507,  new Item.Properties());
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("mcgltf", "example_entity_spawn_egg"), spawnEggItem);
		
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(listener -> {
			listener.addAfter(Items.GLASS_BOTTLE, item);
		});
		
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(listener -> {
			listener.addAfter(Items.JUKEBOX, blockItem);
		});
		
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(listener -> {
			listener.addAfter(Items.VILLAGER_SPAWN_EGG, spawnEggItem);
		});
	}
}
