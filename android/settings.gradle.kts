pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Huawei HMS Core Maven repository — required for Account Kit (Huawei ID sign-in).
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}
rootProject.name = "Mindset Frames Tracker"
include(":app")
