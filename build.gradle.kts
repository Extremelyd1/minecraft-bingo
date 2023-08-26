java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {
    mavenLocal()

    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api("io.papermc:paperlib:1.0.7")
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}

group = "com.extremelyd1"
version = "1.10.0"
description = "MinecraftBingo"