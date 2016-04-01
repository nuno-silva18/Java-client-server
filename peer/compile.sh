#!/bin/sh

echo "Compiling Peer"
javac sdis/handler/*.java
javac sdis/multicast/*.java
javac sdis/*.java
echo "Done"
