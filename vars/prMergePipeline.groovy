def call(Map config = [:]) {

    pipeline {
        agent any

        environment {
            // Auto-detect GitHub Org and Repo Name
            GIT_URL = sh(script: "git config --get remote.origin.url", returnStdout: true).trim()
            GITHUB_REPO = sh(script: "basename -s .git ${env.GIT_URL}", returnStdout: true).trim()
            GITHUB_ORG  = sh(script: "basename `dirname ${env.GIT_URL}`", returnStdout: true).trim()

            GITHUB_FULL = "${GITHUB_ORG}/${GITHUB_REPO}"
            GITHUB_API  = "https://api.github.com/repos/${GITHUB_FULL}"

            SONARQUBE_ENV      = "sonarqube"
            SONAR_SCANNER_HOME = "/var/lib/jenkins/sonar-scanner"
        }

        stages {

            stage("Detect Pull Request") {
                when { changeRequest() }
                steps {
                    echo "🔍 PR Detected: #${CHANGE_ID} (${CHANGE_BRANCH} → ${CHANGE_TARGET})"
                    echo "📦 Repository: ${env.GITHUB_FULL}"
                }
            }

            stage("Checkout PR") {
                when { changeRequest() }
                steps {
                    checkout scm
                }
            }

            stage("Fetch PR Metadata") {
                when { changeRequest() }
                steps {
                    script {
                        def pr = org.kb.utils.GitUtils.getPullRequestData(env.GITHUB_API, CHANGE_ID)
                        env.PR_TITLE = pr.title
                        env.PR_BODY  = pr.body
                    }
                }
            }

            stage("Detect Project Type") {
                steps {
                    script {
                        env.PROJECT_TYPE = org.kb.utils.GitUtils.detectProjectType()
                        echo "📦 Detected Project Type: ${env.PROJECT_TYPE}"
                    }
                }
            }

            stage("SonarQube Analysis") {
                when { changeRequest() }
                steps {
                    script {
                        org.kb.utils.SonarUtils.scan(env.PROJECT_TYPE, env.GITHUB_FULL)
                    }
                }
            }

            stage("Quality Gate") {
                when { changeRequest() }
                steps {
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }

            stage("Merge GitHub PR") {
                when { changeRequest() }
                steps {
                    script {
                        org.kb.utils.GitUtils.mergePullRequest(env.GITHUB_API, CHANGE_ID)
                        env.PR_MERGE_STATUS = "SUCCESS"
                    }
                }
            }

            stage("Detect Jira ID") {
                steps {
                    script {
                        env.JIRA_ID = org.kb.utils.JiraUtils.extractJiraId(env.PR_TITLE, env.PR_BODY)
                        echo env.JIRA_ID ? "📌 Jira ID: ${env.JIRA_ID}" : "ℹ No Jira ID found"
                    }
                }
            }

            stage("Update Jira Status") {
                when {
                    expression {
                        env.PR_MERGE_STATUS == "SUCCESS" &&
                        env.JIRA_ID?.trim()
                    }
                }
                steps {
                    script {
                        org.kb.utils.JiraUtils.updateJiraStatus(env.JIRA_ID, "91")
                    }
                }
            }
        }

        post {
            success {
                script {
                    def buildJob = config.buildJob ?: "Build/${env.GITHUB_ORG}/${env.GITHUB_REPO}"

                    echo "🚀 Triggering service build → ${buildJob}"

                    build job: buildJob,
                        wait: false,
                        parameters: [
                            string(name: 'github_release_branch', value: env.CHANGE_TARGET),
                            string(name: 'jira_key', value: env.JIRA_ID ?: ""),
                            string(name: 'pr_number', value: env.CHANGE_ID)
                        ]
                }
            }

            failure {
                script {
                    org.kb.utils.NotificationUtils.sendTeamsFailure(
                        env.GITHUB_FULL,
                        CHANGE_ID,
                        env.CHANGE_BRANCH,
                        env.CHANGE_TARGET,
                        env.JIRA_ID
                    )
                }
            }

            always {
                echo "🏁 Completed"
            }
        }
    }
}

