package tree.matrix;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class TreeMatrixGenClient implements ClientModInitializer {
	private final TreeMatrixGen treeMatrixGen = new TreeMatrixGen();

	@Override
	public void onInitializeClient() {
		registerCommand("generate_cherry_tree", treeMatrixGen::generateCherryTree);
		registerCommand("delete_cherry_tree", treeMatrixGen::deleteCherryTree);
		registerCommand("save_matrix", treeMatrixGen::saveMatrix);
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
	private interface CommandExecutor {
		void execute(ServerWorld world, BlockPos pos);
	}
}