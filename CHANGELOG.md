# Changelog

All notable changes to this project will be documented in this file. The target-audience for this document
is developers and operations.

The changelog-format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Developers should add an entry to "Unreleased work" under the appropriate subsection, describing their work
_prior_ to merging to master. The entry should contain a link to the Jira-story.

Adhere to the following format:
```
- [Name of Jira-story or subtask](link to Jira-story): Description of the completed work`
```
Example-entry:

- [TESFOUND-146](https://sunepoulsen.atlassian.net/browse/TESFOUND-146): Create new feature

For release-dates, use date-format: YYYY-MM-DD

## Unreleased work

### Features

- [TESFOUND-146](https://sunepoulsen.atlassian.net/browse/TESFOUND-146): Create new feature
  
  1. Added new endpoint to register a new feature with this backend service.

### Fixed

### Security

- [TESFOUND-146](https://sunepoulsen.atlassian.net/browse/TESFOUND-146): Create new feature

  1. Update dependencies to address vulnerabilities.
  
### DevOps

- [TESFOUND-146](https://sunepoulsen.atlassian.net/browse/TESFOUND-146): Create new feature
  
  1. Add integration with `SonarQube` for code analysis.
  2. Add integration with `OWASP Dependency Check` for checks of dependency vulnerabilities.
