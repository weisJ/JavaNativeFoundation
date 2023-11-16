#!/bin/sh -e

SDK_NAME=macosx

CONCURRENCY=$(sysctl -n hw.activecpu)
CODE_SIGN_IDENTITY=${AMFITRUSTED_IDENTITY:--}
DEBUG_LEVEL=release

# Exporting this such that /usr/bin/clang uses it implictly
export SDKROOT=$(xcrun --sdk ${SDK_NAME} --show-sdk-path)
MACOSX_DEPLOYMENT_TARGET=10.10
export LD_DYLIB_INSTALL_NAME=@rpath/JavaNativeFoundation.framework/Versions/A/JavaNativeFoundation

cc=/usr/bin/cc
lipo=/usr/bin/lipo
srcdir=$(pwd)/src/JavaNativeFoundation
include1=${JAVA_HOME}/include
include2=${JAVA_HOME}/include/darwin
include3=${srcdir}/..

topdir=$(pwd)
libdir=$(pwd)/buildNative/lib
fwdir=$(pwd)/buildNative/Frameworks

build1() {
    arch=$1
    echo building for $arch
    ${cc} -arch $arch -o ${libdir}/lib$arch.dylib -I${include1} -I${include2} -I${include3} -framework Cocoa \
        -dynamiclib -ObjC -fvisibility=hidden -install_name ${LD_DYLIB_INSTALL_NAME} -g \
        -mmacosx-version-min=${MACOSX_DEPLOYMENT_TARGET} -Wl,-current_version,80 -Wl,-compatibility_version,1 \
        ${srcdir}/*.m
}

do_jnf() {
    rm -rf buildNative
    mkdir -p buildNative/Frameworks
    mkdir -p buildNative/lib
    build1 x86_64
    build1 arm64
    ${lipo} ${libdir}/libx86_64.dylib ${libdir}/libarm64.dylib -create -output ${libdir}/JavaNativeFoundation
    ${lipo} ${libdir}/libx86_64.dylib.dSYM/Contents/Resources/DWARF/libx86_64.dylib \
            ${libdir}/libarm64.dylib.dSYM/Contents/Resources/DWARF/libarm64.dylib \
            -create -output ${libdir}/JavaNativeFoundation-DSYM

    cd ${fwdir}
    mkdir JavaNativeFoundation.framework
    cd JavaNativeFoundation.framework
    mkdir -p Versions/A/Headers
    mkdir -p Versions/A/Resources
    mkdir -p Versions/A/Modules
    cp -p ${libdir}/JavaNativeFoundation Versions/A
    cp -p ${srcdir}/J*.h Versions/A/Headers
    cp -p ${srcdir}/Modules/module.modulemap Versions/A/Modules
    xcrun tapi stubify --filetype=tbd-v5 -o Versions/A/JavaNativeFoundation.tbd \
         Versions/A/JavaNativeFoundation
    cp -p ${topdir}/Info.plist Versions/A/Resources

    (cd Versions; ln -s A Current)
    ln -s Versions/Current/Headers .
    ln -s Versions/Current/Resources .
    ln -s Versions/Current/Modules .
    ln -s Versions/Current/JavaNativeFoundation .
    ln -s Versions/Current/JavaNativeFoundation.tbd .

    cd ${fwdir}
    mkdir -p JavaNativeFoundation.framework.dSYM/Contents/Resources/DWARF
    cp -p ${libdir}/JavaNativeFoundation-DSYM JavaNativeFoundation.framework.dSYM/Contents/Resources/DWARF/JavaNativeFoundation
    cp -p ${topdir}/dsym-Info.plist JavaNativeFoundation.framework.dSYM/Contents/Info.plist

    codesign --sign ${CODE_SIGN_IDENTITY} --timestamp --force --verbose JavaNativeFoundation.framework/*.tbd
    codesign --sign ${CODE_SIGN_IDENTITY} --timestamp --force --verbose JavaNativeFoundation.framework
    codesign --sign ${CODE_SIGN_IDENTITY} --timestamp --force --verbose JavaNativeFoundation.framework.dSYM
}

do_jnf
