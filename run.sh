#!/bin/sh

rm -f bin/*
javac -d bin -cp jar/* Main.java
java -cp bin:jar/* Main
