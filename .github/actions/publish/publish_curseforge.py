from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

from publish_common import artifact_path, display_name, load_matrix, multipart_request, retry_upload

API_BASE = "https://minecraft.curseforge.com/api"


def request_json(path: str, token: str) -> object:
    request = urllib.request.Request(
        f"{API_BASE}{path}",
        headers={"X-Api-Token": token, "User-Agent": "AutoConnect publish workflow"},
    )
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.loads(response.read().decode("utf-8"))


def normalize(value: str) -> str:
    return value.casefold().replace(" ", "").replace("-", "").replace("_", "")


def names_for(value: str, java_version: str) -> list[str]:
    if value == java_version:
        return [value, f"Java {value}"]
    return [value]


def find_type_ids(version_types: list[dict[str, object]], slug_prefix: str) -> set[int]:
    return {
        int(item["id"])
        for item in version_types
        if str(item.get("slug", "")).startswith(slug_prefix)
    }


def find_game_version_id(
    game_versions: list[dict[str, object]],
    type_ids: set[int],
    value: str,
    java_version: str,
) -> int:
    candidates = {normalize(name) for name in names_for(value, java_version)}
    for item in game_versions:
        if int(item["gameVersionTypeID"]) in type_ids and normalize(str(item["name"])) in candidates:
            return int(item["id"])
    raise RuntimeError(f"Could not resolve CurseForge game version id for {value!r}.")


def main() -> int:
    version = os.environ["VERSION"]
    release_type = os.environ["RELEASE_TYPE"]
    token = os.environ["CURSEFORGE_TOKEN"]
    changelog = Path("build/publish-changelog.md").read_text(encoding="utf-8")
    matrix = load_matrix()
    java_version = matrix["java_version"]
    environment = matrix["environment"]

    version_types = request_json("/game/version-types?cache=true", token)
    game_versions = request_json("/game/versions?cache=true", token)
    if not isinstance(version_types, list) or not isinstance(game_versions, list):
        raise RuntimeError("CurseForge returned an unexpected game version response.")

    type_ids = {
        "minecraft": find_type_ids(version_types, "minecraft"),
        "loader": find_type_ids(version_types, "modloader"),
        "java": find_type_ids(version_types, "java"),
        "environment": find_type_ids(version_types, "environment"),
    }

    java_id = find_game_version_id(game_versions, type_ids["java"], java_version, java_version)
    environment_id = find_game_version_id(
        game_versions, type_ids["environment"], environment, java_version
    )

    for target in matrix["targets"]:
        file_path = artifact_path(target, version)
        release_name = display_name(matrix, target, version)
        game_version_ids = [
            *(
                find_game_version_id(game_versions, type_ids["minecraft"], item, java_version)
                for item in target["minecraft_versions"]
            ),
            find_game_version_id(game_versions, type_ids["loader"], target["loader"], java_version),
            java_id,
            environment_id,
        ]
        metadata = {
            "changelog": changelog,
            "changelogType": "markdown",
            "displayName": release_name,
            "gameVersions": game_version_ids,
            "releaseType": release_type,
        }

        print(f"Uploading {file_path} to CurseForge with environment {environment}.")
        result = retry_upload(
            str(file_path),
            lambda: multipart_request(
                f"{API_BASE}/projects/{matrix['curseforge_project_id']}/upload-file",
                ("X-Api-Token", token),
                {"metadata": json.dumps(metadata, separators=(",", ":"))},
                {"file": file_path},
            ),
        )
        print(f"Uploaded CurseForge file id {result.get('id')} for {release_name}.")

    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except urllib.error.HTTPError as error:
        sys.stderr.write(error.read().decode("utf-8", errors="replace") + "\n")
        raise
