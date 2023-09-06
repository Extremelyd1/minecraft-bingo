java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

group = "com.extremelyd1"
version = "1.10.0"
description = "MinecraftBingo"