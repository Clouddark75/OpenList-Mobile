#!/bin/bash

GIT_REPO="https://github.com/Clouddark75/OpenList.git"

function to_int() {
    echo $(echo "$1" | grep -oE '[0-9]+' | tr -d '\n')
}

# Usar siempre el tag fijo
LATEST_VER="0_Disaster-2.0"
LATEST_VER_INT=$(to_int "$LATEST_VER")
echo "Usando tag fijo: $LATEST_VER (${LATEST_VER_INT})"

echo "openlist_version=$LATEST_VER" >> "$GITHUB_ENV"
# VERSION_FILE="$GITHUB_WORKSPACE/openlist_version.txt"

VER=$(cat "$VERSION_FILE" 2>/dev/null)

if [ -z "$VER" ]; then
  VER="v3.25.1"
  echo "No version file, use default version ${VER}"
fi

VER_INT=$(to_int $VER)

echo "Current OpenList version: $VER ${VER_INT}"

if [ "$VER_INT" -ge "$LATEST_VER_INT" ]; then
    echo "Current >= Target"
    echo "openlist_update=0" >> "$GITHUB_ENV"
else
    echo "Current < Target"
    echo "openlist_update=1" >> "$GITHUB_ENV"
fi
