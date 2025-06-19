package me.noramibu;

import me.noramibu.command.Command;
import me.noramibu.config.Config;
import me.noramibu.event.DoorOpenHandler;
import me.noramibu.event.KnockingHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterDoors implements ModInitializer {
	public static final String MOD_ID = "better-doors";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		Config.load();
		AttackBlockCallback.EVENT.register(new KnockingHandler());
		UseBlockCallback.EVENT.register(new DoorOpenHandler());
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Command.register(dispatcher));
		LOGGER.info("Better Doors has been initialized.");
	}
}