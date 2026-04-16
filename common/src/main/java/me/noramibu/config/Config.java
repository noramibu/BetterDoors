package me.noramibu.config;

import me.noramibu.BetterDoorsCommon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public final class Config {
    private static final Map<String, String> VALUES = new HashMap<>();
    private static Path configPath;

    private Config() {
    }

    public static boolean knockingRequiresEmptyHand;
    public static boolean knockingRequiresShift;
    public static boolean requirePermissionToKnock;
    public static boolean allowKnockingWoodenDoors;
    public static boolean allowKnockingOakDoors;
    public static boolean allowKnockingSpruceDoors;
    public static boolean allowKnockingBirchDoors;
    public static boolean allowKnockingJungleDoors;
    public static boolean allowKnockingAcaciaDoors;
    public static boolean allowKnockingDarkOakDoors;
    public static boolean allowKnockingMangroveDoors;
    public static boolean allowKnockingBambooDoors;
    public static boolean allowKnockingCherryDoors;
    public static boolean allowKnockingCrimsonDoors;
    public static boolean allowKnockingWarpedDoors;
    public static boolean allowKnockingPaleOakDoors;
    public static boolean allowKnockingIronDoors;
    public static boolean allowKnockingCopperDoors;
    public static boolean allowKnockingWoodenTrapdoors;
    public static boolean allowKnockingIronTrapdoors;
    public static boolean allowKnockingCopperTrapdoors;
    public static boolean allowKnockingFenceGates;
    public static boolean requirePermissionToKnockFenceGates;

    public static String soundKnockIron;
    public static String soundKnockWood;
    public static String soundKnockCopper;
    public static double soundKnockVolume;
    public static double soundKnockPitch;

    public static boolean allowDoubleWoodenDoors;
    public static boolean allowDoubleIronDoors;
    public static boolean allowDoubleCopperDoors;
    public static boolean allowDoubleFenceGates;
    public static boolean checkForRedstone;
    public static boolean requirePermissionForDoubleDoors;
    public static boolean requirePermissionForDoubleFenceGates;

    public static synchronized void load(Path configDir, String fileName) {
        configPath = configDir.resolve(fileName);
        ensureConfigExists(configDir, fileName);
        readConfig();
    }

    public static synchronized void reload() {
        if (configPath == null) {
            BetterDoorsCommon.LOGGER.warn("Config reload requested before initialization.");
            return;
        }
        readConfig();
    }

    private static void ensureConfigExists(Path configDir, String fileName) {
        try {
            Files.createDirectories(configDir);
            if (!Files.exists(configPath)) {
                try (InputStream in = Config.class.getClassLoader().getResourceAsStream(fileName)) {
                    if (in == null) {
                        BetterDoorsCommon.LOGGER.warn("Default config {} is missing from resources.", fileName);
                        Files.createFile(configPath);
                    } else {
                        Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            BetterDoorsCommon.LOGGER.error("Failed to create default config at {}", configPath, e);
        }
    }

    private static void readConfig() {
        VALUES.clear();

        if (configPath == null || !Files.exists(configPath)) {
            BetterDoorsCommon.LOGGER.warn("Config file not found at {}", configPath);
            applyDefaults();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception e) {
            BetterDoorsCommon.LOGGER.error("Failed to read config from {}", configPath, e);
        }

        applyDefaults();
    }

    private static void parseLine(String rawLine) {
        String line = stripComment(rawLine).trim();
        if (line.isEmpty()) {
            return;
        }

        int equalsIndex = line.indexOf('=');
        if (equalsIndex < 0) {
            return;
        }

        String key = line.substring(0, equalsIndex).trim();
        if (key.isEmpty()) {
            return;
        }

        String value = line.substring(equalsIndex + 1).trim();
        if (value.isEmpty()) {
            return;
        }

        VALUES.put(key, unquote(value));
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        char quoteChar = 0;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (!quoted && (ch == '"' || ch == '\'')) {
                quoted = true;
                quoteChar = ch;
                continue;
            }
            if (quoted && ch == quoteChar) {
                quoted = false;
                quoteChar = 0;
                continue;
            }
            if (!quoted && ch == '#') {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        String value = VALUES.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private static String getString(String key, String defaultValue) {
        String value = VALUES.get(key);
        return value == null ? defaultValue : value;
    }

    private static double getDouble(String key, double defaultValue) {
        String value = VALUES.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static void applyDefaults() {
        knockingRequiresEmptyHand = getBoolean("knocking-requires-empty-hand", true);
        knockingRequiresShift = getBoolean("knocking-requires-shift", false);
        requirePermissionToKnock = getBoolean("require-permission-to-knock", false);
        allowKnockingWoodenDoors = getBoolean("allow-knocking-wooden-doors", true);
        allowKnockingOakDoors = getBoolean("allow-knocking-oak-doors", true);
        allowKnockingSpruceDoors = getBoolean("allow-knocking-spruce-doors", true);
        allowKnockingBirchDoors = getBoolean("allow-knocking-birch-doors", true);
        allowKnockingJungleDoors = getBoolean("allow-knocking-jungle-doors", true);
        allowKnockingAcaciaDoors = getBoolean("allow-knocking-acacia-doors", true);
        allowKnockingDarkOakDoors = getBoolean("allow-knocking-dark-oak-doors", true);
        allowKnockingMangroveDoors = getBoolean("allow-knocking-mangrove-doors", true);
        allowKnockingBambooDoors = getBoolean("allow-knocking-bamboo-doors", true);
        allowKnockingCherryDoors = getBoolean("allow-knocking-cherry-doors", true);
        allowKnockingCrimsonDoors = getBoolean("allow-knocking-crimson-doors", true);
        allowKnockingWarpedDoors = getBoolean("allow-knocking-warped-doors", true);
        allowKnockingPaleOakDoors = getBoolean("allow-knocking-pale-oak-doors", true);
        allowKnockingIronDoors = getBoolean("allow-knocking-iron-doors", true);
        allowKnockingCopperDoors = getBoolean("allow-knocking-copper-doors", true);
        allowKnockingWoodenTrapdoors = getBoolean("allow-knocking-wooden-trapdoors", true);
        allowKnockingIronTrapdoors = getBoolean("allow-knocking-iron-trapdoors", true);
        allowKnockingCopperTrapdoors = getBoolean("allow-knocking-copper-trapdoors", true);
        allowKnockingFenceGates = getBoolean("allow-knocking-fence-gates", false);
        requirePermissionToKnockFenceGates = getBoolean("require-permission-to-knock-fence-gates", false);

        soundKnockIron = getString("sound-knock-iron", "minecraft:entity.zombie.attack_iron_door");
        soundKnockWood = getString("sound-knock-wood", "minecraft:item.shield.block");
        soundKnockCopper = getString("sound-knock-copper", "minecraft:block.copper.step");
        soundKnockVolume = getDouble("sound-knock-volume", 1.0);
        soundKnockPitch = getDouble("sound-knock-pitch", 1.0);

        allowDoubleWoodenDoors = getBoolean("allow-double-wooden-doors", true);
        allowDoubleIronDoors = getBoolean("allow-double-iron-doors", true);
        allowDoubleCopperDoors = getBoolean("allow-double-copper-doors", true);
        allowDoubleFenceGates = getBoolean("allow-double-fence-gates", false);
        checkForRedstone = getBoolean("check-for-redstone", true);
        requirePermissionForDoubleDoors = getBoolean("require-permission-for-double-doors", false);
        requirePermissionForDoubleFenceGates = getBoolean("require-permission-for-double-fence-gates", false);
    }
}
