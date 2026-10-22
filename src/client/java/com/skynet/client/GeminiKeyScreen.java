
package com.skynet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.io.IOException;

public class GeminiKeyScreen extends Screen {

    private final Screen parent;
    private EditBox keyInput;
    private String status = "";

    public GeminiKeyScreen(Screen parent) {
        super(Component.literal("SkyNet API Key"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int fieldWidth = Math.min(300, this.width - 40);
        int fieldX = (this.width - fieldWidth) / 2;
        int centerY = this.height / 2;

        keyInput = new EditBox(
                this.font,
                fieldX,
                centerY - 12,
                fieldWidth,
                20,
                Component.literal("Gemini API key")
        );

        keyInput.setMaxLength(512);
        keyInput.setHint(Component.literal("Paste your Gemini API key"));

        addRenderableWidget(keyInput);

        addRenderableWidget(
                Button.builder(
                        Component.literal("Save"),
                        button -> saveKey()
                ).bounds(
                        this.width / 2 - 105,
                        centerY + 20,
                        100,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Cancel"),
                        button -> onClose()
                ).bounds(
                        this.width / 2 + 5,
                        centerY + 20,
                        100,
                        20
                ).build()
        );

        setFocused(keyInput);
    }

    private void saveKey() {
        String key = keyInput.getValue().trim();

        if (key.isEmpty()) {
            status = "Enter an API key first.";
            return;
        }

        try {
            GeminiKeyStore.saveApiKey(key);

            Minecraft.getInstance().setScreen(new SkyNetChatScreen());

        } catch (IOException | IllegalArgumentException exception) {
            status = "Couldn't save the key. Check file permissions.";
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        graphics.fill(0, 0, width, height, 0xFF0B0F19);

        String title = "SKYNET SETUP";
        String subtitle = "Enter your Gemini API key";
        String privacy = "Saved locally in your Minecraft config folder.";

        graphics.text(
                font,
                title,
                (width - font.width(title)) / 2,
                height / 2 - 75,
                0xFF38BDF8,
                false
        );

        graphics.text(
                font,
                subtitle,
                (width - font.width(subtitle)) / 2,
                height / 2 - 48,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                privacy,
                (width - font.width(privacy)) / 2,
                height / 2 + 52,
                0xFFB8C3D9,
                false
        );

        if (!status.isEmpty()) {
            graphics.text(
                    font,
                    status,
                    (width - font.width(status)) / 2,
                    height / 2 + 70,
                    0xFFFF7777,
                    false
            );
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
