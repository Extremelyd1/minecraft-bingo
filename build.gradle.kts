java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

group = "com.extremelyd1"
version = "1.11.0"
description = "MinecraftBingo"