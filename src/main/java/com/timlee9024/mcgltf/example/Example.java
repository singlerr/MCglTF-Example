package com.timlee9024.mcgltf.example;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.timlee9024.mcgltf.MCglTF;

import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
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

	public static ExampleBlock EXAMPLE_BLOCK;
	public static TileEntityType<ExampleTileEntity> EXAMPLE_TILE_ENTITY_TYPE;
	public static EntityType<ExampleEntity> EXAMPLE_ENTITY_TYPE;
	
	@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Server {
		
		@SubscribeEvent
		public static void onBlockRegistryEvent(final RegistryEvent.Register<Block> event) {
			EXAMPLE_BLOCK = new ExampleBlock(AbstractBlock.Properties.of(Material.STONE).strength(0.3F)
					.sound(SoundType.STONE)
					.noOcclusion()
					.isValidSpawn((a, b, c, d) -> false)
					.isRedstoneConductor((a, b, c) -> false)
					.isSuffocating((a, b, c) -> false)
					.isViewBlocking((a, b, c) -> false));
			EXAMPLE_BLOCK.setRegistryName(new ResourceLocation("mcgltf", "example_block"));
			event.getRegistry().register(EXAMPLE_BLOCK);
		}
		
		@SubscribeEvent
		public static void onTileEntityTypeRegistryEvent(final RegistryEvent.Register<TileEntityType<?>> event) {
			EXAMPLE_TILE_ENTITY_TYPE = TileEntityType.Builder.of(ExampleTileEntity::new, EXAMPLE_BLOCK).build(null);
			EXAMPLE_TILE_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_tileentity"));
			event.getRegistry().register(EXAMPLE_TILE_ENTITY_TYPE);
		}

		@SubscribeEvent
		public static void onEntityTypeRegistryEvent(final RegistryEvent.Register<EntityType<?>> event) {
			EXAMPLE_ENTITY_TYPE = EntityType.Builder.of(ExampleEntity::new, EntityClassification.MISC)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build("mcgltf:example_entity");
			EXAMPLE_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_entity"));
			event.getRegistry().register(EXAMPLE_ENTITY_TYPE);
		}
		
		@SubscribeEvent
		public static void onItemRegistryEvent(final RegistryEvent.Register<Item> event) {
			Item item = new Item(new Item.Properties().tab(ItemGroup.TAB_MISC));
			item.setRegistryName(new ResourceLocation("mcgltf", "example_item"));
			event.getRegistry().register(item);
			
			BlockItem blockItem = new BlockItem(EXAMPLE_BLOCK, new Item.Properties().tab(ItemGroup.TAB_MISC));
			item.setRegistryName(EXAMPLE_BLOCK.getRegistryName());
			event.getRegistry().register(blockItem);
			
			ForgeSpawnEggItem spawnEggItem = new ForgeSpawnEggItem(() -> EXAMPLE_ENTITY_TYPE, 12422002, 5651507, new Item.Properties().tab(ItemGroup.TAB_MISC));
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
			EXAMPLE_BLOCK = new ExampleBlock(AbstractBlock.Properties.of(Material.STONE).strength(0.3F)
					.sound(SoundType.STONE)
					.noOcclusion()
					.isValidSpawn((a, b, c, d) -> false)
					.isRedstoneConductor((a, b, c) -> false)
					.isSuffocating((a, b, c) -> false)
					.isViewBlocking((a, b, c) -> false));
			EXAMPLE_BLOCK.setRegistryName(new ResourceLocation("mcgltf", "example_block"));
			event.getRegistry().register(EXAMPLE_BLOCK);
		}
		
		@SubscribeEvent
		public static void onTileEntityTypeRegistryEvent(final RegistryEvent.Register<TileEntityType<?>> event) {
			EXAMPLE_TILE_ENTITY_TYPE = TileEntityType.Builder.of(ExampleTileEntity::new, EXAMPLE_BLOCK).build(null);
			EXAMPLE_TILE_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_tileentity"));
			event.getRegistry().register(EXAMPLE_TILE_ENTITY_TYPE);
		}

		@SubscribeEvent
		public static void onEntityTypeRegistryEvent(final RegistryEvent.Register<EntityType<?>> event) {
			EXAMPLE_ENTITY_TYPE = EntityType.Builder.of(ExampleEntity::new, EntityClassification.MISC)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build("mcgltf:example_entity");
			EXAMPLE_ENTITY_TYPE.setRegistryName(new ResourceLocation("mcgltf", "example_entity"));
			event.getRegistry().register(EXAMPLE_ENTITY_TYPE);
		}
		
		@SubscribeEvent
		public static void onItemRegistryEvent(final RegistryEvent.Register<Item> event) {
			AbstractItemGltfModelReceiver itemModelReceiver = new AbstractItemGltfModelReceiver() {

				@Override
				public ResourceLocation getModelLocation() {
					return new ResourceLocation("mcgltf", "models/item/water_bottle.gltf");
				}
			};
			MCglTF.getInstance().addGltfModelReceiver(itemModelReceiver);
			
			AbstractItemGltfModelReceiver blockItemModelReceiver = new AbstractItemGltfModelReceiver() {

				@Override
				public ResourceLocation getModelLocation() {
					return new ResourceLocation("mcgltf", "models/block/boom_box.gltf");
				}
			};
			MCglTF.getInstance().addGltfModelReceiver(blockItemModelReceiver);
			
			//According to Forge Doc "Each mod should only have one instance of a custom TEISR/ISTER/BEWLR.", due to creating an instance will also initiate unused fields inside the class which waste a lots of memory.
			ItemStackTileEntityRenderer ister = new ItemStackTileEntityRenderer() {

				@Override
				public void renderByItem(ItemStack p_239207_1_, TransformType p_239207_2_, MatrixStack p_239207_3_, IRenderTypeBuffer p_239207_4_, int p_239207_5_, int p_239207_6_) {
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
					
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glShadeModel(GL11.GL_SMOOTH);
					GL11.glEnable(GL11.GL_COLOR_MATERIAL);
					
					RenderSystem.multMatrix(p_239207_3_.last().pose());
					
					switch(p_239207_2_) {
					case THIRD_PERSON_LEFT_HAND:
					case THIRD_PERSON_RIGHT_HAND:
					case HEAD:
						GL11.glEnable(GL12.GL_RESCALE_NORMAL);
						GL11.glEnable(GL11.GL_DEPTH_TEST);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						
						GL13.glMultiTexCoord2s(GL13.GL_TEXTURE1, (short)(p_239207_6_ & '\uffff'), (short)(p_239207_6_ >> 16 & '\uffff'));
						GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(p_239207_5_ & '\uffff'), (short)(p_239207_5_ >> 16 & '\uffff'));
						
						MCglTF.CURRENT_PROGRAM = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
						if(MCglTF.CURRENT_PROGRAM == 0) {
							GL13.glActiveTexture(GL13.GL_TEXTURE1);
							GL11.glEnable(GL11.GL_TEXTURE_2D);
							GL13.glActiveTexture(GL13.GL_TEXTURE2);
							GL11.glEnable(GL11.GL_TEXTURE_2D);
							GL13.glActiveTexture(GL13.GL_TEXTURE0);
							
							renderWithVanillaCommands(p_239207_1_);
						}
						else {
							renderWithShaderModCommands(p_239207_1_);
						}
						break;
					case FIRST_PERSON_LEFT_HAND:
					case FIRST_PERSON_RIGHT_HAND:
					case GROUND:
					case FIXED:
						GL11.glEnable(GL12.GL_RESCALE_NORMAL);
						GL11.glEnable(GL11.GL_DEPTH_TEST);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						
						GL13.glMultiTexCoord2s(GL13.GL_TEXTURE2, (short)(p_239207_5_ & '\uffff'), (short)(p_239207_5_ >> 16 & '\uffff'));
						
						MCglTF.CURRENT_PROGRAM = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
						if(MCglTF.CURRENT_PROGRAM == 0) {
							GL13.glActiveTexture(GL13.GL_TEXTURE2);
							GL11.glEnable(GL11.GL_TEXTURE_2D);
							GL13.glActiveTexture(GL13.GL_TEXTURE0);
							
							renderWithVanillaCommands(p_239207_1_);
						}
						else {
							renderWithShaderModCommands(p_239207_1_);
						}
						break;
					default:
						MCglTF.CURRENT_PROGRAM = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
						if(MCglTF.CURRENT_PROGRAM == 0) {
							renderWithVanillaCommands(p_239207_1_);
						}
						else {
							renderWithShaderModCommands(p_239207_1_);
						}
						break;
					}
					
					GL11.glPopAttrib();
					GL11.glPopMatrix();
				}
				
				private void renderWithVanillaCommands(ItemStack itemStack) {
					Item currentItem = itemStack.getItem();
					if(currentItem == item) {
						Minecraft mc = Minecraft.getInstance();
						//Play every animation clips simultaneously
						for(Animation animation : itemModelReceiver.animations) {
							animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(mc.level, net.minecraftforge.client.model.animation.Animation.getPartialTickTime()) % animation.getEndTimeS());
						}
						itemModelReceiver.vanillaCommands.forEach((command) -> command.run());
					}
					else if(currentItem == blockItem) {
						Minecraft mc = Minecraft.getInstance();
						for(Animation animation : blockItemModelReceiver.animations) {
							animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(mc.level, net.minecraftforge.client.model.animation.Animation.getPartialTickTime()) % animation.getEndTimeS());
						}
						blockItemModelReceiver.vanillaCommands.forEach((command) -> command.run());
					}
				}
				
				private void renderWithShaderModCommands(ItemStack itemStack) {
					Item currentItem = itemStack.getItem();
					if(currentItem == item) {
						Minecraft mc = Minecraft.getInstance();
						for(Animation animation : itemModelReceiver.animations) {
							animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(mc.level, net.minecraftforge.client.model.animation.Animation.getPartialTickTime()) % animation.getEndTimeS());
						}
						itemModelReceiver.shaderModCommands.forEach((command) -> command.run());
					}
					else if(currentItem == blockItem) {
						Minecraft mc = Minecraft.getInstance();
						for(Animation animation : blockItemModelReceiver.animations) {
							animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(mc.level, net.minecraftforge.client.model.animation.Animation.getPartialTickTime()) % animation.getEndTimeS());
						}
						blockItemModelReceiver.shaderModCommands.forEach((command) -> command.run());
					}
				}
				
			};
			
			item = new Item(new Item.Properties().tab(ItemGroup.TAB_MISC).setISTER(() -> () -> ister));
			item.setRegistryName(new ResourceLocation("mcgltf", "example_item"));
			event.getRegistry().register(item);
			
			blockItem = new BlockItem(EXAMPLE_BLOCK, new Item.Properties().tab(ItemGroup.TAB_MISC).setISTER(() -> () -> ister));
			blockItem.setRegistryName(EXAMPLE_BLOCK.getRegistryName());
			event.getRegistry().register(blockItem);
			
			ForgeSpawnEggItem spawnEggItem = new ForgeSpawnEggItem(() -> EXAMPLE_ENTITY_TYPE, 12422002, 5651507, new Item.Properties().tab(ItemGroup.TAB_MISC));
			spawnEggItem.setRegistryName(new ResourceLocation("mcgltf", "example_entity_spawn_egg"));
			event.getRegistry().register(spawnEggItem);
		}

		@SubscribeEvent
		public static void onEvent(final EntityAttributeCreationEvent event) {
			event.put(EXAMPLE_ENTITY_TYPE, ExampleEntity.createAttributes().build());
		}
		
		@SubscribeEvent
		public static void onEvent(final FMLClientSetupEvent event) {
			ClientRegistry.bindTileEntityRenderer(EXAMPLE_TILE_ENTITY_TYPE, (dispatcher) -> {
				ExampleTileEntityRenderer ter = new ExampleTileEntityRenderer(dispatcher);
				MCglTF.getInstance().addGltfModelReceiver(ter);
				return ter;
			});
			
			RenderingRegistry.registerEntityRenderingHandler(EXAMPLE_ENTITY_TYPE, new IRenderFactory<ExampleEntity>() {

				@Override
				public EntityRenderer<? super ExampleEntity> createRenderFor(EntityRendererManager manager) {
					ExampleEntityRenderer entityRenderer = new ExampleEntityRenderer(manager);
					MCglTF.getInstance().addGltfModelReceiver(entityRenderer);
					return entityRenderer;
				}
				
			});
		}
	}

}
