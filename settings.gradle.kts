pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // MuPDF repository
        maven { url = uri("https://maven.ghostscript.com") }
    }
}

rootProject.name = "PDF Reader Pro"
include(":app")