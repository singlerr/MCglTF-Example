package com.timlee9024.mcgltf.example;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.timlee9024.mcgltf.MCglTF;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("example_mcgltf_usage")
public class Example {

	public static ExampleBlock EXAMPLE_BLOCK;
	public static BlockEntityType<ExampleBlockEntity> EXAMPLE_BLOCK_ENTITY_TYPE;
	public static EntityType<ExampleEntity> EXAMPLE_ENTITY_TYPE;
	
	@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Server {
		
		@SubscribeEvent
		public static void onBlockRegistryEvent(final RegistryEvent.Register<Block> event) {
			EXAMPLE_BLOCK = new ExampleBlock(BlockBehaviour.Properties.of(Material.STONE).strength(0.3F).sound(SoundType.STONE).noOcclusion().isValidSpawn((a, b, c, d) -> false).isRedstoneConductor((a, b, c) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false));
			EXAMPLE_BLOCK.setRegistryName(new ResourceLocation("mcgltf", "example_block"));
			event.getRegistry().register(EXAMPLE_BLOCK);
		}
		
		@SubscribeEvent
		public static void onBlockEntityTypeRegistryEvent(final RegistryEvent.Register<BlockEntityType<?>> event) {
			EXAMPLE_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(ExampleBlockEntity::new, EXAMPLE_BLOCK).build(null);
			EXAMPLE_BLOCK_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_blockentity"));
			event.getRegistry().register(EXAMPLE_BLOCK_ENTITY_TYPE);
		}
		
		@SubscribeEvent
		public static void onEntityTypeRegistryEvent(final RegistryEvent.Register<EntityType<?>> event) {
			EXAMPLE_ENTITY_TYPE = EntityType.Builder.of(ExampleEntity::new, MobCategory.MISC)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build("mcgltf:example_entity");
			EXAMPLE_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_entity"));
			event.getRegistry().register(EXAMPLE_ENTITY_TYPE);
		}
		
		@SubscribeEvent
		public static void onItemRegistryEvent(final RegistryEvent.Register<Item> event) {
			Item item = new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC));
			item.setRegistryName(new ResourceLocation("mcgltf", "example_item"));
			event.getRegistry().register(item);
			
