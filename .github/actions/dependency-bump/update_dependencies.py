#!/usr/bin/env python3
import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
CHANGES = []

URLS = {
    "gradle": "https://services.gradle.org/versions/current",
    "fabric_loader": "https://maven.fabricmc.net/net/fabricmc/fabric-loader/maven-metadata.xml",
    "fabric_loom": "https://maven.fabricmc.net/net/fabricmc/fabric-loom/net.fabricmc.fabric-loom.gradle.plugin/maven-metadata.xml",
    "sponge_mixin": "https://maven.fabricmc.net/net/fabricmc/sponge-mixin/maven-metadata.xml",
    "mod_menu": "https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/maven-metadata.xml",
    "mod_menu_modrinth": "https://api.modrinth.com/v2/project/mOgUt4GM/version?include_changelog=false",
    "cloth_config": "https://maven.shedaniel.me/me/shedaniel/cloth/cloth-config-fabric/maven-metadata.xml",
    "forge": "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml",
    "forge_gradle": "https://plugins.gradle.org/m2/net/minecraftforge/gradle/net.minecraftforge.gradle.gradle.plugin/maven-metadata.xml",
    "neoforge": "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml",
    "neoforge_moddev": "https://maven.neoforged.net/releases/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml",
    "quilt_loader": "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-loader/maven-metadata.xml",
    "quilt_loom": "https://maven.quiltmc.org/repository/release/org/quiltmc/loom/org.quiltmc.loom.gradle.plugin/maven-metadata.xml",
    "quilt_config": "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-config/maven-metadata.xml",
    "quilt_json5": "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-json5/maven-metadata.xml",
    "asm": "https://repo1.maven.org/maven2/org/ow2/asm/asm/maven-metadata.xml",
    "stonecutter": "https://plugins.gradle.org/m2/dev/kikugie/stonecutter/dev.kikugie.stonecutter.gradle.plugin/maven-metadata.xml",
    "foojay_resolver": "https://plugins.gradle.org/m2/org/gradle/toolchains/foojay-resolver-convention/org.gradle.toolchains.foojay-resolver-convention.gradle.plugin/maven-metadata.xml",
}


def read_metadata(url):
    print(f"Reading {url}")
    request = urllib.request.Request(url, headers={"User-Agent": "AutoConnect dependency updater"})
    with urllib.request.urlopen(request, timeout=60) as response:
        return ET.fromstring(response.read())


def maven_versions(url):
    root = read_metadata(url)
    return [node.text for node in root.findall("./versioning/versions/version") if node.text]


def maven_release(url):
    root = read_metadata(url)
    release = root.findtext("./versioning/release") or root.findtext("./versioning/latest")
    if not release:
        raise RuntimeError(f"No release/latest version found in {url}")
    return release


def gradle_release(url):
    print(f"Reading {url}")
    request = urllib.request.Request(url, headers={"User-Agent": "AutoConnect dependency updater"})
    with urllib.request.urlopen(request, timeout=60) as response:
        version = json.load(response).get("version")
    if not version:
        raise RuntimeError(f"No Gradle version found in {url}")
    return version


def modrinth_versions(url):
    print(f"Reading {url}")
    request = urllib.request.Request(url, headers={"User-Agent": "AutoConnect dependency updater"})
    with urllib.request.urlopen(request, timeout=60) as response:
        versions = json.load(response)
    if not isinstance(versions, list):
        raise RuntimeError(f"No Modrinth versions found in {url}")
    return versions


def modrinth_release(versions, version_number):
    matches = [version for version in versions if version.get("version_number") == version_number]
    if not matches:
        raise RuntimeError(f"Mod Menu {version_number} has no Modrinth fallback artifact")
    return {"version_number": version_number, "id": matches[0]["id"]}


def version_sort_key(version):
    main = version
    qualifier_rank = 9
    qualifier_number = 0

    lower = version.lower()
    for name, rank in (("snapshot", 0), ("alpha", 1), ("beta", 2), ("rc", 3)):
        if name in lower:
            main = re.split(r"[-+]", version, maxsplit=1)[0]
            qualifier_rank = rank
            suffix_numbers = [int(item) for item in re.findall(r"\d+", version[len(main):])]
            qualifier_number = suffix_numbers[-1] if suffix_numbers else 0
            break

    numbers = [int(item) for item in re.findall(r"\d+", main)]
    numbers = (numbers + [0, 0, 0, 0, 0, 0])[:6]
    return (*numbers, qualifier_rank, qualifier_number, version)


