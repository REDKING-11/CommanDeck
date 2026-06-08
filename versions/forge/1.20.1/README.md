# CommanDeck 1.2 for Forge 1.20.1

**CommanDeck** is a lightweight client mod for Minecraft that gives you an in-game deck for running commands, chat messages, keybind actions, and small action chains.

This folder builds a **Minecraft 1.20.1 Forge** jar. It targets Forge **47.4.10+** and Java **17**.

## Why CommanDeck?

CommanDeck is meant to be a cleaner, modern continuation of the QuickMenu idea with fewer required dependencies and better support for current Minecraft versions.

Compared to the older QuickMenu projects:

* CommanDeck is written in plain Java.
* CommanDeck does not require Fabric API.
* CommanDeck does not require Fabric Language Kotlin.
* CommanDeck does not require owo-lib or Cloth Config.
* You only need Minecraft Forge on the client.

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

## Usage

1. Press `G` to open CommanDeck.
2. Click the pencil icon, or press `E`, to open edit tools.
3. Use `+ Action` to create a new button.
4. Use `Settings` to change layout and behavior.
5. Use `Ctrl + F` to search.
6. Use Backspace to leave folders.

## Build

The built Forge jar is written to this folder's `build/libs/`.

```powershell
.\gradlew.bat build
```

If Gradle is not already running on Java 17, set `JAVA_HOME` to a JDK 17 before building.

## Dependencies

Required:

* Minecraft 1.20.1
* Minecraft Forge 47.4.10 or newer
* Java 17

No additional client libraries are required.

## License

CommanDeck is licensed under the **GNU General Public License v3.0**.

Inspired by the GPL-3.0 QuickMenu projects:

* [ImCodist/quick-menu](https://github.com/ImCodist/quick-menu)
* [tenkun0317/quick-menu](https://github.com/tenkun0317/quick-menu)

Original rights belong to their respective authors. CommanDeck changes and additions are copyright REDKING.
