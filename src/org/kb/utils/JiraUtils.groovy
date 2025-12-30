package org.kb.utils

class JiraUtils {

    static def extractJiraId(title, body) {
        def pattern = ~/^(KB-\d+)/
        if (title?.trim() ==~ pattern) return title.split(" ")[0]
        if (body?.trim() ==~ pattern) return body.split(" ")[0]
        return ""
    }

    static def updateJiraStatus(script, jiraId, transitionId) {
        script.withCredentials([script.usernamePassword(
            credentialsId: 'JIRA_CREDS',
            usernameVariable: 'JIRA_USER',
            passwordVariable: 'JIRA_TOKEN'
        )]) {
            script.sh """
                curl -X POST \
                  -u $JIRA_USER:$JIRA_TOKEN \
                  -H "Content-Type: application/json" \
                  -d '{"transition":{"id":"${transitionId}"}}' \
                  https://karmayogibharat.atlassian.net/rest/api/3/issue/${jiraId}/transitions
            """
        }
    }
}

