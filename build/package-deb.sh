echo "Preparing package"

owlplugversion=$1

echo "Exporting LICENSE File"

cp ../LICENSE ./input/LICENSE

echo "Copying owlplug-client-$owlplugversion.jar to owlplug.jar"

cp ../owlplug-client/target/owlplug-client-$owlplugversion.jar ./input/owlplug.jar

echo "Generating OwlPlug DEB Install package"

jpackage --input ./input/ --name OwlPlug --main-class org.springframework.boot.loader.JarLauncher \
--main-jar owlplug.jar --license-file ./input/LICENSE --dest ./output \
--java-options "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" \
--app-version $owlplugversion --icon ./resources/owlplug.icns --vendor OwlPlug \
--linux-package-name owlplug --linux-deb-maintainer contact@owlplug.com \
--linux-menu-group "AudioVideo;Audio" --linux-shortcut

mv ./output/owlplug_$owlplugversion-1_amd64.deb ./output/OwlPlug-$owlplugversion.deb

ls ./output

echo "OwlPlug DEB Install package generated"
