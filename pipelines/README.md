# DevOps

## Building pipelines

`TES Features` does not specify a CI/CD pipeline for building the libraries. But it defines
a set of Python and Bash scripts that can be used to build a "local pipeline".

### Prerequisites

To use the local pipeline the following tools are required

#### SDK Man

[SDK Man](https://sdkman.io/) is used to install the required Java version. It the moment this repo
required Java 21.

#### Python 3

The pipeline is written in python 3 and bash.

### Local pipeline

This local pipeline can be executed with:

```bash
./pipeline.sh
```

Each step of the pipeline is implemented as a python script. This is done so its possible to share the
execution of a pipeline step between `Jenkins` and local execution.

The pipeline defines the following steps that is executed in order:

| **Step**    | **Script**            | **Description**                                                                            |
|-------------|-----------------------|--------------------------------------------------------------------------------------------|
| **Clean**   | `pipeline-clean.py`   | Clean the repository.                                                                      |
| **Build**   | `pipeline-build.py`   | Bulds all the libraries.                                                                   |
| **Publish** | `pipeline-publish.py` | Publish the artifacts to the local Maven repository and/or to a remote Maven repository.   |
| **Analyze** | `pipeline-analyze.py` | Analyse the artifacts for test coverage, vulnerabilities and code analysis with SonarQube. |

#### Additional scripts

Two additional scripts has also be introduced:

1. `pipeline-tools.sh`: Exports the used versions of `Java` in the console, so the pipeline always uses
   the current `Java` version.
2. `pipeline.sh`: Sources `pipeline-tools.sh` before it calls `pipeline.py`. All extra arguments that is passed
   to `pipeline.sh` is also passed to `pipeline.py`.

These scripts are created to make it possible to emulate the `tools` section of a `Jenkins` pipeline.

### Jenkins pipeline

A jenkins pipeline is not present at the moment for this repository. But it can be introduced by reusing
the scripts from the local pipeline.

Example of a `Jenkinsfile`:

```groovy
pipeline {
    agent any

    options {
        timeout(time: 120, unit: 'MINUTES')
    }

    environment {
        // Set owaspDependencyCheckApiKey System property for Gradle.
        // owaspDependencyCheckApiKey is the Api get that is used by 
        // `org.owasp:dependency-check-gradle` to check for vulnerabilities.
        ORG_GRADLE_PROJECT_owaspDependencyCheckApiKey = credentials('<nvd-api-credential>')

        // Give Gradle extra resources to execute the tasks.
        GRADLE_OPTS = '-Dorg.gradle.jvmargs="-Xmx2g -Xms512m -XX:+HeapDumpOnOutOfMemoryError"'
    }

    tools {
        jdk 'jdk-21'
    }

    triggers {
        // Run pipeline daily on a project determined time between midnight and 7am (only main)
        cron(env.branch_name == "main" ? 'H H(0-6) * * *' : '')
    }

    stages {
        stage('Clean') {
            steps {
                sh './pipelines/pipeline-clean.py'
            }
        }
        stage('Build') {
            steps {
                sh './pipelines/pipeline-build.py'
            }
            post {
                always {
                    archiveArtifacts artifacts: '**/build/docs/javadoc/**', fingerprint: true, onlyIfSuccessful: false
                    junit '**/build/test-results/**/*.xml'
                }
            }
        }
        stage('Publish & Analysis') {
            parallel {
                stage('Publish') {
                    steps {
                        sh './pipelines/pipeline-publish.py'
                    }
                }
                stage('Analyze') {
                    steps {
                        sh './pipelines/pipeline-analyze.py'
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: '**/build/reports/jacoco/**', fingerprint: true, onlyIfSuccessful: false
                            archiveArtifacts artifacts: 'build/reports/**', fingerprint: true, onlyIfSuccessful: false
                        }
                    }
                }
            }
        }
    }
}
```
