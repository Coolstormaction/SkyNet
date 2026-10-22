
package com.skynet.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;

public final class SkyNetCommands {

    private SkyNetCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> {

                    // /skn opens chat if a key exists,
                    // otherwise it opens the API key setup screen.
                    dispatcher.register(
                            ClientCommands.literal("skn")
                                    .executes(context -> {
                                        Minecraft minecraft =
                                                Minecraft.getInstance();

                                        minecraft.execute(() -> {
                                            if (GeminiKeyStore.hasApiKey()) {
                                                minecraft.setScreen(
                                                        new SkyNetChatScreen()
                                                );
                                            } else {
                                                minecraft.setScreen(
                                                        new GeminiKeyScreen(
                                                                minecraft.screen
                                                        )
                                                );
                                            }
                                        });

                                        return 1;
                                    })
                    );

                    // /setapikey always opens the setup screen.
                    dispatcher.register(
                            ClientCommands.literal("setapikey")
                                    .executes(context -> {
                                        Minecraft minecraft =
                                                Minecraft.getInstance();

                                        minecraft.execute(() ->
                                                minecraft.setScreen(
                                                        new GeminiKeyScreen(
                                                                minecraft.screen
                                                        )
                                                )
                                        );

                                        return 1;
                                    })
                    );
                }
        );
    }
}
