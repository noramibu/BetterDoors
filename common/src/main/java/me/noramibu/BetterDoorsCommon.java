package me.noramibu;

import me.noramibu.config.Config;
import me.noramibu.platform.PermissionChecker;
import me.noramibu.platform.PermissionsBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class BetterDoorsCommon {
    public static final String MOD_ID = "betterdoors";
    public static final String CONFIG_FILE_NAME = "better-doors.toml";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private BetterDoorsCommon() {
    }

    public static void init(Path configDir, PermissionChecker permissionChecker) {
        PermissionsBridge.setChecker(permissionChecker);
        Config.load(configDir, CONFIG_FILE_NAME);
        LOGGER.info("Better Doors initialized.");
    }
}
