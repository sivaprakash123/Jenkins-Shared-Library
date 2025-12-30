package org.kb.utils

import groovy.json.JsonSlurper

class GitUtils {

    static def getPullRequestData(script, repo, prId) {
        script.withCredentials([script.string(
            credentialsId: 'gerrit-github-token',
            variable: 'GITHUB_TOKEN'
        )]) {
            def out = script.sh(
                script: """
                    curl -s \
                      -H "Authorization: Bearer $GITHUB_TOKEN" \
                      https://api.github.com/repos/${repo}/pulls/${prId}
                """,
                returnStdout: true
            )
            return new JsonSlurper().parseText(out)
        }
    }

    static def detectProjectType(script) {
        if (script.fileExists("pom.xml")) return "java"
        if (script.fileExists("package.json")) return "node"
        return "unknown"
    }

    static def mergePullRequest(script, repo, prId) {
        script.withCredentials([script.string(
            credentialsId: 'gerrit-github-token',
            variable: 'GITHUB_TOKEN'
        )]) {
            def code = script.sh(
                script: """
                    curl -s -o /dev/null -w "%{http_code}" \
                      -X PUT \
                      -H "Authorization: Bearer $GITHUB_TOKEN" \
                      -d '{"merge_method":"squash"}' \
                      https://api.github.com/repos/${repo}/pulls/${prId}/merge
                """,
                returnStdout: true
            ).trim()

            if (code != "200") {
                script.error("GitHub merge failed (HTTP ${code})")
            }
        }
    }
}

