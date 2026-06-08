# CommanDeck Loader Versions

This directory holds loader-specific CommanDeck builds and notes.

## Current Layout

* `../` - root project; current Minecraft version, Fabric build tooling, and experimental NeoForge metadata/entrypoint.
* `fabric/` - notes for the Fabric version line.
* `neoforge/` - notes for the NeoForge version line.
* `forge/1.20.1/` - standalone Forge 1.20.1 port requested for Forge users.
* `quilt/` - placeholder notes for Quilt, currently expected to follow Fabric unless Quilt requires a separate port.

## Rule Of Thumb

Put loader or Minecraft-version-specific code in the matching loader folder.

Put behavior that should remain the same everywhere in `../common/`, then copy or port that behavior into each active loader implementation as needed.
