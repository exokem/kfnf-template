
plugins {
    id("java-library")
    id("idea")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version "7.0.105"

    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
}

object Versions
{
    const val minecraft = "1.20.4"
    const val neoforge = "20.4.231"
    const val kotlinforforge = "4.8.0"

    const val jei = "17.3.0.49"

    const val minecraftRange = "[1.20.4,1.21)"
    const val neoforgeRange = "[20.4,)"
    const val loaderRange = "[2,)"

    const val java = 17
    val javaLang = JavaLanguageVersion.of(java)
}

object Mod
{
    const val group = "com.example.examplemod"
    const val id = "examplemod"
    const val name = "Example Mod"
    const val version = "1.0.0"
    const val authors = "Author 1, Author 2"
    const val license = "All Rights Reserved"
    const val description = "A mod."
}

object DevUser
{
    // The name and uuid must match the same user for skins to work.
    const val name = "Israphel"
    const val uuid = "7c8c12fd45194f159d7b0da76dca41f9"
}

java.toolchain.languageVersion = Versions.javaLang

base.archivesName = Mod.id
group = Mod.group
version = Mod.version

minecraft.accessTransformers.file {
    rootProject.file("src/main/resources/META-INF/accesstransformer.cfg")
}

runs {
    configureEach {
        // SCAN - mods scan
        // REGISTRIES - firing of registry events
        // REGISTRYDUMP - contents of all registries
        systemProperty("forge.logging.markers", "REGISTRIES")

        // https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
        systemProperty("forge.logging.console.level", "debug")

        modSource(project.sourceSets.main.get())
    }

    register("client") {
        systemProperty("forge.enabledGameTestNamespaces", Mod.id)
        programArguments.addAll(
            "--username", DevUser.name,
            "--uuid", DevUser.uuid
        )
    }

    register("server") {
        systemProperty("forge.enabledGameTestNamespaces", Mod.id)
        programArgument("--nogui")
    }

    register("gameTestServer") {
        systemProperty("forge.enabledGameTestNamespaces", Mod.id)
    }

    register("data") {
        programArguments.addAll(
            "--mod", Mod.id,
            "--all",
            "--output", file("src/generated/resources").absolutePath,
            "--existing", file("src/main/resources").absolutePath
        )
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    // Kotlin for Forge
    maven(url = "https://thedarkcolour.github.io/KotlinForForge/")

    // Jei
    maven("https://maven.blamejared.com/")
    maven("https://modmaven.dev/")
}

dependencies {
    implementation("net.neoforged:neoforge:${Versions.neoforge}")
    implementation("thedarkcolour:kotlinforforge-neoforge:${Versions.kotlinforforge}")

    compileOnly("mezz.jei:jei-${Versions.minecraft}-common-api:${Versions.jei}")
    compileOnly("mezz.jei:jei-${Versions.minecraft}-neoforge-api:${Versions.jei}")
    runtimeOnly("mezz.jei:jei-${Versions.minecraft}-neoforge:${Versions.jei}")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks {
    withType<ProcessResources>().configureEach {
        val properties = linkedMapOf(
            "minecraft_version" to Versions.minecraft,
            "minecraft_version_range" to Versions.minecraftRange,

            "neo_version" to Versions.neoforge,
            "neo_version_range" to Versions.neoforgeRange,

            "loader_version_range" to Versions.loaderRange,

            "mod_id" to Mod.id,
            "mod_name" to Mod.name,
            "mod_license" to Mod.license,
            "mod_version" to Mod.version,
            "mod_authors" to Mod.authors,
            "mod_description" to Mod.description
        )
        inputs.properties(properties)

        filesMatching("META-INF/mods.toml") {
            expand(properties)
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(17)
}