def latest_matching(url, predicate):
    matches = [version for version in maven_versions(url) if predicate(version)]
    if not matches:
        raise RuntimeError(f"No matching versions found in {url}")
    return sorted(matches, key=version_sort_key)[-1]


def read_text(relative_path):
    return (ROOT / relative_path).read_text(encoding="utf-8")


def write_text(relative_path, text, dry_run):
    if not dry_run:
        (ROOT / relative_path).write_text(text, encoding="utf-8", newline="")


def read_project_metadata():
    return json.loads(read_text("gradle/autoconnect-metadata.json"))


def write_metadata(metadata, dry_run):
    text = json.dumps(metadata, indent=2) + "\n"
    write_text("gradle/autoconnect-metadata.json", text, dry_run)


def update_metadata_path(metadata, path, new_value, label):
    current = metadata
    for key in path[:-1]:
        current = current[key]

    old_value = current[path[-1]]
    if old_value == new_value:
        return

    current[path[-1]] = new_value
    CHANGES.append(f"gradle/autoconnect-metadata.json: {label} {old_value} -> {new_value}")


def update_metadata_dependency_pair(metadata, loader, minecraft_version, version_key, dependency_key, new_version, label):
    target = metadata["loaders"][loader]["targets"][minecraft_version]
    old_version = target[version_key]
    if old_version != new_version:
        target[version_key] = new_version
        CHANGES.append(f"gradle/autoconnect-metadata.json: {label} {old_version} -> {new_version}")

    old_dependency = target[dependency_key]
    new_dependency = f">={new_version}"
    if old_dependency != new_dependency:
        target[dependency_key] = new_dependency
        CHANGES.append(f"gradle/autoconnect-metadata.json: {label} dependency {old_dependency} -> {new_dependency}")


def update_mod_menu(metadata, loader, minecraft_version, release, label):
    update_metadata_dependency_pair(
        metadata,
        loader,
        minecraft_version,
        "modMenu",
        "modMenuDependency",
        release["version_number"],
        label,
    )
    update_metadata_path(
        metadata,
        ["loaders", loader, "targets", minecraft_version, "modMenuArtifact"],
        release["id"],
        f"{label} Modrinth artifact",
    )


def update_regex(relative_path, pattern, replacement, label, dry_run):
    text = read_text(relative_path)
    match = re.search(pattern, text)
    if not match:
        raise RuntimeError(f"Could not find {label} in {relative_path}")

    new_value = replacement(match)
    updated = re.sub(pattern, lambda item: replacement(item), text, count=1)
    if updated != text:
        old_value = match.group(0)
        CHANGES.append(f"{relative_path}: {label} {old_value} -> {new_value}")
        write_text(relative_path, updated, dry_run)


def update_gradle_plugin_version(relative_path, plugin_id, new_version, label, dry_run):
    text = read_text(relative_path)
    lines = text.splitlines(keepends=True)
    prefix_pattern = re.compile(rf"^(\s*id\s+['\"]{re.escape(plugin_id)}['\"]\s+version\s+['\"])([^'\"]+)(['\"].*)$")

    for index, line in enumerate(lines):
        match = prefix_pattern.match(line.rstrip("\r\n"))
        if not match:
            continue

        old_version = match.group(2)
        updated_line = f"{match.group(1)}{new_version}{match.group(3)}"
        newline = "\r\n" if line.endswith("\r\n") else "\n" if line.endswith("\n") else ""
        if old_version != new_version:
            lines[index] = updated_line + newline
            CHANGES.append(f"{relative_path}: {label} {old_version} -> {new_version}")
            write_text(relative_path, "".join(lines), dry_run)
        return

    raise RuntimeError(f"Could not find Gradle plugin {plugin_id} in {relative_path}")


def update_gradle_plugin_minimum(relative_path, plugin_id, new_minimum, label, dry_run):
    text = read_text(relative_path)
    lines = text.splitlines(keepends=True)
    prefix_pattern = re.compile(
        rf"^(\s*id\s+['\"]{re.escape(plugin_id)}['\"]\s+version\s+['\"]\[)([^,]+)(,[^)]+\)['\"].*)$"
    )

    for index, line in enumerate(lines):
        match = prefix_pattern.match(line.rstrip("\r\n"))
        if not match:
            continue

        old_minimum = match.group(2)
        updated_line = f"{match.group(1)}{new_minimum}{match.group(3)}"
        newline = "\r\n" if line.endswith("\r\n") else "\n" if line.endswith("\n") else ""
        if old_minimum != new_minimum:
            lines[index] = updated_line + newline
            CHANGES.append(f"{relative_path}: {label} {old_minimum} -> {new_minimum}")
            write_text(relative_path, "".join(lines), dry_run)
        return

    raise RuntimeError(f"Could not find Gradle plugin range for {plugin_id} in {relative_path}")


