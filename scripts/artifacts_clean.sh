#!/bin/bash

# Program arguments:
# 1.- The artifacts path to clean in the local filesystem.

if [ $# -ne 1 ];
then
  echo "[ERROR] Missing artifact path argument. Exiting!"
  exit 65
fi

KIE_ARTIFACTS_PATH=$1

# Clean previously deployed artifacts
if [[ ! -z "$KIE_ARTIFACTS_PATH" ]] ; then
    if [ ! -d "$KIE_ARTIFACTS_PATH" ]; then
          echo "Artifacts path '$KIE_ARTIFACTS_PATH' not found. Creating it."
          mkdir -p $KIE_ARTIFACTS_PATH
    fi
    echo "Cleaning previous deployed artifacts at path '$KIE_ARTIFACTS_PATH'.."
    rm -rf $KIE_ARTIFACTS_PATH/*
fi
