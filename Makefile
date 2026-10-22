
.PHONY: build run clean

build:
	.\gradlew.bat build

run:
	.\gradlew.bat runClient

clean:
	.\gradlew.bat clean
