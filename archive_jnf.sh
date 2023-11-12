#!/bin/sh -e

echo "Running archive_jnf.sh"
pwd
mkdir frameworks
rm -f frameworks/JavaNativeFoundation.framework.zip
cd buildNative/Frameworks; zip --symlinks -r ../../frameworks/JavaNativeFoundation.framework.zip JavaNativeFoundation.framework
