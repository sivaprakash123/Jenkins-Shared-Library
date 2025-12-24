package org.kb.utils

class SonarUtils {

    static def scan(projectType, projectKey) {

        if (projectType == "java") {
            sh "mvn -DskipTests sonar:sonar"
            return
        }

        if (projectType == "node") {
            sh """
                ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                  -Dsonar.projectKey=${projectKey.replaceAll('/', '_')} \
                  -Dsonar.sources=. \
                  -Dsonar.host.url=$SONAR_HOST_URL \
                  -Dsonar.token=$SONAR_AUTH_TOKEN
            """
            return
        }

        echo "⚠️ Unknown project type, skipping scan"
    }
}

