java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

dependencies {
    paperweight.paperDevBundle("26.3.build.7-alpha")
}

group = "com.extremelyd1"
version = "1.13.0"
description = "MinecraftBingo"