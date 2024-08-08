package tree.matrix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TreeMatrixGen implements ModInitializer {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private volatile boolean running = false;
	private final List<Map<String, Integer>> allBlockData = new ArrayList<>();

	@Override
	public void onInitialize() {
		registerCommandWithLoops("generate_loop", this::startLoop);
		registerCommand("kill_loop", this::stopLoop);
	}

	private void startLoop(ServerWorld world, BlockPos pos, int numLoops) {
		if (!running) {
			running = true;
			final int[] counter = {0};
			scheduler.scheduleAtFixedRate(() -> {
				if (running && counter[0] < numLoops) {
					generateCherryTree(world, pos);
					saveMatrix(world, pos);
					deleteCherryTree(world, pos);
					counter[0]++;
					world.getServer().getPlayerManager().broadcast(Text.literal("Current loop: " + counter[0]), false);
				} else {
					running = false;
					saveAllMatricesToFile();
				}
			}, 1, 1, TimeUnit.MILLISECONDS);
		}
	}

	private void stopLoop(ServerWorld world, BlockPos pos) {
		running = false;
	}

	private void registerCommandWithLoops(String commandName, CommandExecutorWithLoops executor) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal(commandName)
					.then(CommandManager.argument("x", IntegerArgumentType.integer())
							.then(CommandManager.argument("y", IntegerArgumentType.integer())
									.then(CommandManager.argument("z", IntegerArgumentType.integer())
											.then(CommandManager.argument("loops", IntegerArgumentType.integer())
													.executes(context -> {
														BlockPos pos = getCoordinates(context);
														ServerWorld world = context.getSource().getWorld();
														int loops = IntegerArgumentType.getInteger(context, "loops");
														executor.execute(world, pos, loops);
														return 1;
													}))))));
		});
	}

	private void registerCommand(String commandName, CommandExecutor executor) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal(commandName)
					.then(CommandManager.argument("x", IntegerArgumentType.integer())
							.then(CommandManager.argument("y", IntegerArgumentType.integer())
									.then(CommandManager.argument("z", IntegerArgumentType.integer())
											.executes(context -> {
												BlockPos pos = getCoordinates(context);
												ServerWorld world = context.getSource().getWorld();
												executor.execute(world, pos);
												return 1;
											})))));
		});
	}

	private BlockPos getCoordinates(CommandContext<ServerCommandSource> context) {
		int x = IntegerArgumentType.getInteger(context, "x");
		int y = IntegerArgumentType.getInteger(context, "y");
		int z = IntegerArgumentType.getInteger(context, "z");
		return new BlockPos(x, y, z);
	}

	@FunctionalInterface
	private interface CommandExecutorWithLoops {
		void execute(ServerWorld world, BlockPos pos, int loops);
	}

	@FunctionalInterface
	private interface CommandExecutor {
		void execute(ServerWorld world, BlockPos pos);
	}

	public void generateCherryTree(ServerWorld world, BlockPos pos) {
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

	public void deleteCherryTree(ServerWorld world, BlockPos pos) {
		BlockPos startPos = pos.add(-8, 0, -8);
		BlockPos endPos = pos.add(8, 10, 8);
		for (BlockPos blockPos : BlockPos.iterate(startPos, endPos)) {
			world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
		}
	}

	public void saveMatrix(ServerWorld world, BlockPos pos) {
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

		allBlockData.add(blockData);
	}

	private void saveAllMatricesToFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter("block_matrix.json")) {
			gson.toJson(allBlockData, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}