#!/bin/bash

# Cambiado al repositorio correcto
GIT_REPO="https://github.com/Clouddark75/OpenList.git"
TARGET_BRANCH="0_Disaster-2"  # Branch específico que queremos usar

function to_int() {
    echo $(echo "$1" | grep -oE '[0-9]+' | tr -d '\n')
}

function get_latest_version() {
    # Modificado para obtener el último commit del branch específico en lugar de tags
    echo $(git ls-remote --heads $GIT_REPO $TARGET_BRANCH | cut -c1-7)
}

function get_branch_version() {
    # Función alternativa para obtener información del branch
    git ls-remote --heads $GIT_REPO $TARGET_BRANCH | head -1 | cut -f1 | cut -c1-12
}

# Intentar obtener la versión/commit del branch objetivo
LATEST_VER=""
for index in $(seq 5)
do
    echo "Intentando obtener la última versión del branch $TARGET_BRANCH, intento=$index"
    LATEST_VER=$(get_branch_version)
    if [ -z "$LATEST_VER" ]; then
      if [ "$index" -ge 5 ]; then
        echo "Falló al obtener la última versión, saliendo"
        exit 1
      fi
      echo "Falló al obtener la versión, esperando 15s y reintentando"
      sleep 15
    else
      break
    fi
done

# Para commits usaremos los primeros 8 caracteres como "versión"
LATEST_VER_SHORT=$(echo "$LATEST_VER" | cut -c1-8)
LATEST_VER_INT=$(echo "$LATEST_VER" | cut -c1-8 | tr 'a-f' '0-5' | tr -d 'g-z')

echo "Último commit del branch $TARGET_BRANCH: $LATEST_VER_SHORT"
echo "openlist_version=$LATEST_VER_SHORT" >> "$GITHUB_ENV"

# Verificar si existe el archivo de versión
if [ -n "$VERSION_FILE" ] && [ -f "$VERSION_FILE" ]; then
    VER=$(cat "$VERSION_FILE")
else
    VER="0000000"  # Versión por defecto (commit hash ficticio)
    echo "No existe archivo de versión, usando versión por defecto: ${VER}"
fi

echo "Versión actual de OpenList: $VER"

# Comparación simple de commits (si son diferentes, actualizar)
if [ "$VER" = "$LATEST_VER_SHORT" ]; then
    echo "Versión actual = Última versión"
    echo "openlist_update=0" >> "$GITHUB_ENV"
else
    echo "Versión actual != Última versión, se requiere actualización"
    echo "openlist_update=1" >> "$GITHUB_ENV"
fi

echo "✅ Verificación de versión completada"
