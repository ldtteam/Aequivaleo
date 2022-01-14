import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.githubIssues
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule

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

version = "2021.2"

project {
    description = "The mediator library for alchemy mods."

    params {
        param("Current Minecraft Version", "latest")
        text("Repository", "ldtteam/aequivaleo", label = "Repository", description = "The repository for the project.", readOnly = true, allowEmpty = true)
        param("env.Version.Minor", "1")
        param("Project.Type", "mods")
        param("env.Version.Patch", "0")
        param("Upsource.Project.Id", "aequivaleo")
        param("env.Version.Suffix", "")
        param("env.Version.Major", "0")
        param("filename.prefix", "aequivaleo")
        text("env.Version", "%env.Version.Major%.%env.Version.Minor%.%env.Version.Patch%%env.Version.Suffix%", label = "Version", description = "The version of the project.", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }

    features {
        githubIssues {
            id = "PROJECT_EXT_12"
            displayName = "ldtteam/minecolonies"
            repositoryURL = "https://github.com/ldtteam/minecolonies"
            authType = accessToken {
                accessToken = "credentialsJSON:47381468-aceb-4992-93c9-1ccd4d7aa67f"
            }
        }
    }
    subProjectsOrder = arrayListOf(RelativeId("UpgradeBetaRelease"), RelativeId("UpgradeAlphaBeta"), RelativeId("Alpha"), RelativeId("OfficialPublications"), RelativeId("Branches"))

    subProject(OfficialPublications)
    subProject(UpgradeAlphaBeta)
    subProject(Branches)
    subProject(Beta)
    subProject(UpgradeBetaRelease)
    subProject(Release)
    subProject(Alpha)
}


object Alpha : Project({
    name = "Alpha"
    description = "Alpha version builds of Aquivaleo"

    buildType(Alpha_Release)

    params {
        password("env.crowdinKey", "credentialsJSON:be67336c-4ed1-464c-b531-92270ba39b53", label = "Crowdin key", description = "The API key for crowdin to pull translations")
        param("Default.Branch", "version/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/version/(*)")
        param("env.CURSERELEASETYPE", "alpha")
        param("env.Version.Suffix", "-ALPHA")
    }
})

object Alpha_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Alpha to CurseForge"

    allowExternalStatus = true

    params {
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
        param("Default.Branch", "version/latest")
    }

    vcs {
        branchFilter = "+:*"
    }

    steps {
        gradle {
            name = "Analyze"
            id = "RUNNER_140"
            tasks = "sonarqube"
            gradleParams = "-Dsonar.projectKey=ldtteam_Aequivaleo -Dsonar.host.url=https://code-analysis.ldtteam.com -Dsonar.login=%sonarqube.token%"
            dockerImage = "gradle:%env.GRADLE_VERSION%-%env.JDK_VERSION%"
            dockerRunParameters = "-u root -v /opt/buildagent/gradle/caches:/home/gradle/.gradle/caches"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
    
    disableSettings("RUNNER_134")
})


object Beta : Project({
    name = "Beta"
    description = "Beta version builds of Aequivaleo"

    buildType(Beta_Release)

    params {
        password("env.crowdinKey", "credentialsJSON:be67336c-4ed1-464c-b531-92270ba39b53", label = "Crowdin key", description = "The API for getting the crowdin translations")
        param("Default.Branch", "testing/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/testing/(*)")
        param("env.CURSERELEASETYPE", "beta")
        param("env.Version.Suffix", "-BETA")
    }
})

object Beta_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Alpha to CurseForge"

    params {
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
    }

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})


object Branches : Project({
    name = "Branches"
    description = "All none release branches."

    buildType(Branches_Build)
    buildType(Branches_Common)

    params {
        text("Default.Branch", "CI/Default", label = "Default branch", description = "The default branch for branch builds", readOnly = true, allowEmpty = true)
        param("VCS.Branches", """
            +:refs/heads/(*)
            -:refs/heads/version/*
            -:refs/heads/testing/*
            -:refs/heads/release/*
            -:refs/pull/*/head
            -:refs/heads/CI/*
        """.trimIndent())
        param("env.Version.Suffix", "-PERSONAL")
    }

    cleanup {
        baseRule {
            all(days = 60)
        }
    }
})

