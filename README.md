# ☁️ SkyNet — Your Hypixel SkyBlock AI Companion

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=for-the-badge&logo=minecraft&logoColor=white" />
  <img src="https://img.shields.io/badge/Fabric-Mod-DBB06A?style=for-the-badge" />
  <img src="https://img.shields.io/badge/AI-Powered-8A63D2?style=for-the-badge" />
  <img src="https://img.shields.io/badge/BYOK-Bring%20Your%20Own%20Key-4C9AFF?style=for-the-badge" />
</p>

<p align="center">
  <i>Less tab-switching. Less confusion. More SkyBlock.</i>
</p>

---

## 💡 What is SkyNet?

We all know Hypixel SkyBlock players can be lazy. And honestly, who can blame them?

Hypixel SkyBlock throws an absurd amount of content at you. New items, complicated progression systems, unfamiliar mechanics, hundreds of different resources, and countless things to learn.

For early-game players, even figuring out what to do next can feel overwhelming. Mid-game players aren't immune either, and returning players often come back after months or years only to find an entirely different game waiting for them.

The result? Endless alt-tabbing between Minecraft, the Hypixel SkyBlock Wiki, guides, forums, and Google searches. Sometimes, there's so much to figure out that you just give up.

**SkyNet aims to fix exactly that.**

SkyNet brings an AI-powered chatbot directly into your Minecraft client, letting you ask questions about Hypixel SkyBlock without constantly leaving the game.

Whether you're trying to understand a mechanic, figure out an item, learn about progression, or simply get an explanation of something the game never bothered to explain properly, SkyNet is designed to make finding answers easier.

**Your game stays open. Your questions get answered. Your adventure continues.**

## ✨ Features

- 💬 **In-Game AI Chatbot** — Ask questions and get answers without leaving Minecraft.
- ☁️ **Hypixel SkyBlock Focused** — Built around the questions, mechanics, and progression that matter to SkyBlock players.
- ⚡ **Quick Access** — Open SkyNet directly through an in-game command.
- 🔑 **Bring Your Own Key (BYOK)** — Use your own Gemini API key instead of relying on a shared API key.
- 🧭 **Beginner-Friendly** — Make the overwhelming amount of SkyBlock content easier to understand, especially for newer and returning players.

## 🔑 Bring Your Own Key (BYOK)

SkyNet uses a **Bring Your Own Key** model. You provide your own Gemini API key to power the chatbot.

This means you control your own API access rather than depending on a shared key provided by the project.

- Create or obtain your own Gemini API key.
- Run `/setapikey` in-game to open the API key setup screen.
- Save your key locally and use `/skn` to open SkyNet.

Your API usage is subject to Google's applicable API terms, quotas, and pricing. Keep your API key private and never commit it to GitHub.


# 🚀 GET STARTED — COMPLETE INSTALLATION GUIDE

New to SkyNet? Follow these steps to get your own AI-powered Hypixel SkyBlock assistant running in minutes!

> [!WARNING]
> **⚠️ MANUAL INSTALLATION ONLY — NOT AVAILABLE ON MODRINTH OR OTHER MOD DISTRIBUTION PLATFORMS**
>
> SkyNet is currently distributed exclusively through GitHub Releases. You must download and install the mod manually.

---

## 📦 STEP 1 — DOWNLOAD SKYNET

1. Scroll down to the **Assets** section at the bottom of this release.
2. Download **`skynet-1.0.0.jar`**.
3. **Do not download `skynet-1.0.0-sources.jar`** — that file contains source code, not the mod you need to install.

## 🛠️ STEP 2 — INSTALL THE MOD

Make sure you have the following installed:

- **Minecraft:** `26.1.2`
- **Fabric Loader:** `0.19.5`
- **Fabric API:** `0.155.3+26.1.2`

1. Open your Minecraft game directory.
2. Find the `mods` folder. If it doesn't exist, create it.
3. Move `skynet-1.0.0.jar` into the `mods` folder.
4. Launch Minecraft using your compatible Fabric installation.

## 🔑 STEP 3 — GET YOUR GEMINI API KEY

SkyNet uses a **Bring Your Own Key (BYOK)** system. You provide your own Gemini API key to power the chatbot.

1. Visit the official Google AI Studio API key page:

   **[👉 Generate your Gemini API key here](https://aistudio.google.com/api-keys)**

2. Sign in to your Google account if prompted.
3. Follow the instructions to create an API key.
4. Copy your key and keep it private.

> [!CAUTION]
> **Never share your API key or upload it to GitHub.** Treat it like a password. Your API usage is subject to Google's applicable terms, quotas, and pricing.

## ⌨️ STEP 4 — CONFIGURE SKYNET IN-GAME

**IMPORTANT: Run `/setapikey` FIRST, before `/skn`.**

1. Launch Minecraft and open the in-game chat.
2. Enter the following command:

   ```text
   /setapikey
   ```

3. Press Enter to open the SkyNet API key setup screen.
4. Paste your Gemini API key into the input field.
5. Save your key.

SkyNet stores your API key locally in its configuration file. Never share that file with anyone.

## ☁️ STEP 5 — OPEN SKYNET!

Now that your API key is configured, open the in-game chat and run:

```text
/skn
```

The SkyNet chat interface should open!

Ask questions about Hypixel SkyBlock, get help understanding unfamiliar mechanics, and spend less time switching between Minecraft and the Wiki.

---

## ❓ TROUBLESHOOTING

<details>
<summary><strong>❌ SkyNet says no API key is configured</strong></summary>

Run `/setapikey` first and save a valid API key before opening SkyNet with `/skn`.

</details>

<details>
<summary><strong>⚠️ The chatbot cannot get a response</strong></summary>

- Check your internet connection.
- Verify that your Gemini API key is valid.
- Check whether your Google AI Studio project has available API quota.
- Check Google's current API availability and usage limits.

</details>

<details>
<summary><strong>🛠️ The mod doesn't load</strong></summary>

- Verify your Minecraft version.
- Verify your Fabric Loader and Fabric API installations.
- Make sure you're launching the correct Minecraft profile.
- Ensure you downloaded `skynet-1.0.0.jar`, not the sources JAR.

</details>

<details>
<summary><strong>🐛 Still having problems?</strong></summary>

Open an issue in the [SkyNet GitHub repository](https://github.com/Coolstormaction/SkyNet) and include a description of the problem and any relevant error messages.

**Never include your API key in an issue, screenshot, or publicly shared log.**

</details>

---

<p align="center">
  ☁️ <strong>You're all set!</strong>
  <br>
  Less tab-switching. Less confusion. More SkyBlock.
  <br>
  <em>Welcome to SkyNet.</em>
</p>


<p align="center">
  <b>Made for SkyBlock players who would rather play the game than spend half their session searching for answers.</b>
  <br/>
  ☁️ <i>Welcome to SkyNet.</i>
</p>
