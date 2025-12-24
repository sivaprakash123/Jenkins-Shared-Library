package org.kb.utils

class NotificationUtils {

    static def sendTeamsFailure(repo, pr, branch, target, jira) {

        withCredentials([string(credentialsId: 'TEAMS_WEBHOOK_URL', variable: 'TEAMS_URL')]) {

            def msg = """
            {
                "title": "🚨 Jenkins Pipeline Failed",
                "text": "**Repository:** ${repo}\n\n**PR:** #${pr}\n\n**Branch:** ${branch} → ${target}\n\n**Jira:** ${jira ?: "N/A"}"
            }
            """

            sh """
                curl -H 'Content-Type: application/json' \
                     -X POST \
                     -d '${msg}' \
                     $TEAMS_URL
            """
        }
    }
}

