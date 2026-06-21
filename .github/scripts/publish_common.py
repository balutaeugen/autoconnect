from __future__ import annotations

import json
import mimetypes
import time
import urllib.error
import urllib.request
import uuid
from pathlib import Path
from typing import Any, Callable, TypeVar


SCRIPT_DIR = Path(__file__).resolve().parent
USER_AGENT = "AutoConnect publish workflow"
UPLOAD_RETRY_SECONDS = 10 * 60
UPLOAD_RETRY_DELAY_SECONDS = 15
T = TypeVar("T")


def load_matrix() -> dict[str, Any]:
    return json.loads((SCRIPT_DIR / "publish_matrix.json").read_text(encoding="utf-8"))


def artifact_path(target: dict[str, Any], version: str) -> Path:
    return Path(
        f"publish/autoconnect-{target['loader']}-{version}-{target['minecraft_label']}.jar"
    )


def display_name(matrix: dict[str, Any], target: dict[str, Any], version: str) -> str:
    loader_name = matrix["loaders"][target["loader"]]
    return f"AutoConnect {version} {loader_name} {target['minecraft_label']}"


def platform_version(target: dict[str, Any], version: str) -> str:
    return f"{version}+{target['loader']}-{target['minecraft_label']}"


def multipart_request(
    url: str,
    token_header: tuple[str, str],
    fields: dict[str, str],
    files: dict[str, Path],
) -> object:
    boundary = f"----autoconnect-{uuid.uuid4().hex}"
    body = bytearray()

    for name, value in fields.items():
        body.extend(f"--{boundary}\r\n".encode("utf-8"))
        body.extend(f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode("utf-8"))
        body.extend(value.encode("utf-8"))
        body.extend(b"\r\n")

    for name, source in files.items():
        file_name = source.name
        content_type = mimetypes.guess_type(file_name)[0] or "application/java-archive"
        body.extend(f"--{boundary}\r\n".encode("utf-8"))
        body.extend(
            (
                f'Content-Disposition: form-data; name="{name}"; filename="{file_name}"\r\n'
                f"Content-Type: {content_type}\r\n\r\n"
            ).encode("utf-8")
        )
        body.extend(source.read_bytes())
        body.extend(b"\r\n")

    body.extend(f"--{boundary}--\r\n".encode("utf-8"))

    request = urllib.request.Request(
        url,
        data=bytes(body),
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            "User-Agent": USER_AGENT,
            token_header[0]: token_header[1],
        },
        method="POST",
    )

    with urllib.request.urlopen(request, timeout=180) as response:
        return json.loads(response.read().decode("utf-8"))


def retry_upload(description: str, upload: Callable[[], T]) -> T:
    deadline = time.monotonic() + UPLOAD_RETRY_SECONDS
    attempt = 1

    while True:
        try:
            return upload()
        except urllib.error.HTTPError as error:
            if error.code < 500 or time.monotonic() >= deadline:
                raise

            remaining = max(0, int(deadline - time.monotonic()))
            print(
                f"{description} failed with HTTP {error.code}; retrying in "
                f"{UPLOAD_RETRY_DELAY_SECONDS}s ({remaining}s left)."
            )
            time.sleep(min(UPLOAD_RETRY_DELAY_SECONDS, remaining))
            attempt += 1
