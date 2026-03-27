plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "yaml.visual.preview.plugin.hashizume.online"
version = "1.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1")
        bundledPlugin("org.jetbrains.plugins.yaml")
        instrumentationTools()
    }
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "253.*"
        }
    }
    signing {
        privateKey = providers.fileContents(layout.projectDirectory.file("signing/private.pem")).asText
        certificateChain = providers.fileContents(layout.projectDirectory.file("signing/chain.crt")).asText
    }
    buildSearchableOptions = false
}
