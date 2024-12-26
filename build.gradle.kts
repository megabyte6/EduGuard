import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    kotlin("jvm") version "2.+"
    id("com.gradleup.shadow") version "8.+"
    id("com.modrinth.minotaur") version "2.+"
    id("io.papermc.hangar-publish-plugin") version "0.1.+"
    id("xyz.jpenilla.run-paper") version "2.+"
}

project.group = providers.gradleProperty("group").get()
project.version = providers.gradleProperty("version").get()

val minecraftVersion: String by project
val paperMcVersion: String by project

val modrinthVersionType: String by project
val modrinthReleaseGameVersions: String by project

val hangarReleaseChannel: String by project
val hangarReleaseGameVersions: String by project

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperMcVersion")
}

kotlin {
    jvmToolchain(21)
}

modrinth {
    // Remember to have the MODRINTH_TOKEN environment variable set or else this will fail - just make sure it stays private!
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("ueOBZDS1")
    versionType.set(modrinthVersionType)
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(modrinthReleaseGameVersions.split(",").map(String::trim))
    syncBodyFrom.set(rootProject.file("README.md").readText())
}

hangarPublish {
    publications.register("plugin") {
        version.set(project.version as String)
        channel.set(hangarReleaseChannel)
        id.set("EduGuard")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(hangarReleaseGameVersions.split(",").map(String::trim))
            }
        }
    }
}

tasks {
    build {
        dependsOn(processResources, shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set("mc$minecraftVersion")
        archiveClassifier.set("")
    }

    modrinth.get().dependsOn(modrinthSyncBody)

    runServer {
        minecraftVersion(providers.gradleProperty("serverVersion").getOrElse(minecraftVersion))
    }
}
