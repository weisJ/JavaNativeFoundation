#!/bin/sh -e

distdir=$(pwd)/dist

mkdir ${distdir}
rm -f ${distdir}/JavaNativeFoundation.zip
rm -f ${distdir}/JavaNativeFoundation.dmg

(cd buildNative/Frameworks; zip --symlinks -r ${distdir}/JavaNativeFoundation.zip JavaNativeFoundation.framework)

hdiutil create -srcfolder buildNative/Frameworks -volname JavaNativeFoundation ${distdir}/JavaNativeFoundation.dmg

