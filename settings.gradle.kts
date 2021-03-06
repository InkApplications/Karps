enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "karps"

dependencyResolutionManagement {
    versionCatalogs {
        create("libraries") {
            from(files(
                "gradle/libraries.versions.toml"
            ))
        }
    }
}

include("cli")
include("client")
include("parser")
include("structures")

