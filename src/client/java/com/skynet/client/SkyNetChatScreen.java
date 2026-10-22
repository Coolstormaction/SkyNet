
package com.skynet.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SkyNetChatScreen extends Screen {

    private EditBox input;
    private Button sendButton;

    private static final String MODEL = "gemini-3.5-flash-lite";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final String SYSTEM_PROMPT = """
            You are SkyNet, an AI assistant specializing in Hypixel SkyBlock,
            the Minecraft MMORPG game on the Hypixel server.

            YOUR ROLE:
            - Help players understand Hypixel SkyBlock mechanics.
            - Assist with weapons, armor, accessories, pets, minions, collections, skills, farming, mining, combat, fishing, dungeons, slayers, and progression.
            - Recommend gear and strategies based on a player's stage of progression, budget, stats, and goals.
            - Explain crafting recipes, item requirements, and game systems when you have reliable information.
            - Help beginners learn the game without overwhelming them.

            ACCURACY:
            - Never invent item stats, prices, recipes, drop rates, requirements, or game mechanics.
            - Hypixel SkyBlock changes frequently. Your existing knowledge may be outdated.
            - If you are uncertain, explicitly say so.
            - Do not claim to have checked the live wiki or current prices unless a search or reliable source has actually been provided.
            - Distinguish known facts from estimates and recommendations.
            - If a question requires current information you do not have, tell the player that it needs verification.

            RECOMMENDATIONS:
            - Consider the player's budget and progression.
            - Explain why a recommendation is suitable.
            - Avoid recommending expensive equipment to beginners without explaining cheaper alternatives.
            - Ask follow-up questions when important details are missing.
            - Do not assume every player has access to endgame gear.

            RESPONSE STYLE:
            - Be friendly, helpful, and concise.
            - Use Hypixel SkyBlock terminology correctly.
            - Explain complicated mechanics in simple steps.
            - Use lists when they make an answer easier to follow.
            - Do not pretend to have access to the player's inventory, profile, stats, or live game state unless that data is explicitly supplied.
            - Markdown emphasis and lists are allowed.

            IDENTITY:
            - You are SkyNet, running inside a Minecraft client mod.
            - Your purpose is to help players enjoy and understand Hypixel SkyBlock.
            """;

    private static final List<ChatMessage> HISTORY = new ArrayList<>();

    private static boolean sending = false;
    private static long sessionGeneration = 0;

    private double scrollOffset = 0.0;
    private static final double SCROLL_SPEED = 36.0;

    private static final class ChatMessage {
        private final boolean fromUser;
        private final StringBuilder text;

        private ChatMessage(String text, boolean fromUser) {
            this.text = new StringBuilder(text);
            this.fromUser = fromUser;
        }

        private String text() {
            return text.toString();
        }
    }

    private record BubbleLayout(
            ChatMessage message,
            int x,
            int width,
            int height,
            List<FormattedCharSequence> lines
    ) {}

    public SkyNetChatScreen() {
        super(Component.literal("SkyNet"));
    }

    public static void clearHistory() {
        sessionGeneration++;
        HISTORY.clear();
        sending = false;
    }

    private static String decodeEntities(String text) {
        return text
                .replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&#x20;", " ")
                .replace("&#X20;", " ")
                .replace("&#39;", "'")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    private static Component parseMarkdownLine(String originalLine) {
        String line = originalLine;

        line = line.replaceFirst("^\\s{0,3}#{1,6}\\s+", "");
        line = line.replaceFirst("^(\\s*)[-*+]\\s+", "$1• ");
        line = line.replaceFirst("^(\\s*\\d+)\\.\\s+", "$1. ");
        line = line.replaceAll("`([^`]+)`", "$1");

        MutableComponent result = Component.empty();
        StringBuilder segment = new StringBuilder();

        boolean bold = false;
        boolean italic = false;
        int i = 0;

        while (i < line.length()) {
            boolean boldMarker =
                    line.startsWith("**", i) || line.startsWith("__", i);

            boolean italicMarker =
                    !boldMarker &&
                            (line.charAt(i) == '*' || line.charAt(i) == '_');

            if (boldMarker || italicMarker) {
                if (!segment.isEmpty()) {
                    result.append(
                            Component.literal(segment.toString())
                                    .setStyle(
                                            Style.EMPTY
                                                    .withBold(bold)
                                                    .withItalic(italic)
                                    )
                    );
                    segment.setLength(0);
                }

                if (boldMarker) {
                    bold = !bold;
                    i += 2;
                } else {
                    italic = !italic;
                    i++;
                }

                continue;
            }

            segment.append(line.charAt(i));
            i++;
        }

        if (!segment.isEmpty()) {
            result.append(
                    Component.literal(segment.toString())
                            .setStyle(
                                    Style.EMPTY
                                            .withBold(bold)
                                            .withItalic(italic)
                            )
            );
        }

        return result;
    }

    private static Component createDisplayComponent(String rawText) {
        String decoded = decodeEntities(rawText)
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        String[] originalLines = decoded.split("\n", -1);
        MutableComponent combined = Component.empty();

        int consecutiveBlankLines = 0;
        boolean firstLine = true;

        for (String originalLine : originalLines) {
            String line = originalLine;

            if (line.matches("^\\s*(?:-{3,}|\\*{3,}|_{3,})\\s*$")) {
                continue;
            }

            if (line.isBlank()) {
                consecutiveBlankLines++;

                if (consecutiveBlankLines > 2) {
                    continue;
                }
            } else {
                consecutiveBlankLines = 0;
            }

            if (!firstLine) {
                combined.append(Component.literal("\n"));
            }

            combined.append(parseMarkdownLine(line));
            firstLine = false;
        }

        return combined;
    }

    @Override
    protected void init() {
        super.init();

        int padding = 16;
        int inputHeight = 20;
        int buttonWidth = 42;
        int gap = 5;

        int inputX = padding;
        int inputY = this.height - padding - inputHeight - 18;
        int inputWidth = Math.max(
                40,
                this.width - (padding * 2) - buttonWidth - gap
        );

        input = new EditBox(
                this.font,
                inputX,
                inputY,
                inputWidth,
                inputHeight,
                Component.literal("Ask SkyNet...")
        );

        input.setMaxLength(2000);
        input.setEditable(!sending);
        addRenderableWidget(input);

        sendButton = Button.builder(
                Component.literal("Send"),
                button -> submitPrompt()
        ).bounds(
                inputX + inputWidth + gap,
                inputY,
                buttonWidth,
                inputHeight
        ).build();

        sendButton.active = !sending;
        addRenderableWidget(sendButton);

        if (!sending) {
            setFocused(input);
        }
    }

    private void submitPrompt() {
        String submitted = input.getValue().trim();

        if (submitted.isEmpty() || sending) {
            return;
        }

        HISTORY.add(new ChatMessage(submitted, true));
        scrollOffset = 0.0;

        ChatMessage assistantMessage = new ChatMessage("", false);
        HISTORY.add(assistantMessage);

        input.setValue("");

        sending = true;
        updateUIState();

        long requestGeneration = sessionGeneration;
        String requestBody = buildConversationRequest();

        callGeminiStream(
                requestBody,
                assistantMessage,
                requestGeneration
        );
    }

    private void updateUIState() {
        if (input != null) {
            input.setEditable(!sending);
        }

        if (sendButton != null) {
            sendButton.active = !sending;
        }
    }

    private String buildConversationRequest() {
        JsonObject root = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();

        systemPart.addProperty("text", SYSTEM_PROMPT);
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);

        root.add("system_instruction", systemInstruction);

        JsonArray contents = new JsonArray();

        for (ChatMessage message : HISTORY) {
            String messageText = message.text();

            if (messageText.isEmpty()) {
                continue;
            }

            JsonObject content = new JsonObject();
            content.addProperty(
                    "role",
                    message.fromUser ? "user" : "model"
            );

            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();

            part.addProperty("text", messageText);

            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
        }

        root.add("contents", contents);

        return root.toString();
    }

    private void callGeminiStream(
            String requestBody,
            ChatMessage targetMessage,
            long requestGeneration
    ) {
        CompletableFuture.runAsync(() -> {
            String apiKey = GeminiKeyStore.getApiKey();

            try {
                if (apiKey.isBlank()) {
                    throw new IllegalStateException(
                            "No Gemini API key configured. Run /skn first."
                    );
                }

                String url = String.format(
                        "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s",
                        MODEL,
                        apiKey
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        requestBody
                                )
                        )
                        .build();

                HttpResponse<InputStream> response = HTTP_CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofInputStream()
                );

                if (response.statusCode() < 200 ||
                        response.statusCode() >= 300) {

                    String errorBody;

                    try (InputStream body = response.body()) {
                        errorBody = new String(
                                body.readAllBytes(),
                                StandardCharsets.UTF_8
                        );
                    }

                    // Do not print the response body: it may contain
                    // details we do not want in the game logs.
                    System.err.println(
                            "SkyNet: Gemini returned HTTP " +
                                    response.statusCode()
                    );

                    throw new IllegalStateException(
                            "Gemini request failed (HTTP " +
                                    response.statusCode() + ")."
                    );
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.body(),
                                StandardCharsets.UTF_8
                        )
                )) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data:")) {
                            continue;
                        }

                        String jsonData = line.substring(5).trim();

                        if (jsonData.isEmpty() ||
                                jsonData.equals("[DONE]")) {
                            continue;
                        }

                        try {
                            JsonObject json = JsonParser
                                    .parseString(jsonData)
                                    .getAsJsonObject();

                            if (!json.has("candidates") ||
                                    json.getAsJsonArray("candidates").isEmpty()) {
                                continue;
                            }

                            JsonObject candidate = json
                                    .getAsJsonArray("candidates")
                                    .get(0)
                                    .getAsJsonObject();

                            if (!candidate.has("content")) {
                                continue;
                            }

                            JsonArray parts = candidate
                                    .getAsJsonObject("content")
                                    .getAsJsonArray("parts");

                            StringBuilder chunkText = new StringBuilder();

                            for (int i = 0; i < parts.size(); i++) {
                                JsonObject part = parts
                                        .get(i)
                                        .getAsJsonObject();

                                if (part.has("text")) {
                                    chunkText.append(
                                            part.get("text").getAsString()
                                    );
                                }
                            }

                            if (!chunkText.isEmpty()) {
                                String chunk = chunkText.toString();

                                Minecraft.getInstance().execute(() -> {
                                    if (sessionGeneration == requestGeneration
                                            && HISTORY.contains(targetMessage)) {
                                        targetMessage.text.append(chunk);
                                    }
                                });
                            }

                        } catch (RuntimeException ignoredMalformedChunk) {
                            // Ignore malformed or non-content SSE events.
                        }
                    }
                }

            } catch (Exception exception) {
                String detail = exception.getClass().getSimpleName();
                String message = exception.getMessage();

                if (message != null && !message.isBlank()) {
                    String safeMessage = message;

                    if (!apiKey.isBlank()) {
                        safeMessage = safeMessage.replace(
                                apiKey,
                                "[REDACTED_API_KEY]"
                        );
                    }

                    detail += ": " + safeMessage;
                }

                System.err.println("SkyNet request failed: " + detail);

                Minecraft.getInstance().execute(() -> {
                    if (sessionGeneration == requestGeneration
                            && HISTORY.contains(targetMessage)) {
                        targetMessage.text.setLength(0);
                        targetMessage.text.append(
                                "SkyNet couldn't get a response. " +
                                        "Check game logs for details."
                        );
                    }
                });

            } finally {
                Minecraft.getInstance().execute(() -> {
                    if (sessionGeneration == requestGeneration) {
                        sending = false;

                        if (Minecraft.getInstance().screen
                                instanceof SkyNetChatScreen currentScreen) {
                            currentScreen.updateUIState();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        int padding = 16;
        int headerHeight = 42;
        int inputHeight = 20;
        int inputY = this.height - padding - inputHeight - 18;

        int conversationTop = headerHeight + 12;
        int conversationBottom = inputY - 12;

        int conversationHeight = Math.max(
                0,
                conversationBottom - conversationTop
        );

        graphics.fill(0, 0, width, height, 0xFF0B0F19);
        graphics.fill(0, 0, width, headerHeight, 0xFF101522);

        graphics.text(
                font,
                "SKYNET",
                padding,
                8,
                0xFF38BDF8,
                false
        );

        graphics.text(
                font,
                "Hypixel SkyBlock AI",
                padding,
                24,
                0xFFB8C3D9,
                false
        );

        int maxBubbleWidth = Math.max(
                60,
                (width - (padding * 2)) / 2
        );

        List<BubbleLayout> layouts = createBubbleLayouts(maxBubbleWidth);

        int totalContentHeight = 0;

        for (BubbleLayout bubble : layouts) {
            totalContentHeight += bubble.height() + 8;
        }

        double maxScroll = Math.max(
                0,
                totalContentHeight - conversationHeight
        );

        scrollOffset = Math.max(
                0.0,
                Math.min(scrollOffset, maxScroll)
        );

        int y = (int) Math.round(
                conversationBottom - totalContentHeight + scrollOffset
        );

        graphics.enableScissor(
                padding,
                conversationTop,
                width - padding,
                conversationBottom
        );

        for (BubbleLayout bubble : layouts) {
            int bubbleHeight = bubble.height();

            int bubbleColor = bubble.message().fromUser
                    ? 0xFF164E63
                    : 0xFF202938;

            graphics.fill(
                    bubble.x(),
                    y,
                    bubble.x() + bubble.width(),
                    y + bubbleHeight,
                    bubbleColor
            );

            int lineY = y + 6;

            for (FormattedCharSequence line : bubble.lines()) {
                graphics.text(
                        font,
                        line,
                        bubble.x() + 8,
                        lineY,
                        0xFFFFFFFF,
                        false
                );

                lineY += font.lineHeight;
            }

            y += bubbleHeight + 8;
        }

        graphics.disableScissor();

        graphics.fill(
                0,
                inputY - 8,
                width,
                inputY - 7,
                0xFF263247
        );

        graphics.text(
                font,
                "ESC to close",
                padding,
                inputY + inputHeight + 5,
                0xFF8994A8,
                false
        );

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private List<BubbleLayout> createBubbleLayouts(int maxBubbleWidth) {
        List<BubbleLayout> layouts = new ArrayList<>();

        for (ChatMessage message : HISTORY) {
            String rawText = message.text();
            String displayText = rawText;

            if (rawText.isEmpty() && !message.fromUser && sending) {
                String[] dots = {"", ".", "..", "..."};

                displayText = "Generating" +
                        dots[(int) ((System.currentTimeMillis() / 250)
                                % dots.length)];
            }

            if (displayText.isEmpty()) {
                displayText = " ";
            }

            Component component = createDisplayComponent(displayText);

            int bubbleWidth = Math.min(
                    maxBubbleWidth,
                    Math.max(60, font.width(component) + 16)
            );

            int textWidth = Math.max(1, bubbleWidth - 16);

            List<FormattedCharSequence> lines = font.split(
                    component,
                    textWidth
            );

            if (lines.isEmpty()) {
                lines = font.split(Component.literal(" "), textWidth);
            }

            int bubbleHeight = Math.max(
                    24,
                    lines.size() * font.lineHeight + 12
            );

            // User messages are right-aligned.
            // Assistant messages are left-aligned.
            int bubbleX = message.fromUser
                    ? width - 16 - bubbleWidth
                    : 16;

            layouts.add(
                    new BubbleLayout(
                            message,
                            bubbleX,
                            bubbleWidth,
                            bubbleHeight,
                            lines
                    )
            );
        }

        return layouts;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        int padding = 16;
        int headerHeight = 42;
        int inputHeight = 20;

        int inputY = this.height - padding - inputHeight - 18;

        int conversationTop = headerHeight + 12;
        int conversationBottom = inputY - 12;

        int conversationHeight = Math.max(
                0,
                conversationBottom - conversationTop
        );

        int maxBubbleWidth = Math.max(
                60,
                (width - (padding * 2)) / 2
        );

        List<BubbleLayout> layouts = createBubbleLayouts(maxBubbleWidth);

        int totalContentHeight = 0;

        for (BubbleLayout bubble : layouts) {
            totalContentHeight += bubble.height() + 8;
        }

        double maxScroll = Math.max(
                0,
                totalContentHeight - conversationHeight
        );

        if (maxScroll <= 0) {
            scrollOffset = 0.0;

            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    horizontalAmount,
                    verticalAmount
            );
        }

        scrollOffset = Math.max(
                0.0,
                Math.min(
                        maxScroll,
                        scrollOffset - verticalAmount * SCROLL_SPEED
                )
        );

        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!sending &&
                (event.key() == GLFW.GLFW_KEY_ENTER ||
                        event.key() == GLFW.GLFW_KEY_KP_ENTER)) {
            submitPrompt();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
