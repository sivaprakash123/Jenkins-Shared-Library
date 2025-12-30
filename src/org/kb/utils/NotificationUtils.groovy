package org.kb.utils

class NotificationUtils {

    static def sendTeamsFailure(script, repo, pr, branch, target, jira) {

        script.withCredentials([script.string(
            credentialsId: 'TEAMS_WEBHOOK_URL',
            variable: 'TEAMS_URL'
        )]) {

            def payload = """
            {
              "title": "🚨 Jenkins Pipeline Failed",
              "text": "**Repo:** ${repo}\\n\\n**PR:** #${pr}\\n\\n**Branch:** ${branch} → ${target}\\n\\n**Jira:** ${jira ?: "N/A"}"
            }
            """

            script.sh """
                curl -H 'Content-Type: application/json' \
                     -X POST \
                     -d '${payload}' \
                     $TEAMS_URL
            """
        }
    }
}

