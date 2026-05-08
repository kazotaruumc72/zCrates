plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("re.alwyn974.groupez.repository") version "1.0.0"
}

apply("gradle/copy-build.gradle")

extra.set("targetFolder", file("target/"))
extra.set("apiFolder", file("target-api/"))
extra.set("classifier", System.getProperty("archive.classifier"))
extra.set("sha", System.getProperty("github.sha"))

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "re.alwyn974.groupez.repository")

    group = "fr.traqueur"
    version = "1.1.0"

    repositories {
        mavenCentral()

        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven("https://jitpack.io")
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

        compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
        compileOnly("org.mozilla:rhino:1.7.14")
        compileOnly("org.reflections:reflections:0.10.2")

        compileOnly(files(rootProject.files("libs/zMenu-1.1.0.4.jar")))

        /* Libraries */
        implementation("fr.traqueur:structura:1.7.0")
        implementation("fr.traqueur.commands:platform-spigot:5.1.0")
        implementation("fr.maxlego08.sarah:sarah:1.23")
        implementation("org.bstats:bstats-bukkit:3.1.0")

        /* Test dependencies */
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
        testImplementation("org.mozilla:rhino:1.7.14")
        testImplementation("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
        testImplementation("org.slf4j:slf4j-simple:2.0.9")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set(if (project.path == ":") "" else project.name)
        archiveClassifier.set("")

        relocate("fr.traqueur.structura", "fr.traqueur.crates.libs.structura")
        relocate("fr.traqueur.commands", "fr.traqueur.crates.libs.commands")
        relocate("fr.maxlego08.sarah", "fr.traqueur.crates.libs.sarah")
        relocate("org.bstats", "fr.traqueur.crates.libs.bstats")
    }

}

dependencies {
    api(project(":api"))
    api(project(":common"))
    rootProject.subprojects.filter { it.path.startsWith(":hooks:") }.forEach { subproject ->
        implementation(project(subproject.path))
    }
}

tasks {
    shadowJar {
        rootProject.extra.properties["sha"]?.let { sha ->
            archiveClassifier.set("${rootProject.extra.properties["classifier"]}-${sha}")
        } ?: run {
            archiveClassifier.set(rootProject.extra.properties["classifier"] as String?)
        }
        destinationDirectory.set(rootProject.extra["targetFolder"] as File)
    }

    build {
        dependsOn(shadowJar)
        dependsOn(subprojects.map { it.tasks.shadowJar })
    }

    processResources {
        from("resources")
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}