package org.kb.utils

import org.yaml.snakeyaml.Yaml

class GitUtils implements Serializable {

    static String resolveBuildJob(String org, String repo, def steps) {

        def fullRepo = "${org}/${repo}"
        def defaultJob = "Build/${org}/${repo}"

        try {
            def yamlText = steps.libraryResource('repo-build-map.yaml')
            def yaml = new Yaml().load(yamlText)

            if (yaml?.containsKey(fullRepo)) {
                steps.echo "🔁 Using mapped build job for ${fullRepo}"
                return yaml[fullRepo]
            }

        } catch (Exception e) {
            steps.echo "⚠️ Repo-job mapping not found or failed to load, using default"
        }

        steps.echo "ℹ️ Using default build job: ${defaultJob}"
        return defaultJob
    }
}

