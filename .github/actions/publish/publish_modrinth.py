from __future__ import annotations

import json
import os
import sys
import urllib.error
from pathlib import Path

from publish_common import (
    artifact_path,
    display_name,
    load_matrix,
    multipart_request,
    platform_version,
    retry_upload,
)


API_BASE = "https://api.modrinth.com/v2"


def main() -> int:
    version = os.environ["VERSION"]
    release_type = os.environ["RELEASE_TYPE"]
    token = os.environ["MODRINTH_TOKEN"]
    changelog = Path("build/publish-changelog.md").read_text(encoding="utf-8")
    matrix = load_matrix()

    for target in matrix["targets"]:
        file_path = artifact_path(target, version)
        release_name = display_name(matrix, target, version)
        version_number = platform_version(target, version)
        metadata = {
            "name": release_name,
            "version_number": version_number,
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

        print(f"Uploading {file_path} to Modrinth as {version_number}.")
        result = retry_upload(
            str(file_path),
            lambda: multipart_request(
                f"{API_BASE}/version",
                ("Authorization", token),
                {"data": json.dumps(metadata, separators=(",", ":"))},
                {"file": file_path},
            ),
        )
        print(f"Uploaded Modrinth version id {result.get('id')} for {release_name}.")

    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except urllib.error.HTTPError as error:
        sys.stderr.write(error.read().decode("utf-8", errors="replace") + "\n")
        raise
