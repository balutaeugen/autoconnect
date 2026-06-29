#!/usr/bin/env python3
import argparse
import os
import re
import shutil
import subprocess
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CHANGES = []

URLS = {
    "fabric_loader": "https://maven.fabricmc.net/net/fabricmc/fabric-loader/maven-metadata.xml",
    "fabric_loom": "https://maven.fabricmc.net/net/fabricmc/fabric-loom/net.fabricmc.fabric-loom.gradle.plugin/maven-metadata.xml",
    "sponge_mixin": "https://maven.fabricmc.net/net/fabricmc/sponge-mixin/maven-metadata.xml",
    "mod_menu": "https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/maven-metadata.xml",
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


def update_regex(relative_path, pattern, replacement, label, dry_run):
    text = read_text(relative_path)
    match = re.search(pattern, text)
    if not match:
        raise RuntimeError(f"Could not find {label} in {relative_path}")

    new_value = replacement(match)
    updated = re.sub(pattern, lambda item: replacement(item), text, count=1)
    if updated != text:
        old_value = match.group(match.lastindex or 0)
        CHANGES.append(f"{relative_path}: {label} {old_value} -> {new_value}")
        write_text(relative_path, updated, dry_run)


def update_quoted_version(relative_path, prefix_pattern, new_version, label, dry_run, index=0):
    text = read_text(relative_path)
    pattern = re.compile(f"({prefix_pattern})\"([^\"]+)\"")
    matches = list(pattern.finditer(text))
    if len(matches) <= index:
        raise RuntimeError(f"Could not find {label} occurrence {index} in {relative_path}")

    match = matches[index]
    old_version = match.group(2)
    if old_version == new_version:
        return

    new_value = f'{match.group(1)}"{new_version}"'
    updated = text[:match.start()] + new_value + text[match.end():]
    CHANGES.append(f"{relative_path}: {label} {old_version} -> {new_version}")
    write_text(relative_path, updated, dry_run)


def update_dependency_pair(relative_path, version_key, dependency_key, new_version, label, dry_run, index=0):
    update_quoted_version(relative_path, rf"\s+{version_key}\s+:\s+", new_version, label, dry_run, index)
    update_quoted_version(relative_path, rf"\s+{dependency_key}\s*:\s+", f">={new_version}", f"{label} dependency", dry_run, index)


def update_gradle_plugin_range(relative_paths, plugin_id, new_version, dry_run):
    escaped = re.escape(plugin_id)
    pattern = rf"(id '{escaped}' version ')\[[^,]+,([^)]+)\)'"
    for relative_path in relative_paths:
        update_regex(
            relative_path,
            pattern,
            lambda match: f"{match.group(1)}[{new_version},{match.group(2)})'",
            f"{plugin_id} minimum",
            dry_run,
        )


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


def main():
    parser = argparse.ArgumentParser(description="Update tracked Minecraft mod dependency versions.")
    parser.add_argument("--dry-run", action="store_true", help="Print proposed updates without changing files.")
    parser.add_argument("--skip-gradle-check", action="store_true", help="Skip Gradle configuration verification.")
    args = parser.parse_args()

    print("Checking dependency metadata...")

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

    mod_menu_18 = latest_matching(URLS["mod_menu"], lambda version: version.startswith("18."))
    mod_menu_20 = latest_matching(URLS["mod_menu"], lambda version: version.startswith("20."))
    cloth_261 = latest_matching(URLS["cloth_config"], lambda version: version.startswith("26.1."))
    cloth_262 = latest_matching(URLS["cloth_config"], lambda version: version.startswith("26.2."))

    dry_run = args.dry_run

    update_regex("gradle.properties", r"(loader_version=)[^\r\n]+", lambda match: f"{match.group(1)}{fabric_loader}", "Fabric loader", dry_run)

    update_regex("settings.gradle", r"(id 'dev\.kikugie\.stonecutter' version ')[^']+'", lambda match: f"{match.group(1)}{stonecutter}'", "Stonecutter", dry_run)
    update_regex("settings.gradle", r"(id 'org\.gradle\.toolchains\.foojay-resolver-convention' version ')[^']+'", lambda match: f"{match.group(1)}{foojay_resolver}'", "Foojay resolver", dry_run)

    update_regex("fabric/fabric.gradle", r"(classpath 'net\.fabricmc:fabric-loom:)[^']+'", lambda match: f"{match.group(1)}{fabric_loom}'", "Fabric Loom", dry_run)
    update_dependency_pair("fabric/fabric.gradle", "modMenu", "modMenuDependency", mod_menu_18, "Fabric 26.1.x Mod Menu", dry_run, 0)
    update_dependency_pair("fabric/fabric.gradle", "modMenu", "modMenuDependency", mod_menu_20, "Fabric 26.2 Mod Menu", dry_run, 1)
    update_dependency_pair("fabric/fabric.gradle", "clothConfig", "clothConfigDependency", cloth_261, "Fabric 26.1.x Cloth Config", dry_run, 0)
    update_dependency_pair("fabric/fabric.gradle", "clothConfig", "clothConfigDependency", cloth_262, "Fabric 26.2 Cloth Config", dry_run, 1)
    update_quoted_version("fabric/fabric.gradle", r"    def mixinVersion = ", sponge_mixin, "Fabric Sponge Mixin", dry_run)
    update_quoted_version("fabric/fabric.gradle", r"    def asmVersion = ", asm, "Fabric ASM", dry_run)

    update_quoted_version("forge/forge.gradle", r"\s+forgeVersion\s+:\s+", forge_2612_loader, "Forge 26.1.2", dry_run, 0)
    update_quoted_version("forge/forge.gradle", r"\s+forgeVersion\s+:\s+", forge_262_loader, "Forge 26.2", dry_run, 1)
    update_gradle_plugin_range(["forge/26.1.2/build.gradle", "forge/26.2/build.gradle"], "net.minecraftforge.gradle", forge_gradle, dry_run)

    update_quoted_version("neoforge/neoforge.gradle", r"\s+neoForgeVersion\s+:\s+", neoforge_2612, "NeoForge 26.1.2", dry_run, 0)
    update_quoted_version("neoforge/neoforge.gradle", r"\s+neoForgeVersion\s+:\s+", neoforge_262, "NeoForge 26.2", dry_run, 1)
    update_quoted_version("neoforge/neoforge.gradle", r"\s+neoForgeRange\s+:\s+", f"[{neoforge_2612},)", "NeoForge 26.1.2 range", dry_run, 0)
    update_quoted_version("neoforge/neoforge.gradle", r"\s+neoForgeRange\s+:\s+", f"[{neoforge_262},)", "NeoForge 26.2 range", dry_run, 1)
    update_regex("neoforge/26.1.2/build.gradle", r"(id 'net\.neoforged\.moddev' version ')[^']+'", lambda match: f"{match.group(1)}{neoforge_moddev}'", "NeoForge ModDev", dry_run)
    update_regex("neoforge/26.2/build.gradle", r"(id 'net\.neoforged\.moddev' version ')[^']+'", lambda match: f"{match.group(1)}{neoforge_moddev}'", "NeoForge ModDev", dry_run)

    update_dependency_pair("quilt/quilt.gradle", "modMenu", "modMenuDependency", mod_menu_18, "Quilt 26.1.x Mod Menu", dry_run, 0)
    update_dependency_pair("quilt/quilt.gradle", "modMenu", "modMenuDependency", mod_menu_20, "Quilt 26.2 Mod Menu", dry_run, 1)
    update_quoted_version("quilt/quilt.gradle", r"def quiltLoaderVersion = ", quilt_loader, "Quilt loader", dry_run)
    update_quoted_version("quilt/quilt.gradle", r"def fabricLoaderDependency = ", fabric_loader, "Quilt Fabric loader dependency", dry_run)
    update_quoted_version("quilt/quilt.gradle", r"def quiltJson5Version = ", quilt_json5, "Quilt JSON5", dry_run)
    update_quoted_version("quilt/quilt.gradle", r"def quiltConfigVersion = ", quilt_config, "Quilt Config", dry_run)
    update_quoted_version("quilt/quilt.gradle", r"    def mixinVersion = ", sponge_mixin, "Quilt Sponge Mixin", dry_run)
    update_quoted_version("quilt/quilt.gradle", r"    def asmVersion = ", asm, "Quilt ASM", dry_run)
    update_regex("quilt/26.1.2/build.gradle", r"(id 'org\.quiltmc\.loom' version ')[^']+'", lambda match: f"{match.group(1)}{quilt_loom}'", "Quilt Loom", dry_run)
    update_regex("quilt/26.2/build.gradle", r"(id 'org\.quiltmc\.loom' version ')[^']+'", lambda match: f"{match.group(1)}{quilt_loom}'", "Quilt Loom", dry_run)

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