			BlockItem blockItem = new BlockItem(EXAMPLE_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_MISC));
			blockItem.setRegistryName(EXAMPLE_BLOCK.getRegistryName());
			event.getRegistry().register(blockItem);
			
			ForgeSpawnEggItem spawnEggItem = new ForgeSpawnEggItem(() -> EXAMPLE_ENTITY_TYPE, 12422002, 5651507, new Item.Properties().tab(CreativeModeTab.TAB_MISC));
			spawnEggItem.setRegistryName(new ResourceLocation("mcgltf", "example_entity_spawn_egg"));
			event.getRegistry().register(spawnEggItem);
		}
		
		@SubscribeEvent
		public static void onEvent(final EntityAttributeCreationEvent event) {
			event.put(EXAMPLE_ENTITY_TYPE, ExampleEntity.createAttributes().build());
		}
	}
	
	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Client {

		private static Item item;
		private static BlockItem blockItem;
		
		@SubscribeEvent
		public static void onBlockRegistryEvent(final RegistryEvent.Register<Block> event) {
			EXAMPLE_BLOCK = new ExampleBlock(BlockBehaviour.Properties.of(Material.STONE).strength(0.3F).sound(SoundType.STONE).noOcclusion().isValidSpawn((a, b, c, d) -> false).isRedstoneConductor((a, b, c) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false));
			EXAMPLE_BLOCK.setRegistryName(new ResourceLocation("mcgltf", "example_block"));
			event.getRegistry().register(EXAMPLE_BLOCK);
		}
		
		@SubscribeEvent
		public static void onBlockEntityTypeRegistryEvent(final RegistryEvent.Register<BlockEntityType<?>> event) {
			EXAMPLE_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(ExampleBlockEntity::new, EXAMPLE_BLOCK).build(null);
			EXAMPLE_BLOCK_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_blockentity"));
			event.getRegistry().register(EXAMPLE_BLOCK_ENTITY_TYPE);
		}
		
		@SubscribeEvent
		public static void onEntityTypeRegistryEvent(final RegistryEvent.Register<EntityType<?>> event) {
			EXAMPLE_ENTITY_TYPE = EntityType.Builder.of(ExampleEntity::new, MobCategory.MISC)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build("mcgltf:example_entity");
			EXAMPLE_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_entity"));
			event.getRegistry().register(EXAMPLE_ENTITY_TYPE);
		}
		
		@SubscribeEvent
		public static void onItemRegistryEvent(final RegistryEvent.Register<Item> event) {
			ExampleItemRenderer itemRenderer = new ExampleItemRenderer() {

				@Override
				public ResourceLocation getModelLocation() {
					return new ResourceLocation("mcgltf", "models/item/water_bottle.gltf");
				}
			};
			MCglTF.getInstance().addGltfModelReceiver(itemRenderer);
			
			ExampleItemRenderer blockItemRenderer = new ExampleItemRenderer() {

				@Override
				public ResourceLocation getModelLocation() {
					return new ResourceLocation("mcgltf", "models/block/boom_box.gltf");
				}
			};
			MCglTF.getInstance().addGltfModelReceiver(blockItemRenderer);
			
			//According to Forge Doc "Each mod should only have one instance of a custom TEISR/ISTER/BEWLR.", due to creating an instance will also initiate unused fields inside the class which waste a lots of memory.
			BlockEntityWithoutLevelRenderer bewlr = new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {

				@Override
				public void renderByItem(ItemStack p_108830_, ItemTransforms.TransformType p_108831_, PoseStack p_108832_, MultiBufferSource p_108833_, int p_108834_, int p_108835_) {
					Item currentItem = p_108830_.getItem();
					if(currentItem == item) {
						itemRenderer.renderByItem(p_108831_, p_108832_, p_108833_, p_108834_, p_108835_);
					}
					else if(currentItem == blockItem) {
						blockItemRenderer.renderByItem(p_108831_, p_108832_, p_108833_, p_108834_, p_108835_);
					}
				}
				
			};
			IItemRenderProperties renderProperties = new IItemRenderProperties() {

				@Override
				public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
					return bewlr;
				}
				
			};
			
			item = new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)) {

				@Override
				public void initializeClient(Consumer<IItemRenderProperties> consumer) {
					consumer.accept(renderProperties);
				}
				
			};
			item.setRegistryName(new ResourceLocation("mcgltf", "example_item"));
			event.getRegistry().register(item);
			
			blockItem = new BlockItem(EXAMPLE_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_MISC)) {

				@Override
				public void initializeClient(Consumer<IItemRenderProperties> consumer) {
					consumer.accept(renderProperties);
				}
				
			};
			blockItem.setRegistryName(EXAMPLE_BLOCK.getRegistryName());
			event.getRegistry().register(blockItem);
			
			ForgeSpawnEggItem spawnEggItem = new ForgeSpawnEggItem(() -> EXAMPLE_ENTITY_TYPE, 12422002, 5651507, new Item.Properties().tab(CreativeModeTab.TAB_MISC));
			spawnEggItem.setRegistryName(new ResourceLocation("mcgltf", "example_entity_spawn_egg"));
			event.getRegistry().register(spawnEggItem);
		}
		
		@SubscribeEvent
		public static void onEvent(final EntityAttributeCreationEvent event) {
			event.put(EXAMPLE_ENTITY_TYPE, ExampleEntity.createAttributes().build());
		}
		
		@SubscribeEvent
		public static void onEvent(final EntityRenderersEvent.RegisterRenderers event) {
			event.registerBlockEntityRenderer(EXAMPLE_BLOCK_ENTITY_TYPE, (context) -> {
				ExampleBlockEntityRenderer ber = new ExampleBlockEntityRenderer();
				MCglTF.getInstance().addGltfModelReceiver(ber);
				return ber;
			});
			event.registerEntityRenderer(EXAMPLE_ENTITY_TYPE, (context) -> {
				ExampleEntityRenderer entityRenderer = new ExampleEntityRenderer(context);
				MCglTF.getInstance().addGltfModelReceiver(entityRenderer);
				return entityRenderer;
			});
		}
	}
}
