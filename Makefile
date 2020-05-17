all: pom.xml \
	src/main/java/xyz/magicpixel/spigotplugin/SpigotPlugin.java\
	src/main/java/xyz/magicpixel/spigotplugin/MpxCommand.java
	mvn package

install: all
	cp target/MagicPixel-0.1-SNAPSHOT.jar ../plugins
