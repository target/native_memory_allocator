#!/bin/bash

# Script to invoke the FlatBuffers compiler flatc to generate Kotlin FlatBuffer classes.
# You must have flatc installed locally and in your PATH before running this script.

BASE_DIRECTORY=$(dirname $0)

rm -fr $BASE_DIRECTORY/src/main/kotlin/com/target/nativememoryallocator/examples/map/offheap/flatbuffers/generated

flatc --kotlin -o $BASE_DIRECTORY/src/main/kotlin $BASE_DIRECTORY/src/main/flatbuffers/DemoSchema.fbs