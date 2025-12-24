package org.kb.utils

import groovy.json.JsonSlurper

class GitUtils {

    static def getPullRequestData(apiUrl, prId) {
        def token = Jenkins.instance.getDescriptorByType(
            org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl.DescriptorImpl
        )

        def out = sh(
            script: """
                curl -s ${apiUrl}/pulls/${prId}
            """,
            returnStdout: true
        )

        return new JsonSlurper().parseText(out)
    }

    static def detectProjectType() {
        if (fileExists("pom.xml")) return "java"
        if (fileExists("package.json")) return "node"
        return "unknown"
    }

    static def mergePullRequest(apiUrl, prId) {
        sh """
            curl -s -X PUT \
              -H "Authorization: Bearer ${GITHUB_TOKEN}" \
              -d '{"merge_method":"squash"}' \
              ${apiUrl}/pulls/${prId}/merge
        """
    }
}

