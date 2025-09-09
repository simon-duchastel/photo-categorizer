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
    }
}

rootProject.name = "Photo Categorizer"
include(":modules:app")

// Business logic modules
include(":modules:lib:auth")
include(":modules:lib:concurrency")
include(":modules:lib:filemanager")
include(":modules:lib:dropbox")
include(":modules:lib:storage")
include(":modules:lib:navigation")
include(":modules:lib:time")
include(":modules:lib:utils")

// UI modules
include(":modules:ui:theme")
include(":modules:ui:components")
include(":modules:ui:screens:login")
include(":modules:ui:screens:photoswiper")
include(":modules:ui:screens:settings")
include(":modules:ui:screens:splash")
