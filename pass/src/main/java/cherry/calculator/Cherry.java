package cherry.calculator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.MinecraftServer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Cherry implements ModInitializer {

	@Override
	public void onInitialize() {
	}

	public static void generateCherryTree(ServerWorld world, BlockPos pos) {
		world.setBlockState(pos, Blocks.CHERRY_SAPLING.getDefaultState());
		BlockState saplingState = world.getBlockState(pos);
		if (saplingState.getBlock() == Blocks.CHERRY_SAPLING) {
			MinecraftServer server = world.getServer();
			server.execute(() -> {
				BlockState updatedState = world.getBlockState(pos);
				while (updatedState.getBlock() == Blocks.CHERRY_SAPLING) {
					((SaplingBlock) updatedState.getBlock()).grow(world, world.random, pos, updatedState);
					updatedState = world.getBlockState(pos);
				}
			});
		}
	}

	public static void deleteCherryTree(ServerWorld world, BlockPos pos) {
		BlockPos startPos = pos.add(-8, 0, -8);
		BlockPos endPos = pos.add(8, 10, 8);
		for (BlockPos blockPos : BlockPos.iterate(startPos, endPos)) {
			world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
		}
	}

	public static void saveMatrix(ServerWorld world, BlockPos pos) {
		BlockPos startPos = pos.add(-8, 0, -8);
		BlockPos endPos = pos.add(8, 10, 8);
		Map<String, Integer> blockData = new HashMap<>();

		for (BlockPos blockPos : BlockPos.iterate(startPos, endPos)) {
			BlockState state = world.getBlockState(blockPos);
			BlockPos relativePos = blockPos.subtract(startPos);

			int blockType;
			if (state.getBlock() == Blocks.CHERRY_LOG) {
				blockType = 0;
			} else if (state.getBlock() == Blocks.CHERRY_LEAVES) {
				blockType = 1;
			} else {
				continue;
			}

			blockData.put(relativePos.toShortString(), blockType);
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter("block_matrix.json")) {
			gson.toJson(blockData, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}