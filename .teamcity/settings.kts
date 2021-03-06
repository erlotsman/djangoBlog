import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.2"

project {

    buildType(Build)

    features {
        dockerRegistry {
            id = "PROJECT_EXT_3"
            name = "Docker Registry"
            url = "https://docker.io"
            userName = "revolyram"
            password = "credentialsJSON:8b0c4741-a4bf-46b3-933a-46bf202033c9"
        }
    }
}

object Build : BuildType({
    name = "Build and Push"

    vcs {
        root(DslContext.settingsRoot)

        branchFilter = """
            +:master
            +:stage
            +:develop
        """.trimIndent()
    }

    steps {
        dockerCommand {
            name = "Build Blog"
            commandType = build {
                source = file {
                    path = "Dockerfile"
                }
                namesAndTags = "revolyram/djangoblog:%teamcity.build.branch%-%build.number%"
                commandArgs = "--pull"
            }
        }
        dockerCommand {
            name = "Push blog"
            commandType = push {
                namesAndTags = "revolyram/djangoblog:%teamcity.build.branch%-%build.number%"
            }
        }
    }

    triggers {
        vcs {
            enableQueueOptimization = false
        }
    }

    features {
        dockerSupport {
            cleanupPushedImages = true
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_3"
            }
        }
        vcsLabeling {
            vcsRootId = "${DslContext.settingsRoot.id}"
            labelingPattern = "%teamcity.build.branch%-%build.number%"
            successfulOnly = true
            branchFilter = ""
        }
    }
})