def update_gradle_plugin_range(relative_paths, plugin_id, new_version, dry_run):
    for relative_path in relative_paths:
        update_gradle_plugin_minimum(relative_path, plugin_id, new_version, f"{plugin_id} minimum", dry_run)


def gradle_command():
    explicit = os.environ.get("GRADLE_CMD")
    if explicit:
        return explicit

    found = shutil.which("gradle")
    if found:
        return found

    local_gradle = Path.home() / ".gradle/wrapper/dists/gradle-9.5.0-bin/bvnork1r7n8i6kp5cnkibsc9q/gradle-9.5.0/bin/gradle.bat"
    if local_gradle.exists():
        return str(local_gradle)

    return None


def run_gradle_check():
    command = gradle_command()
    if not command:
        print("Gradle was not found; skipping verification.", file=sys.stderr)
        return

    subprocess.run([command, "help", "--no-daemon", "--console=plain"], cwd=ROOT, check=True)


def write_github_output(name, value):
    output_path = os.environ.get("GITHUB_OUTPUT")
    if output_path:
        with open(output_path, "a", encoding="utf-8") as output:
            output.write(f"{name}={value}\n")


def main():
    parser = argparse.ArgumentParser(description="Update tracked Minecraft mod dependency versions.")
    parser.add_argument("--dry-run", action="store_true", help="Print proposed updates without changing files.")
    parser.add_argument("--skip-gradle-check", action="store_true", help="Skip Gradle configuration verification.")
    args = parser.parse_args()

    print("Checking dependency metadata...")

    gradle = gradle_release(URLS["gradle"])
    fabric_loader = maven_release(URLS["fabric_loader"])
    fabric_loom = maven_release(URLS["fabric_loom"])
    sponge_mixin = maven_release(URLS["sponge_mixin"])
    asm = maven_release(URLS["asm"])
    stonecutter = maven_release(URLS["stonecutter"])
    foojay_resolver = maven_release(URLS["foojay_resolver"])
    forge_gradle = maven_release(URLS["forge_gradle"])
    neoforge_moddev = maven_release(URLS["neoforge_moddev"])
    quilt_loader = maven_release(URLS["quilt_loader"])
    quilt_loom = maven_release(URLS["quilt_loom"])
    quilt_config = maven_release(URLS["quilt_config"])
    quilt_json5 = maven_release(URLS["quilt_json5"])

    forge_2612 = latest_matching(URLS["forge"], lambda version: version.startswith("26.1.2-"))
    forge_262 = latest_matching(URLS["forge"], lambda version: version.startswith("26.2-"))
    forge_2612_loader = re.sub(r"^26\.1\.2-", "", forge_2612)
    forge_262_loader = re.sub(r"^26\.2-", "", forge_262)

    neoforge_2612 = latest_matching(URLS["neoforge"], lambda version: version.startswith("26.1.2."))
    neoforge_262 = latest_matching(URLS["neoforge"], lambda version: version.startswith("26.2."))

    mod_menu_18_version = latest_matching(URLS["mod_menu"], lambda version: version.startswith("18."))
    mod_menu_20_version = latest_matching(URLS["mod_menu"], lambda version: version.startswith("20."))
    mod_menu_modrinth_versions = modrinth_versions(URLS["mod_menu_modrinth"])
    mod_menu_18 = modrinth_release(mod_menu_modrinth_versions, mod_menu_18_version)
    mod_menu_20 = modrinth_release(mod_menu_modrinth_versions, mod_menu_20_version)
    cloth_261 = latest_matching(URLS["cloth_config"], lambda version: version.startswith("26.1."))
    cloth_262 = latest_matching(URLS["cloth_config"], lambda version: version.startswith("26.2."))

    dry_run = args.dry_run
    metadata = read_project_metadata()

    update_regex(
        "gradle/wrapper/gradle-wrapper.properties",
        r"(distributionUrl=https\\://services\.gradle\.org/distributions/gradle-)[^-]+(-bin\.zip)",
        lambda match: f"{match.group(1)}{gradle}{match.group(2)}",
        "Gradle",
        dry_run,
    )
    write_github_output("gradle_version", gradle)

    update_metadata_path(metadata, ["dependencyVersions", "fabricLoader"], fabric_loader, "Fabric loader")
    update_metadata_path(metadata, ["dependencyVersions", "spongeMixin"], sponge_mixin, "Sponge Mixin")
    update_metadata_path(metadata, ["dependencyVersions", "asm"], asm, "ASM")
    update_metadata_path(metadata, ["dependencyVersions", "quiltLoader"], quilt_loader, "Quilt loader")
    update_metadata_path(metadata, ["dependencyVersions", "quiltJson5"], quilt_json5, "Quilt JSON5")
    update_metadata_path(metadata, ["dependencyVersions", "quiltConfig"], quilt_config, "Quilt Config")

    update_gradle_plugin_version("settings.gradle", "dev.kikugie.stonecutter", stonecutter, "Stonecutter", dry_run)
    update_gradle_plugin_version("settings.gradle", "org.gradle.toolchains.foojay-resolver-convention", foojay_resolver, "Foojay resolver", dry_run)

    update_regex("fabric/fabric.gradle", r"(classpath 'net\.fabricmc:fabric-loom:)[^']+'", lambda match: f"{match.group(1)}{fabric_loom}'", "Fabric Loom", dry_run)
    update_mod_menu(metadata, "fabric", "26.1.2", mod_menu_18, "Fabric 26.1.x Mod Menu")
    update_mod_menu(metadata, "fabric", "26.2", mod_menu_20, "Fabric 26.2 Mod Menu")
    update_metadata_dependency_pair(metadata, "fabric", "26.1.2", "clothConfig", "clothConfigDependency", cloth_261, "Fabric 26.1.x Cloth Config")
    update_metadata_dependency_pair(metadata, "fabric", "26.2", "clothConfig", "clothConfigDependency", cloth_262, "Fabric 26.2 Cloth Config")

    update_metadata_path(metadata, ["loaders", "forge", "targets", "26.1.2", "forgeVersion"], forge_2612_loader, "Forge 26.1.2")
    update_metadata_path(metadata, ["loaders", "forge", "targets", "26.2", "forgeVersion"], forge_262_loader, "Forge 26.2")
    update_gradle_plugin_range(["forge/26.1.2/build.gradle", "forge/26.2/build.gradle"], "net.minecraftforge.gradle", forge_gradle, dry_run)

    update_metadata_path(metadata, ["loaders", "neoforge", "targets", "26.1.2", "neoForgeVersion"], neoforge_2612, "NeoForge 26.1.2")
    update_metadata_path(metadata, ["loaders", "neoforge", "targets", "26.2", "neoForgeVersion"], neoforge_262, "NeoForge 26.2")
    update_metadata_path(metadata, ["loaders", "neoforge", "targets", "26.1.2", "neoForgeRange"], f"[{neoforge_2612},)", "NeoForge 26.1.2 range")
    update_metadata_path(metadata, ["loaders", "neoforge", "targets", "26.2", "neoForgeRange"], f"[{neoforge_262},)", "NeoForge 26.2 range")
    update_gradle_plugin_version("neoforge/26.1.2/build.gradle", "net.neoforged.moddev", neoforge_moddev, "NeoForge ModDev", dry_run)
    update_gradle_plugin_version("neoforge/26.2/build.gradle", "net.neoforged.moddev", neoforge_moddev, "NeoForge ModDev", dry_run)

    update_mod_menu(metadata, "quilt", "26.1.2", mod_menu_18, "Quilt 26.1.x Mod Menu")
    update_mod_menu(metadata, "quilt", "26.2", mod_menu_20, "Quilt 26.2 Mod Menu")
    update_metadata_dependency_pair(metadata, "quilt", "26.1.2", "clothConfig", "clothConfigDependency", cloth_261, "Quilt 26.1.x Cloth Config")
    update_metadata_dependency_pair(metadata, "quilt", "26.2", "clothConfig", "clothConfigDependency", cloth_262, "Quilt 26.2 Cloth Config")
    update_gradle_plugin_version("quilt/26.1.2/build.gradle", "org.quiltmc.loom", quilt_loom, "Quilt Loom", dry_run)
    update_gradle_plugin_version("quilt/26.2/build.gradle", "org.quiltmc.loom", quilt_loom, "Quilt Loom", dry_run)

    write_metadata(metadata, dry_run)

    if CHANGES:
        print("\nPlanned dependency updates:")
        for change in CHANGES:
            print(f" - {change}")
    else:
        print("All tracked dependency versions are already current.")

    if dry_run:
        print("\nDry run only; no files were changed.")
        return

    if not args.skip_gradle_check:
        run_gradle_check()


if __name__ == "__main__":
    main()
