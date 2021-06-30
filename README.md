# JavaNativeFoundation

This project provides a version of the JavaNativeFoundation framework compiled both for `x86-64` and the M1 `arm64` architecture.
As there is no `arm64` definition for the JavaNativeFramework it is not possible to cross compile jni-libraries for M1 macs.
(See [this relevant thread](https://developer.apple.com/forums/thread/654601))
You can either include the framework directly or just use it as a drop in replacement for the linker during cross compilation on non arm macs.
If you only use the framwork for linking then you also need to specify the following linker options to ensure that the dynamic library is loaded
from the system framework path. Otherwise linking will fail as macOS can't find the JNF dynamic library relative to your library.
````
-rpath /System/Library/Frameworks/JavaVM.framework/Versions/A/Frameworks/
-rpath /System/Library/Frameworks/
````
The framework is compiled directly from the [apple openjdk](https://github.com/apple/openjdk/tree/xcodejdk14-release) port.
