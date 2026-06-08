# CommanDeck Common Notes

This folder is the source of truth for behavior that should stay aligned across Fabric, NeoForge, Forge, and possible Quilt builds.

Keep these things common unless a Minecraft or loader API forces a difference:

* Action button JSON shape and migration expectations
* Action type names: `cmd`, `key`, `url`, `screen`, `delay`, `condition`, `sound`, `folder`
* UI concepts: deck grid, edit mode, search, folders, breadcrumbs, settings
* Default keybind behavior
* Asset names, language keys, icon usage, and user-facing wording
* README feature descriptions and release checklist wording

Loader-specific projects may need their own Java source because Minecraft client APIs differ between versions. For example, the Forge 1.20.1 port uses older `GuiGraphics` input signatures and integer NBT `CustomModelData`, while the current root build targets newer APIs.

When changing common behavior, update the active loader folders that carry code copies:

* Root project: current Fabric plus experimental NeoForge metadata
* `versions/forge/1.20.1`: Forge 1.20.1 requested port
