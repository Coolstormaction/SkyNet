
package com.skynet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GeminiKeyStore {

    private static final String KEY_NAME = "apiKey";

    private GeminiKeyStore() {
    }

    private static Path getConfigPath() {
        return Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("config")
                .resolve("skynet.json");
    }

    public static String getApiKey() {
        Path path = getConfigPath();

        // Debug: print the path, never the API key.
        System.out.println(
                "SkyNet: Looking for API key at "
                        + path.toAbsolutePath()
        );

        if (!Files.exists(path)) {
            System.out.println(
                    "SkyNet: Config file does not exist yet."
            );
            return "";
        }

        try {
            String json = Files.readString(
                    path,
                    StandardCharsets.UTF_8
            );

            JsonObject config = JsonParser.parseString(json)
                    .getAsJsonObject();

            if (!config.has(KEY_NAME)
                    || config.get(KEY_NAME).isJsonNull()) {
                System.out.println(
                        "SkyNet: API key is missing from config."
                );
                return "";
            }

            String key = config.get(KEY_NAME)
                    .getAsString()
                    .trim();

            System.out.println(
                    key.isBlank()
                            ? "SkyNet: Stored API key is empty."
                            : "SkyNet: API key loaded successfully."
            );

            return key;

        } catch (Exception exception) {
            System.err.println(
                    "SkyNet: Could not read API configuration: "
                            + exception.getClass().getSimpleName()
            );
            return "";
        }
    }

    public static void saveApiKey(String apiKey) throws IOException {
        String cleanedKey = apiKey == null ? "" : apiKey.trim();

        if (cleanedKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "API key cannot be empty."
            );
        }

        Path path = getConfigPath();

        Files.createDirectories(path.getParent());

        JsonObject config = new JsonObject();
        config.addProperty(KEY_NAME, cleanedKey);

        Files.writeString(
                path,
                config.toString(),
                StandardCharsets.UTF_8
        );

        // Debug: confirm the destination without exposing the key.
        System.out.println(
                "SkyNet: API key saved to "
                        + path.toAbsolutePath()
        );
    }

    public static boolean hasApiKey() {
        return !getApiKey().isBlank();
    }

    public static void deleteApiKey() throws IOException {
        Files.deleteIfExists(getConfigPath());

        System.out.println(
                "SkyNet: Config file deleted."
        );
    }
}
