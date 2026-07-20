#!/usr/bin/env bash
set -u

if [[ "$#" -eq 0 ]]; then
  echo "Usage: $0 <gradle-task> [<gradle-task> ...]" >&2
  exit 2
fi

run_with_source() {
  local source_name="$1"
  shift
  local source_args=("$@")

  for attempt in {1..3}; do
    echo "$source_name Gradle attempt $attempt of 3"
    if gradle "${GRADLE_TASKS[@]}" "${source_args[@]}" --no-daemon --console=plain; then
      return 0
    fi

    if [[ "$attempt" -lt 3 ]]; then
      local delay=$((attempt * 15))
      echo "$source_name attempt failed; retrying in ${delay}s."
      sleep "$delay"
    fi
  done

  return 1
}

GRADLE_TASKS=("$@")

if run_with_source "Terraformers"; then
  exit 0
fi

echo "Terraformers failed after 3 attempts; switching Mod Menu to Modrinth Maven."

if run_with_source "Modrinth fallback" -PmodMenuRepository=modrinth; then
  exit 0
fi

echo "Gradle failed after 3 Terraformers attempts and 3 Modrinth attempts." >&2
exit 1
