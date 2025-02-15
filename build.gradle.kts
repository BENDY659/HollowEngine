
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.spongepowered.asm.gradle.plugins.MixinExtension
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

buildscript {
    repositories {
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
        maven { url = uri("https://maven.parchmentmc.org") }
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.parchmentmc:librarian:1.+")
        classpath("org.spongepowered:mixingradle:0.7.38")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}

plugins {
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.jetbrains.kotlin.jvm").version("1.8.21")
    id("org.jetbrains.kotlin.plugin.serialization").version("1.8.21")
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

apply {
    plugin("kotlin")
    plugin("maven-publish")
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
    plugin("org.parchmentmc.librarian.forgegradle")
}

val mcVersion: String by project
val modVersion: String by project
val mappingsVersion: String by project
val hcVersion: String by project
val forgeVersion: String by project
val kffVersion: String by project
val ksffVersion: String by project
val bookshelfVersion: String by project
val stagesVersion: String by project

group = "ru.hollowhorizon"
version = "${mcVersion}-$modVersion"
project.setProperty("archivesBaseName", "hollowengine")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<UserDevExtension> {
    mappings("parchment", "${mappingsVersion}-$mcVersion")

    accessTransformer("src/main/resources/META-INF/accesstransformer.cfg")

    runs.create("client") {
        workingDirectory(project.file("run"))
        property("forge.logging.markers", "REGISTRIES") // eg: SCAN,REGISTRIES,REGISTRYDUMP
        property("forge.logging.console.level", "debug")
        //jvmArg("-XX:+AllowEnhancedClassRedefinition")
        arg("-mixin.config=hollowengine.mixins.json")
        mods.create("hollowengine") {
            source(the<JavaPluginExtension>().sourceSets.getByName("main"))
        }
    }

    runs.create("server") {
        workingDirectory(project.file("run"))
        property("forge.logging.markers", "REGISTRIES") // eg: SCAN,REGISTRIES,REGISTRYDUMP
        property("forge.logging.console.level", "debug")
        arg("-mixin.config=hollowengine.mixins.json")
        //jvmArg("-XX:+AllowEnhancedClassRedefinition")
        mods.create("hollowengine") {
            source(the<JavaPluginExtension>().sourceSets.getByName("main"))
        }
    }

}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://cursemaven.com")
    maven("https://maven.blamejared.com")
    flatDir {
        dir("hc")
        dir("libs")
    }
}

configure<MixinExtension> {
    config("hollowengine.mixins.json")
    add(sourceSets.main.get(), "hollowengine.refmap.json")
}

dependencies {
    minecraft("net.minecraftforge:forge:${mcVersion}-${forgeVersion}")

    implementation(kotlin("script-runtime"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(kotlin("scripting-jvm"))
    implementation("thedarkcolour:kotlinforforge:$kffVersion")
    implementation(fg.deobf("ru.hollowhorizon:kotlinscript:${ksffVersion}"))
    implementation(fg.deobf("ru.hollowhorizon:hc:${mcVersion}-${hcVersion}"))
    implementation(fg.deobf("curse.maven:jei-238222:4712866"))
    implementation(fg.deobf("curse.maven:wthit-forge-455982:4819215"))
    implementation(fg.deobf("curse.maven:badpackets-615134:4784364"))
    implementation(fg.deobf("curse.maven:embeddium-908741:4984830"))
    implementation(fg.deobf("curse.maven:oculus-581495:4763262"))
    implementation(fg.deobf("curse.maven:spark-361579:4505309"))

    implementation(fg.deobf("net.darkhax.bookshelf:Bookshelf-Forge-$mcVersion:$bookshelfVersion"))
    implementation(fg.deobf("net.darkhax.gamestages:GameStages-Forge-$mcVersion:$stagesVersion"))

    compileOnly(fg.deobf("curse.maven:ftb-library-forge-404465:4661834"))
    compileOnly(fg.deobf("curse.maven:ftb-quests-forge-289412:5060506"))

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

fun Jar.createManifest() = manifest {
    attributes(
        "Automatic-Module-Name" to "hollowengine",
        "Specification-Title" to "HollowEngine",
        "Specification-Vendor" to "HollowHorizon",
        "Specification-Version" to "1", // We are version 1 of ourselves
        "Implementation-Title" to project.name,
        "Implementation-Version" to version,
        "Implementation-Vendor" to "HollowHorizon",
        "Implementation-Timestamp" to ZonedDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")),
        "MixinConfigs" to "hollowengine.mixins.json"
    )
}

val jar = tasks.named<Jar>("jar") {
    archiveClassifier.set("")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude(
        "LICENSE.txt", "META-INF/MANIFSET.MF", "META-INF/maven/**",
        "META-INF/*.RSA", "META-INF/*.SF", "META-INF/versions/**", "**/module-info.class"
    )

    createManifest()

    finalizedBy("reobfJar")
}