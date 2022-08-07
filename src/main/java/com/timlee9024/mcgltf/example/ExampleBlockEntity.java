package com.timlee9024.mcgltf.example;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ExampleBlockEntity extends BlockEntity {

	public ExampleBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(Example.INSTANCE.exampleBlockEntityType, p_155229_, p_155230_);
	}

}
