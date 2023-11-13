#!/bin/sh -e

if [[ $(arch) == "arm64" ]] ; then
    echo "Re-execing build using Rosetta." >&2
    exec arch -x86_64 /bin/sh ${0} "${@}"
fi

SDK_NAME=macosx

CONCURRENCY=$(sysctl -n hw.activecpu)
CODE_SIGN_IDENTITY=${AMFITRUSTED_IDENTITY:--}
DEBUG_LEVEL=release

# Exporting this such that /usr/bin/clang uses it implictly
export SDKROOT=$(xcrun --sdk ${SDK_NAME} --show-sdk-path)
export PATH=/AppleInternal/Java/bin:$PATH

do_jnf() {
    mkdir -p buildNative/Frameworks
    xcodebuild install -project openjdk/apple/JavaNativeFoundation/JavaNativeFoundation.xcodeproj -target JavaNativeFoundation -configuration Release DSTROOT="$(pwd)/buildNative/Frameworks" CODE_SIGN_IDENTITY="${CODE_SIGN_IDENTITY}" LD_DYLIB_INSTALL_NAME=@rpath/JavaNativeFoundation
}

do_jnf
