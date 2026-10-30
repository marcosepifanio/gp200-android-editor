# R8 rules for the release build.
#
# The app has no reflection: JSON caches use literal keys (org.json), the effect
# catalog is plain Kotlin and MIDI goes through android.media.midi. Library rules
# (AndroidX, Compose, Coil, coroutines) ship with the libraries themselves.

# Keep line numbers so crash reports in the Play Console can be deobfuscated with mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
