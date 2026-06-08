package xyz.redking.commanddeck.platform;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Platform {
    private static Path configDir;

    private Platform() {
    }

    public static Path getConfigDir() {
        if (configDir != null) {
            return configDir;
        }

        configDir = detectConfigDir();
        return configDir;
    }

    private static Path detectConfigDir() {
        Path fabricConfigDir = detectFabricConfigDir();
        if (fabricConfigDir != null) {
            return fabricConfigDir;
        }

        Path quiltConfigDir = detectQuiltConfigDir();
        if (quiltConfigDir != null) {
            return quiltConfigDir;
        }

        Path forgeConfigDir = detectForgeConfigDir();
        if (forgeConfigDir != null) {
            return forgeConfigDir;
        }

        Path neoForgeConfigDir = detectNeoForgeConfigDir();
        if (neoForgeConfigDir != null) {
            return neoForgeConfigDir;
        }

        return Paths.get("config");
    }

    private static Path detectFabricConfigDir() {
        try {
            Class<?> loaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = loaderClass.getMethod("getInstance").invoke(null);
            return (Path) loaderClass.getMethod("getConfigDir").invoke(loader);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Path detectQuiltConfigDir() {
        try {
            Class<?> loaderClass = Class.forName("org.quiltmc.loader.api.QuiltLoader");
            return (Path) loaderClass.getMethod("getConfigDir").invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Path detectNeoForgeConfigDir() {
        try {
            Class<?> pathsClass = Class.forName("net.neoforged.fml.loading.FMLPaths");
            Object configDirEnum = pathsClass.getField("CONFIGDIR").get(null);
            Method getMethod = pathsClass.getMethod("get");
            return (Path) getMethod.invoke(configDirEnum);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Path detectForgeConfigDir() {
        try {
            Class<?> pathsClass = Class.forName("net.minecraftforge.fml.loading.FMLPaths");
            Object configDirEnum = pathsClass.getField("CONFIGDIR").get(null);
            Method getMethod = pathsClass.getMethod("get");
            return (Path) getMethod.invoke(configDirEnum);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
