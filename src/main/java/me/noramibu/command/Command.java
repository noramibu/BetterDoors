package me.noramibu.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.noramibu.config.Config;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class Command {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("betterdoors")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            Config.load();
                            context.getSource().sendFeedback(() -> Text.literal("BetterDoors configuration reloaded."), false);
                            return 1;
                        }));

        dispatcher.register(command);
    }
} 