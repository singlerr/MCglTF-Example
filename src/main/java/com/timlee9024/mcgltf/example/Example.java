package com.timlee9024.mcgltf.example;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.timlee9024.mcgltf.MCglTF;
import com.timlee9024.mcgltf.RenderedGltfModel;

import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod("example_mcgltf_usage")
public class Example {

	public static ExampleBlock EXAMPLE_BLOCK;
	public static BlockEntityType<ExampleBlockEntity> EXAMPLE_BLOCK_ENTITY_TYPE;
	public static EntityType<ExampleEntity> EXAMPLE_ENTITY_TYPE;
	
	@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Server {
		
		@SubscribeEvent
		public static void onEvent(RegisterEvent event) {
			event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
				EXAMPLE_BLOCK = new ExampleBlock(BlockBehaviour.Properties.of(Material.STONE).strength(0.3F).sound(SoundType.STONE).noOcclusion().isValidSpawn((a, b, c, d) -> false).isRedstoneConductor((a, b, c) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false));
				helper.register(new ResourceLocation("mcgltf", "example_block"), EXAMPLE_BLOCK);
			});
			
			event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
				EXAMPLE_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(ExampleBlockEntity::new, EXAMPLE_BLOCK).build(null);
				helper.register(new ResourceLocation("mcgltf", "example_blockentity"), EXAMPLE_BLOCK_ENTITY_TYPE);
			});
			
			event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
				EXAMPLE_ENTITY_TYPE = EntityType.Builder.of(ExampleEntity::new, MobCategory.MISC)
						.sized(0.6F, 1.95F)
						.clientTrackingRange(10)
						.build("mcgltf:example_entity");
				helper.register(new ResourceLocation("mcgltf", "example_entity"), EXAMPLE_ENTITY_TYPE);
			});
			
			event.register(ForgeRegistries.Keys.ITEMS, helper -> {
				helper.register(new ResourceLocation("mcgltf", "example_item"), new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
				helper.register(new ResourceLocation("mcgltf", "example_block"), new BlockItem(EXAMPLE_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
				helper.register(new ResourceLocation("mcgltf", "example_entity_spawn_egg"), new ForgeSpawnEggItem(() -> EXAMPLE_ENTITY_TYPE, 12422002, 5651507, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
			});
		}
		
		@SubscribeEvent
		public static void onEvent(EntityAttributeCreationEvent event) {
			event.put(EXAMPLE_ENTITY_TYPE, ExampleEntity.createAttributes().build());
		}
	}
	
	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Client {

		private static Item item;
		private static BlockItem blockItem;
		
		@SubscribeEvent
		public static void onEvent(RegisterEvent event) {
			event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
				EXAMPLE_BLOCK = new ExampleBlock(BlockBehaviour.Properties.of(Material.STONE).strength(0.3F).sound(SoundType.STONE).noOcclusion().isValidSpawn((a, b, c, d) -> false).isRedstoneConductor((a, b, c) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false));
				helper.register(new ResourceLocation("mcgltf", "example_block"), EXAMPLE_BLOCK);
			});
			
			event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
				EXAMPLE_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(ExampleBlockEntity::new, EXAMPLE_BLOCK).build(null);
				helper.register(new ResourceLocation("mcgltf", "example_blockentity"), EXAMPLE_BLOCK_ENTITY_TYPE);
			});
			
			event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
				EXAMPLE_ENTITY_TYPE = EntityType.Builder.of(ExampleEntity::new, MobCategory.MISC)
						.sized(0.6F, 1.95F)
						.clientTrackingRange(10)
						.build("mcgltf:example_entity");
				helper.register(new ResourceLocation("mcgltf", "example_entity"), EXAMPLE_ENTITY_TYPE);
			});
			
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
			BlockEntityWithoutLevelRenderer bewlr = new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {

				@Override
				public void renderByItem(ItemStack p_108830_, TransformType p_108831_, PoseStack p_108832_, MultiBufferSource p_108833_, int p_108834_, int p_108835_) {
					int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
					int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
					int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
					
					boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
					
					boolean currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					
					switch(p_108831_) {
					case THIRD_PERSON_LEFT_HAND:
					case THIRD_PERSON_RIGHT_HAND:
					case HEAD:
						boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
						GL11.glEnable(GL11.GL_BLEND);
						GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						
						MCglTF.CURRENT_POSE = p_108832_.last().pose();
						MCglTF.CURRENT_NORMAL = p_108832_.last().normal();
						
						GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, p_108835_ & '\uffff', p_108835_ >> 16 & '\uffff');
						GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, p_108834_ & '\uffff', p_108834_ >> 16 & '\uffff');
						
						MCglTF.CURRENT_PROGRAM = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
						if(MCglTF.CURRENT_PROGRAM == 0) { //When Optifine is not installed or shader is setting to "internal"
							renderLightOverlayTextureWithVanillaCommands(p_108830_);
							GL20.glUseProgram(0);
						}
						else {
							MCglTF.MODEL_VIEW_MATRIX = GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "modelViewMatrix");
							if(MCglTF.MODEL_VIEW_MATRIX == -1) { //When Optifine is installed and shader is setting to "none"
								int currentProgram = MCglTF.CURRENT_PROGRAM;
								renderLightOverlayTextureWithVanillaCommands(p_108830_);
								GL20.glUseProgram(currentProgram);
							}
							else {
								renderWithShaderModCommands(p_108830_);
							}
						}
						
						GL30.glVertexAttribI2i(RenderedGltfModel.vaUV1, 0, 0);
						GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
						
						if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
						break;
					case FIRST_PERSON_LEFT_HAND:
					case FIRST_PERSON_RIGHT_HAND:
					case GROUND:
					case FIXED:
						currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
						GL11.glEnable(GL11.GL_BLEND);
						GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						
						MCglTF.CURRENT_POSE = p_108832_.last().pose();
						MCglTF.CURRENT_NORMAL = p_108832_.last().normal();
						
						GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, p_108834_ & '\uffff', p_108834_ >> 16 & '\uffff');
						
						MCglTF.CURRENT_PROGRAM = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
						if(MCglTF.CURRENT_PROGRAM == 0) {
							renderLightTextureWithVanillaCommands(p_108830_);
							GL20.glUseProgram(0);
						}
						else {
							MCglTF.MODEL_VIEW_MATRIX = GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "modelViewMatrix");
							if(MCglTF.MODEL_VIEW_MATRIX == -1) {
								int currentProgram = MCglTF.CURRENT_PROGRAM;
								renderLightTextureWithVanillaCommands(p_108830_);
								GL20.glUseProgram(currentProgram);
							}
							else {
								renderWithShaderModCommands(p_108830_);
							}
						}
						
						GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);
						
						if(!currentBlend) GL11.glDisable(GL11.GL_BLEND);
						break;
					case GUI:
						Quaternion rotateAround = new Quaternion(0.0F, 1.0F, 0.0F, 0.0F);
						MCglTF.CURRENT_POSE = RenderSystem.getModelViewMatrix().copy();
						MCglTF.CURRENT_POSE.multiply(rotateAround);
						MCglTF.CURRENT_NORMAL = new Matrix3f();
						MCglTF.CURRENT_NORMAL.setIdentity();
						MCglTF.CURRENT_NORMAL.mul(rotateAround);
						
						int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
						
						GL13.glActiveTexture(GL13.GL_TEXTURE2);
						int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getDefaultColorMap());
						
						GL13.glActiveTexture(GL13.GL_TEXTURE1);
						int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
						
						GL13.glActiveTexture(GL13.GL_TEXTURE0);
						int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
						
						renderWithVanillaCommands(p_108830_);
						
						GL13.glActiveTexture(GL13.GL_TEXTURE2);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2);
						GL13.glActiveTexture(GL13.GL_TEXTURE1);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
						GL13.glActiveTexture(GL13.GL_TEXTURE0);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
						
						GL20.glUseProgram(currentProgram);
						break;
					default:
						break;
					}
					
					if(!currentDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
					
					if(currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
					else GL11.glDisable(GL11.GL_CULL_FACE);
					
					GL30.glBindVertexArray(currentVAO);
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
					GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
				}
				
				private void renderLightTextureWithVanillaCommands(ItemStack itemStack) {
					GL13.glActiveTexture(GL13.GL_TEXTURE2);
					int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getLightTexture().getId());
					
					GL13.glActiveTexture(GL13.GL_TEXTURE1);
					int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
					
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					
					renderWithVanillaCommands(itemStack);
					
					GL13.glActiveTexture(GL13.GL_TEXTURE2);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2);
					GL13.glActiveTexture(GL13.GL_TEXTURE1);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
				}
				
				private void renderLightOverlayTextureWithVanillaCommands(ItemStack itemStack) {
					GL13.glActiveTexture(GL13.GL_TEXTURE2);
					int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getLightTexture().getId());
					
					GL13.glActiveTexture(GL13.GL_TEXTURE1);
					int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					Minecraft mc = Minecraft.getInstance();
					mc.gameRenderer.overlayTexture().setupOverlayColor();
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(1));
					mc.gameRenderer.overlayTexture().teardownOverlayColor();
					
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					
					renderWithVanillaCommands(itemStack);
					
					GL13.glActiveTexture(GL13.GL_TEXTURE2);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2);
					GL13.glActiveTexture(GL13.GL_TEXTURE1);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
				}
				
				private void renderWithVanillaCommands(ItemStack itemStack) {
					Item currentItem = itemStack.getItem();
					if(currentItem == item) {
						Minecraft mc = Minecraft.getInstance();
						for(Animation animation : itemModelReceiver.animations) {
							animation.update((mc.level.getGameTime() + MinecraftForgeClient.getPartialTick()) / 20 % animation.getEndTimeS());
						}
						itemModelReceiver.vanillaSkinningCommands.run();
						
						setupVanillaShader();
						
						itemModelReceiver.vanillaRenderCommands.forEach((command) -> command.run());
					}
					else if(currentItem == blockItem) {
						Minecraft mc = Minecraft.getInstance();
						for(Animation animation : blockItemModelReceiver.animations) {
							animation.update((mc.level.getGameTime() + MinecraftForgeClient.getPartialTick()) / 20 % animation.getEndTimeS());
						}
						blockItemModelReceiver.vanillaSkinningCommands.run();
						
						setupVanillaShader();
						
						blockItemModelReceiver.vanillaRenderCommands.forEach((command) -> command.run());
					}
				}
				
				private void setupVanillaShader() {
					MCglTF.CURRENT_SHADER_INSTANCE = GameRenderer.getRendertypeEntitySolidShader();
					MCglTF.CURRENT_PROGRAM = MCglTF.CURRENT_SHADER_INSTANCE.getId();
					GL20.glUseProgram(MCglTF.CURRENT_PROGRAM);
					
					MCglTF.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
					MCglTF.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.upload();
					
					MCglTF.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
					MCglTF.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.upload();
					
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_START.set(RenderSystem.getShaderFogStart());
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_START.upload();
					
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_END.set(RenderSystem.getShaderFogEnd());
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_END.upload();
					
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_COLOR.set(RenderSystem.getShaderFogColor());
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_COLOR.upload();
					
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
					MCglTF.CURRENT_SHADER_INSTANCE.FOG_SHAPE.upload();
					
					MCglTF.CURRENT_SHADER_INSTANCE.COLOR_MODULATOR.set(1.0F, 1.0F, 1.0F, 1.0F);
					MCglTF.CURRENT_SHADER_INSTANCE.COLOR_MODULATOR.upload();
					
					GL20.glUniform1i(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "Sampler0"), 0);
					GL20.glUniform1i(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "Sampler1"), 1);
					GL20.glUniform1i(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "Sampler2"), 2);
					
					RenderSystem.setupShaderLights(MCglTF.CURRENT_SHADER_INSTANCE);
					MCglTF.LIGHT0_DIRECTION = new Vector3f(MCglTF.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(0), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(1), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(2));
					MCglTF.LIGHT1_DIRECTION = new Vector3f(MCglTF.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(0), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(1), MCglTF.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(2));
				}
				
				private void renderWithShaderModCommands(ItemStack itemStack) {
					MCglTF.MODEL_VIEW_MATRIX_INVERSE = GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "modelViewMatrixInverse");
					MCglTF.NORMAL_MATRIX = GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "normalMatrix");
					
					RenderSystem.getProjectionMatrix().store(MCglTF.BUF_FLOAT_16);
					GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "projectionMatrix"), false, MCglTF.BUF_FLOAT_16);
					Matrix4f projectionMatrixInverse = RenderSystem.getProjectionMatrix().copy();
					projectionMatrixInverse.invert();
					projectionMatrixInverse.store(MCglTF.BUF_FLOAT_16);
					GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(MCglTF.CURRENT_PROGRAM, "projectionMatrixInverse"), false, MCglTF.BUF_FLOAT_16);
					
					GL13.glActiveTexture(GL13.GL_TEXTURE3);
					int currentTexture3 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					GL13.glActiveTexture(GL13.GL_TEXTURE1);
					int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
					
					Item currentItem = itemStack.getItem();
					if(currentItem == item) {
						Minecraft mc = Minecraft.getInstance();
						for(Animation animation : itemModelReceiver.animations) {
							animation.update((mc.level.getGameTime() + MinecraftForgeClient.getPartialTick()) / 20 % animation.getEndTimeS());
						}
						itemModelReceiver.shaderModCommands.forEach((command) -> command.run());
					}
					else if(currentItem == blockItem) {
						Minecraft mc = Minecraft.getInstance();
						for(Animation animation : blockItemModelReceiver.animations) {
							animation.update((mc.level.getGameTime() + MinecraftForgeClient.getPartialTick()) / 20 % animation.getEndTimeS());
						}
						blockItemModelReceiver.shaderModCommands.forEach((command) -> command.run());
					}
					
					GL13.glActiveTexture(GL13.GL_TEXTURE3);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture3);
					GL13.glActiveTexture(GL13.GL_TEXTURE1);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
				}
			};
			IItemRenderProperties renderProperties = new IItemRenderProperties() {

				@Override
				public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
					return bewlr;
				}
				
			};
			
			event.register(ForgeRegistries.Keys.ITEMS, helper -> {
				item = new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)) {

					@Override
					public void initializeClient(Consumer<IItemRenderProperties> consumer) {
						consumer.accept(renderProperties);
					}
					
				};;
				helper.register(new ResourceLocation("mcgltf", "example_item"), item);
				blockItem = new BlockItem(EXAMPLE_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_MISC)) {

					@Override
					public void initializeClient(Consumer<IItemRenderProperties> consumer) {
						consumer.accept(renderProperties);
					}
					
				};
				helper.register(new ResourceLocation("mcgltf", "example_block"), blockItem);
				helper.register(new ResourceLocation("mcgltf", "example_entity_spawn_egg"), new ForgeSpawnEggItem(() -> EXAMPLE_ENTITY_TYPE, 12422002, 5651507, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
			});
		}
		
		@SubscribeEvent
		public static void onEvent(EntityAttributeCreationEvent event) {
			event.put(EXAMPLE_ENTITY_TYPE, ExampleEntity.createAttributes().build());
		}
		
		@SubscribeEvent
		public static void onEvent(EntityRenderersEvent.RegisterRenderers event) {
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
