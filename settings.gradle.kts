import java.util.Properties

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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val keysoftTokenProperty = "gitlab_maven_repo_deployToken"
val keysoftRepoUrl = providers.gradleProperty("gitlabMavenRepoUrl")
    .orElse("https://gitlab.com/api/v4/projects/45979364/packages/maven")
    .get()
val keysoftFileToken = file("keystore.properties")
    .takeIf { it.exists() }
    ?.inputStream()
    ?.use { stream ->
        Properties().apply { load(stream) }.getProperty(keysoftTokenProperty)
    }
    ?: ""
val keysoftRepoToken = providers.gradleProperty(keysoftTokenProperty).orNull
    ?: providers.environmentVariable("GITLAB_MAVEN_REPO_DEPLOY_TOKEN").orNull
    ?: keysoftFileToken

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri(keysoftRepoUrl)
            credentials(HttpHeaderCredentials::class) {
                name = "Deploy-Token"
                value = keysoftRepoToken
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}
rootProject.name = "WordBopper"
include(":app")
