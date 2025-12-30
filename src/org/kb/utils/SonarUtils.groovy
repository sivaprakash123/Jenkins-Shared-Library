package org.kb.utils

class SonarUtils {

    static def scan(script, projectType, projectKey) {

        if (projectType == "java") {
            script.sh "mvn -DskipTests sonar:sonar"
            return
        }

        if (projectType == "node") {
            script.withSonarQubeEnv("sonarqube") {
                script.sh """
                    sonar-scanner \
                      -Dsonar.projectKey=${projectKey.replaceAll('/', '_')} \
                      -Dsonar.sources=. \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.token=$SONAR_AUTH_TOKEN
                """
            }
            return
        }

        script.echo "⚠️ Unknown project type, skipping Sonar scan"
    }
}

