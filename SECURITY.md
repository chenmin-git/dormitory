# Security Policy

## Sensitive Configuration

Do not commit real secrets to this repository.

- `SPARK_API_PASSWORD` must be provided through environment variables or a local `.env` file.
- `JWT_SECRET` should be replaced with a long random value in production.
- Database usernames and passwords should be configured outside source control.
- Token-free HTTPS remotes are required for GitHub and Gitee publishing.

## Demo Data

The bundled SQL contains fake users, fake phone numbers, and demo dormitory data for local testing. Replace all demo data before using this system with real students.

## Reporting Issues

If you find a security issue, remove any private data from your report and contact the project maintainer privately before opening a public issue.
