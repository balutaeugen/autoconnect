from __future__ import annotations

import os
import sys

from publish_common import artifact_path, load_matrix


def main() -> int:
    version = os.environ["VERSION"]
    matrix = load_matrix()
    missing = [artifact_path(target, version) for target in matrix["targets"] if not artifact_path(target, version).is_file()]

    if missing:
        print("Missing expected publish artifact(s):", file=sys.stderr)
        for path in missing:
            print(f" - {path}", file=sys.stderr)
        return 1

    for target in matrix["targets"]:
        print(f"Found {artifact_path(target, version)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
