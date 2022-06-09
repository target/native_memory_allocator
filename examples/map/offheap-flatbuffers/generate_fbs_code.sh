#!/bin/bash

# Script to invoke the FlatBuffers compiler flatc to generate Kotlin FlatBuffer classes.

BASE_DIRECTORY=$(dirname $0)

rm -fr $BASE_DIRECTORY/src/main/kotlin/com/target/nativememoryallocator/examples/map/offheap/flatbuffers/generated

flatc --kotlin -o $BASE_DIRECTORY/src/main/kotlin $BASE_DIRECTORY/src/main/flatbuffers/DemoSchema.fbs