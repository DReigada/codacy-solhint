#!/usr/bin/env bash

set -e

current_directory="$( cd "$( dirname "$0" )" && pwd )"
TOOL_VERSION="$(cat .solhintVersion)"

VERSION="1.0.0-$(git symbolic-ref --short HEAD)-SNAPSHOT"

if [ -z "$1" ]; then
  echo >&2 "Tool name was not provided."
  echo >&2 "usage: $0 <tool-name> [version (1.0.0-branch-name-SNAPSHOT)]"
  exit 1
else
  TOOL_NAME="$1"
fi


echo "Building docker with version ${VERSION} for ${TOOL_NAME}"
sbt 'set version := "'"${VERSION}"'"' docker:stage
docker build --no-cache -t "codacy/${TOOL_NAME}:${VERSION}" -f Dockerfile . --build-arg toolVersion=$TOOL_VERSION
