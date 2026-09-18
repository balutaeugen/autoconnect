# Adding a Minecraft Version

AutoConnect supports Fabric. The release matrix lives in `gradle/autoconnect-metadata.json`, and Stonecutter generates Fabric projects from that metadata.

## 1. Update the metadata matrix

Add the new Minecraft target under `loaders.fabric.targets`.

Each target needs `minecraftDependency`, `minecraftLabel`, `minecraftVersions`, `modMenu`, `modMenuArtifact`, `modMenuDependency`, `clothConfig`, and `clothConfigDependency`. Verify that the dependency versions support the new Minecraft release.

Use `minecraftLabel` for the artifact suffix. If one build supports a range, keep the key on the compiled version and put the full supported list in `minecraftVersions`.

Fabric projects are created from `settings.gradle`, so no `versions/` directory should be committed manually. Update the active target in `stonecutter.gradle.kts` and the Minecraft defaults in `gradle.properties` when changing the development version.

## 2. Refresh generated README content

```sh
gradle updateReadmeGeneratedContent
```

This updates the README compatibility list and artifact examples from the metadata. Also update the compatibility sections in `MODRINTH.md` and `CURSEFORGE.md` whenever supported Minecraft versions change; those descriptions are maintained manually.

## 3. Build and inspect the matrix

```sh
gradle printMinecraftVersionMatrix
gradle checkReadmeGeneratedContent checkAllMinecraftVersions :unit-tests:test
gradle preparePublishArtifacts
# Set VERSION to the mod_version from gradle.properties before running:
python .github/actions/publish/validate_publish_artifacts.py
```

Launch the new target to check the client and configuration screen:

```sh
gradle :fabric-<minecraft-version>:buildJarAndRunClient
```

## 4. Check publish metadata

The `Publish Dry Run` GitHub Actions workflow builds the upload jars and validates Modrinth and CurseForge metadata without using tokens or uploading files.

Run it before the real `Publish` workflow when a Minecraft target, loader version, Java version, platform project id, or release artifact naming changes.
