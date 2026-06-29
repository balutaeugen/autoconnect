from __future__ import annotations

import json
import os
import sys
from pathlib import Path
from typing import Any

from publish_common import artifact_path, display_name, load_matrix, platform_version


VALID_RELEASE_TYPES = {"release", "beta", "alpha"}


def require_text(value: Any, name: str) -> None:
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"{name} must be a non-empty string.")


def require_string_list(value: Any, name: str) -> None:
    if not isinstance(value, list) or not value:
        raise ValueError(f"{name} must be a non-empty list.")
    for item in value:
        require_text(item, name)


def validate_common_target(target: dict[str, Any], version: str) -> Path:
    file_path = artifact_path(target, version)
    if not file_path.is_file():
        raise ValueError(f"Missing publish artifact: {file_path}")
    require_text(target.get("loader"), "loader")
    require_text(target.get("minecraft_label"), "minecraft_label")
    require_string_list(target.get("minecraft_versions"), "minecraft_versions")
    return file_path


def modrinth_metadata(matrix: dict[str, Any], target: dict[str, Any], version: str, release_type: str, changelog: str) -> dict[str, Any]:
    return {
        "name": display_name(matrix, target, version),
        "version_number": platform_version(target, version),
        "changelog": changelog,
        "dependencies": [],
        "game_versions": target["minecraft_versions"],
        "version_type": release_type,
        "loaders": [target["loader"]],
        "featured": True,
        "project_id": matrix["modrinth_project_id"],
        "file_parts": ["file"],
        "primary_file": "file",
    }


def curseforge_metadata(matrix: dict[str, Any], target: dict[str, Any], version: str, release_type: str, changelog: str) -> dict[str, Any]:
    return {
        "changelog": changelog,
        "changelogType": "markdown",
        "displayName": display_name(matrix, target, version),
        "gameVersions": [
            *target["minecraft_versions"],
            target["loader"],
            f"Java {matrix['java_version']}",
            matrix["environment"],
        ],
        "releaseType": release_type,
    }


def validate_modrinth(metadata: dict[str, Any], release_type: str) -> None:
    require_text(metadata.get("name"), "Modrinth name")
    require_text(metadata.get("version_number"), "Modrinth version_number")
    require_text(metadata.get("changelog"), "Modrinth changelog")
    require_string_list(metadata.get("game_versions"), "Modrinth game_versions")
    require_string_list(metadata.get("loaders"), "Modrinth loaders")
    require_text(metadata.get("project_id"), "Modrinth project_id")
    if metadata.get("version_type") != release_type:
        raise ValueError("Modrinth version_type does not match RELEASE_TYPE.")
    if metadata.get("file_parts") != ["file"] or metadata.get("primary_file") != "file":
        raise ValueError("Modrinth file metadata must reference the primary file part.")
    json.dumps(metadata, separators=(",", ":"))


def validate_curseforge(metadata: dict[str, Any], release_type: str) -> None:
    require_text(metadata.get("changelog"), "CurseForge changelog")
    require_text(metadata.get("displayName"), "CurseForge displayName")
    require_string_list(metadata.get("gameVersions"), "CurseForge gameVersions")
    if metadata.get("changelogType") != "markdown":
        raise ValueError("CurseForge changelogType must be markdown.")
    if metadata.get("releaseType") != release_type:
        raise ValueError("CurseForge releaseType does not match RELEASE_TYPE.")
    json.dumps(metadata, separators=(",", ":"))


def main() -> int:
    version = os.environ["VERSION"]
    release_type = os.environ.get("RELEASE_TYPE", "release")
    if release_type not in VALID_RELEASE_TYPES:
        raise ValueError(f"RELEASE_TYPE must be one of {sorted(VALID_RELEASE_TYPES)}.")

    changelog_path = Path("build/publish-changelog.md")
    changelog = changelog_path.read_text(encoding="utf-8")
    require_text(changelog, str(changelog_path))

    matrix = load_matrix()
    require_text(matrix.get("modrinth_project_id"), "modrinth_project_id")
    require_text(matrix.get("curseforge_project_id"), "curseforge_project_id")
    require_text(matrix.get("java_version"), "java_version")
    require_text(matrix.get("environment"), "environment")

    for target in matrix["targets"]:
        file_path = validate_common_target(target, version)

        modrinth = modrinth_metadata(matrix, target, version, release_type, changelog)
        validate_modrinth(modrinth, release_type)

        curseforge = curseforge_metadata(matrix, target, version, release_type, changelog)
        validate_curseforge(curseforge, release_type)

        print(f"Validated dry-run metadata for {file_path}")

    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except ValueError as error:
        print(error, file=sys.stderr)
        raise SystemExit(1)