object Branches_Build : BuildType({
    templates(AbsoluteId("LetSDevTogether_Build"))
    name = "Build"
    description = "Builds the branch without testing."

    params {
        param("env.Version.Patch", "${Branches_Common.depParamRefs.buildNumber}")
    }

    dependencies {
        snapshot(Branches_Common) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
    
    disableSettings("BUILD_EXT_14")
})

object Branches_Common : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Tracks the amount of builds run for branches"
})


object OfficialPublications : Project({
    name = "Official Publications"
    description = "Holds projects and builds related to official publications"

    buildType(OfficialPublications_CommonB)
})

object OfficialPublications_CommonB : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Represents the version counter within Minecolonies for official releases."
})


object Release : Project({
    name = "Release"
    description = "Release version builds of Aquivaleo"

    buildType(Release_Release)

    params {
        password("env.crowdinKey", "credentialsJSON:be67336c-4ed1-464c-b531-92270ba39b53", label = "Crowdin key", description = "The API key for getting crowdin translations")
        param("Default.Branch", "release/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/release/(*)")
        param("env.CURSERELEASETYPE", "release")
        param("env.Version.Suffix", "-RELEASE")
    }
})

object Release_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Release to CurseForge"

    params {
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
    }

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})


object UpgradeAlphaBeta : Project({
    name = "Upgrade - Alpha -> Beta"
    description = "Updates the current alpha to beta."

    buildType(UpgradeAlphaBeta_UpgradeAlphaBeta)
})

object UpgradeAlphaBeta_UpgradeAlphaBeta : BuildType({
    templates(AbsoluteId("LetSDevTogether_Upgrade"))
    name = "Upgrade - Alpha -> Beta"
    description = "Upgrades the current Alpha to Beta."

    params {
        text("Source.Branch", "version", label = "Source branch type", description = "The source branch type for the upgrade. EG: version or testing", allowEmpty = false)
        param("Default.Branch", "testing/latest")
        param("VCS.Branches", "+:refs/heads/testing/(*)")
        text("Target.Branch", "testing", label = "Target branch type", description = "The target branch type for the upgrade. EG: testing or release.", allowEmpty = false)
        text("env.Version", "%env.Version.Major%.%env.Version.Minor%.%build.counter%-BETA", label = "Version", description = "The version of the project.", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }

    steps {
        script {
            name = "Fetch"
            id = "RUNNER_80"
            scriptContent = "git pull"
            param("org.jfrog.artifactory.selectedDeployableServer.downloadSpecSource", "Job configuration")
            param("org.jfrog.artifactory.selectedDeployableServer.useSpecs", "false")
            param("org.jfrog.artifactory.selectedDeployableServer.uploadSpecSource", "Job configuration")
        }
        stepsOrder = arrayListOf("RUNNER_81", "RUNNER_80", "RUNNER_78", "RUNNER_79")
    }

    triggers {
        schedule {
            id = "TRIGGER_1"
            schedulingPolicy = weekly {
                timezone = "Europe/Berlin"
            }
            triggerBuild = always()
            param("revisionRuleBuildBranch", "<default>")
        }
    }
    
    disableSettings("BUILD_EXT_9")
})


object UpgradeBetaRelease : Project({
    name = "Upgrade Beta -> Release"
    description = "Upgrades the current Beta to Release"

    buildType(UpgradeBetaRelease_UpgradeBetaRelease)
})

object UpgradeBetaRelease_UpgradeBetaRelease : BuildType({
    templates(AbsoluteId("LetSDevTogether_Upgrade"))
    name = "Upgrade Beta -> Release"
    description = "Upgrades the current Beta to Release."

    params {
        text("Source.Branch", "testing", label = "Source branch type", description = "The source branch type for the upgrade. EG: version or testing", allowEmpty = false)
        text("Default.Branch", "release/%Current Minecraft Version%", label = "Default branch", description = "The default branch of this build.", allowEmpty = true)
        param("VCS.Branches", "+:refs/heads/release/(*)")
        text("Target.Branch", "release", label = "Target branch type", description = "The target branch type for the upgrade. EG: testing or release.", allowEmpty = false)
        text("env.Version", "%env.Version.Major%.%env.Version.Minor%.%build.counter%-RELEASE", label = "Version", description = "The version of the project.", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }
})
