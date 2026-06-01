# CommanDeck 1.1

**CommanDeck 1.1** is a lightweight client mod for Minecraft that gives you an in-game deck for running commands, chat messages, keybind actions, and small action chains.

It was inspired by **QuickMenu** and **QuickMenu Renewed**, then rebuilt and ported for newer Minecraft versions because the older projects were no longer maintained.

Version **1.1** adds experimental multi-loader support for **Fabric** and **NeoForge**. Quilt does not currently target this Minecraft version, but the jar is structured to stay Quilt-compatible if/when Quilt support catches up.

## Why CommanDeck?

CommanDeck is meant to be a cleaner, modern continuation of the idea with fewer required dependencies and better support for current Minecraft versions.

Compared to the older QuickMenu projects:

* The original project required extra libraries such as **owo-lib** and Fabric API.
* The renewed project still required Fabric API and other support libraries for its stack.
* CommanDeck is written in plain Java.
* CommanDeck does **not** require Fabric API.
* CommanDeck does **not** require Fabric Language Kotlin.
* CommanDeck does **not** require owo-lib or Cloth Config.
* You only need a supported mod loader to use it.
* Mod Menu support is optional, not required.

## Improvements

* Ported for newer Minecraft versions.
* Reduced required dependencies to the mod loader itself.
* Rebuilt on Minecraft's vanilla client UI system.
* Cleaner glass-style menu design.
* Edit tools are built into the main menu.
* Item icons persist through saving and loading.
* Buttons can run multiple actions in sequence.
* Action chains support delays and conditions.
* Actions can play sounds, open URLs, open screens, press keybinds, and run commands.
* Folders allow actions to be organized into nested decks.
* Breadcrumbs make folder navigation easier.
* Search can find actions recursively across folders.
* Configurable grid size and visible rows.
* Optional release-to-execute behavior for fast use.
* Optional Mod Menu integration without making Mod Menu required.

## Features

* Configurable button grid
* Command and chat-message actions
* Keybind actions
* Multi-action buttons
* Delay, condition, sound, URL, screen, and folder-open actions
* Item icons with `CustomModelData` support
* Folders, breadcrumbs, and recursive search
* Optional release-to-execute behavior
* In-game config screen
* Optional Mod Menu integration

## Usage

1. Press `G` to open CommanDeck.
2. Click the pencil icon, or press `E`, to open edit tools.
3. Use `+ Action` to create a new button.
4. Use `Settings` to change layout and behavior.
5. Use `Ctrl + F` to search.
6. Use Backspace to leave folders.

## Dependencies

Required:

* A supported client mod loader:
  * [Fabric Loader](https://fabricmc.net/)
  * Quilt-compatible loading
  * NeoForge support is experimental

Optional:

* [Mod Menu](https://modrinth.com/mod/modmenu)

Fabric API is not required.

## License

CommanDeck is licensed under the **GNU General Public License v3.0**.

Inspired by the GPL-3.0 QuickMenu projects:

* [ImCodist/quick-menu](https://github.com/ImCodist/quick-menu)
* [tenkun0317/quick-menu](https://github.com/tenkun0317/quick-menu)

Original rights belong to their respective authors. CommanDeck changes and additions are copyright REDKING.
