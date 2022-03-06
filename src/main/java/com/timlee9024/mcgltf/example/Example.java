package com.timlee9024.mcgltf.example;

import com.timlee9024.mcgltf.MCglTF;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod("example_mcgltf_usage")
public class Example {

	public static final BlockExample BLOCK_EXAMPLE = new BlockExample(AbstractBlock.Properties.of(Material.STONE).strength(0.3F).sound(SoundType.STONE).noOcclusion().isValidSpawn((a, b, c, d) -> false).isRedstoneConductor((a, b, c) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false));
	public static final TileEntityType<TileEntityExample> TILE_ENTITY_EXAMPLE_TYPE = TileEntityType.Builder.of(TileEntityExample::new, BLOCK_EXAMPLE).build(null);
	public static final EntityType<EntityExample> ENTITY_EXAMPLE = EntityType.Builder.of(EntityExample::new, EntityClassification.MISC).sized(0.6F, 1.95F).clientTrackingRange(10).build("mcgltf:example_entity");
	
	static {
		BLOCK_EXAMPLE.setRegistryName(new ResourceLocation("mcgltf", "example_block"));
		TILE_ENTITY_EXAMPLE_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_tileentity"));
		ENTITY_EXAMPLE.setRegistryName(new ResourceLocation("mcgltf", "example_entity"));
	}
	
	@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Server {
		
		@SubscribeEvent
		public static void onBlockRegistryEvent(final RegistryEvent.Register<Block> event) {
			event.getRegistry().register(BLOCK_EXAMPLE);
		}
		
		@SubscribeEvent
		public static void onTileEntityTypeRegistryEvent(final RegistryEvent.Register<TileEntityType<?>> event) {
			event.getRegistry().register(TILE_ENTITY_EXAMPLE_TYPE);
		}

		@SubscribeEvent
		public static void onEntityTypeRegistryEvent(final RegistryEvent.Register<EntityType<?>> event) {
			event.getRegistry().register(ENTITY_EXAMPLE);
		}
		
		@SubscribeEvent
		public static void onItemRegistryEvent(final RegistryEvent.Register<Item> event) {
			Item item = new Item(new Item.Properties().tab(ItemGroup.TAB_MISC));
			item.setRegistryName(new ResourceLocation("mcgltf", "example_item"));
			event.getRegistry().register(item);
			
			BlockItem blockItem = new BlockItem(BLOCK_EXAMPLE, new Item.Properties().tab(ItemGroup.TAB_MISC));
			item.setRegistryName(BLOCK_EXAMPLE.getRegistryName());
			event.getRegistry().register(blockItem);
			
			ForgeSpawnEggItem spawnEggItem = new ForgeSpawnEggItem(() -> ENTITY_EXAMPLE, 12422002, 5651507, new Item.Properties().tab(ItemGroup.TAB_MISC));
			spawnEggItem.setRegistryName(ENTITY_EXAMPLE.getRegistryName());
			event.getRegistry().register(spawnEggItem);
		}
		
		@SubscribeEvent
		public static void onEvent(final EntityAttributeCreationEvent event) {
			event.put(ENTITY_EXAMPLE, EntityExample.createAttributes().build());
		}
	}
	
	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Client {

		@SubscribeEvent
		public static void onBlockRegistryEvent(final RegistryEvent.Register<Block> event) {
			event.getRegistry().register(BLOCK_EXAMPLE);
		}
		
		@SubscribeEvent
		public static void onTileEntityTypeRegistryEvent(final RegistryEvent.Register<TileEntityType<?>> event) {
			event.getRegistry().register(TILE_ENTITY_EXAMPLE_TYPE);
		}
		
		@SubscribeEvent
		public static void onEntityTypeRegistryEvent(final RegistryEvent.Register<EntityType<?>> event) {
			event.getRegistry().register(ENTITY_EXAMPLE);
		}
		
		@SubscribeEvent
		public static void onItemRegistryEvent(final RegistryEvent.Register<Item> event) {
			ItemStackTileEntityRendererItemExample isterItem = new ItemStackTileEntityRendererItemExample();
			MCglTF.getInstance().addGltfModelReceiver(isterItem);
			Item item = new Item(new Item.Properties().tab(ItemGroup.TAB_MISC).setISTER(() -> () -> isterItem));
			item.setRegistryName(new ResourceLocation("mcgltf", "example_item"));
			event.getRegistry().register(item);
			
			ItemStackTileEntityRendererBlockItemExample isterBlockItem = new ItemStackTileEntityRendererBlockItemExample();
			MCglTF.getInstance().addGltfModelReceiver(isterBlockItem);
			BlockItem blockItem = new BlockItem(BLOCK_EXAMPLE, new Item.Properties().tab(ItemGroup.TAB_MISC).setISTER(() -> () -> isterBlockItem));
			blockItem.setRegistryName(BLOCK_EXAMPLE.getRegistryName());
			event.getRegistry().register(blockItem);
			
			ForgeSpawnEggItem spawnEggItem = new ForgeSpawnEggItem(() -> ENTITY_EXAMPLE, 12422002, 5651507, new Item.Properties().tab(ItemGroup.TAB_MISC));
			spawnEggItem.setRegistryName(ENTITY_EXAMPLE.getRegistryName());
			event.getRegistry().register(spawnEggItem);
		}

		@SubscribeEvent
		public static void onEvent(final EntityAttributeCreationEvent event) {
			event.put(ENTITY_EXAMPLE, EntityExample.createAttributes().build());
		}
		
		@SubscribeEvent
		public static void onEvent(final FMLClientSetupEvent event) {
			ClientRegistry.bindTileEntityRenderer(TILE_ENTITY_EXAMPLE_TYPE, (dispatcher) -> {
				TileEntityRendererExample ter = new TileEntityRendererExample(dispatcher);
				MCglTF.getInstance().addGltfModelReceiver(ter);
				return ter;
			});
			
			RenderingRegistry.registerEntityRenderingHandler(ENTITY_EXAMPLE, new IRenderFactory<EntityExample>() {

				@Override
				public EntityRenderer<? super EntityExample> createRenderFor(EntityRendererManager manager) {
					EntityRendererExample entityRenderer = new EntityRendererExample(manager);
					MCglTF.getInstance().addGltfModelReceiver(entityRenderer);
					return entityRenderer;
				}
				
			});
		}
	}

}
