# CommanDeck Fabric Version

The active Fabric build currently lives in the repository root.

Build from the root directory:

```powershell
.\gradlew.bat build
```

Relevant files:

* `../../build.gradle`
* `../../gradle.properties`
* `../../src/main/resources/fabric.mod.json`
* `../../src/main/java/xyz/redking/commanddeck/platform/fabric/FabricEntrypoint.java`

If Fabric needs to diverge from the root project later, move or copy the root build into this folder and keep shared behavior documented in `../../common/`.
