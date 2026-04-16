package me.noramibu.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.noramibu.config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class BetterDoorsCommand {
    private BetterDoorsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("betterdoors")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("reload")
                        .executes(context -> {
                            Config.reload();
                            context.getSource().sendSuccess(() -> Component.literal("BetterDoors configuration reloaded."), false);
                            return 1;
                        }));

        dispatcher.register(command);
    }
}
