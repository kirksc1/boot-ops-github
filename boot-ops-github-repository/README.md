# BootOps GitHub Repository
The boot-ops-github-repository project provides an implementation
for managing GitHub repositories through a GitOps process
orchestrated by the BootOps platform.

## GitHub Authentication
The converge process uses a GitHub bearer token provided as an environment
variable named GITHUB_TOKEN.  This can be changed through Spring Boot application
configuration (see Altering GitHub Service Defaults).

The token used must be granted the authority to manage repositories, including:
- **'public_repo'** required for managing public repositories.
- **'repo'** required for managing private repositories.

## GitHub User Repositories
GitHub user repositories can be managed by providing details within a
'github-user-repository' attribute.

### Sample Item Manifest
```yaml
name: my-item
attributes:
  github-user-repository:
    name: my-repo                   //the name of the repo
    description: It's my-repo!      //the description for the repo
    is-private: true                //indicate whether the repo will be private
```

## Altering GitHub Service Defaults
The GitHub service defaults can be altered from their default values (see below)
through Spring Boot application configuration.

### Sample section of Spring Boot's application.yaml 
```yaml
github.service:
  base-url: 'https://api.github.com'
  accept: 'application/vnd.github+json'
  token-environment-variable-name: 'GITHUB_TOKEN'
```