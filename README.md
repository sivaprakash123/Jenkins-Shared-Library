# Centralized Jenkins PR Merge Pipeline (Shared Library)

This library provides a unified PR workflow for all GitHub repositories:

✔ PR validation  
✔ SonarQube scanning (Java/Node auto-detect)  
✔ Quality gate  
✔ Auto-merge on success  
✔ Jira ticket update  
✔ MS Teams notification on failure  
✔ Trigger service build automatically  

## Usage

In each repository’s Jenkinsfile:

```groovy
@Library('central-pipeline-lib') _
prMergePipeline()

