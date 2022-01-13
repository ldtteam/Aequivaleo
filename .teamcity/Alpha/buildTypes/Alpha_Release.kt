package Alpha.buildTypes

import OfficialPublications.buildTypes.OfficialPublications_CommonB
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle

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
        snapshot(OfficialPublications.buildTypes.OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
    
    disableSettings("RUNNER_134")
})
