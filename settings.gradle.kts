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
include(":app")
include(":app:modules:auth")
include(":app:modules:filemanager")
include(":app:modules:dropbox")
include(":app:modules:storage")

include(":app:modules:lib:navigation")
include(":app:modules:lib:utils")

include(":app:modules:ui:theme")
include(":app:modules:ui:components")
include(":app:modules:ui:screens:login")
include(":app:modules:ui:screens:photoswiper")
include(":app:modules:ui:screens:settings")
include(":app:modules:ui:screens:splash")
