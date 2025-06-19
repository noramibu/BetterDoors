package me.noramibu.config;

import com.moandjiezana.toml.Toml;
import me.noramibu.BetterDoors;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static Toml toml = new Toml();

    // Knocking settings
    public static boolean knockingRequiresEmptyHand;
    public static boolean knockingRequiresShift;
    public static boolean requirePermissionToKnock;
    public static boolean allowKnockingWoodenDoors;
    public static boolean allowKnockingIronDoors;
    public static boolean allowKnockingCopperDoors;
    public static boolean allowKnockingWoodenTrapdoors;
    public static boolean allowKnockingIronTrapdoors;
    public static boolean allowKnockingCopperTrapdoors;

    // Sound settings
    public static String soundKnockIron;
    public static String soundKnockWood;
    public static String soundKnockCopper;
    public static double soundKnockVolume;
    public static double soundKnockPitch;

    // Double Door settings
    public static boolean allowDoubleWoodenDoors;
    public static boolean allowDoubleIronDoors;
    public static boolean allowDoubleCopperDoors;
    public static boolean checkForRedstone;
    public static boolean requirePermissionForDoubleDoors;

    public static void load() {
        try {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve(BetterDoors.MOD_ID + ".toml");

            if (!Files.exists(configPath)) {
                FabricLoader.getInstance().getModContainer(BetterDoors.MOD_ID)
                        .flatMap(container -> container.findPath("better-doors.toml"))
                        .ifPresent(path -> {
                            try {
                                Files.copy(path, configPath);
                            } catch (IOException e) {
                                BetterDoors.LOGGER.error("Failed to copy default config", e);
                            }
                        });
            }

            toml = new Toml().read(configPath.toFile());
        } catch (Exception e) {
            BetterDoors.LOGGER.error("Failed to load config", e);
            toml = new Toml(); // Use empty config on failure
        }

        // Load knocking settings
        knockingRequiresEmptyHand = toml.getBoolean("knocking-requires-empty-hand", true);
        knockingRequiresShift = toml.getBoolean("knocking-requires-shift", false);
        requirePermissionToKnock = toml.getBoolean("require-permission-to-knock", false);
        allowKnockingWoodenDoors = toml.getBoolean("allow-knocking-wooden-doors", true);
        allowKnockingIronDoors = toml.getBoolean("allow-knocking-iron-doors", true);
        allowKnockingCopperDoors = toml.getBoolean("allow-knocking-copper-doors", true);
        allowKnockingWoodenTrapdoors = toml.getBoolean("allow-knocking-wooden-trapdoors", true);
        allowKnockingIronTrapdoors = toml.getBoolean("allow-knocking-iron-trapdoors", true);
        allowKnockingCopperTrapdoors = toml.getBoolean("allow-knocking-copper-trapdoors", true);

        // Load sound settings
        soundKnockIron = toml.getString("sound-knock-iron", "minecraft:entity.zombie.attack_iron_door");
        soundKnockWood = toml.getString("sound-knock-wood", "minecraft:item.shield.block");
        soundKnockCopper = toml.getString("sound-knock-copper", "minecraft:block.copper.step");
        soundKnockVolume = toml.getDouble("sound-knock-volume", 1.0);
        soundKnockPitch = toml.getDouble("sound-knock-pitch", 1.0);

        // Load Double Door settings
        allowDoubleWoodenDoors = toml.getBoolean("allow-double-wooden-doors", true);
        allowDoubleIronDoors = toml.getBoolean("allow-double-iron-doors", false);
        allowDoubleCopperDoors = toml.getBoolean("allow-double-copper-doors", true);
        checkForRedstone = toml.getBoolean("check-for-redstone", true);
        requirePermissionForDoubleDoors = toml.getBoolean("require-permission-for-double-doors", false);
    }
} 