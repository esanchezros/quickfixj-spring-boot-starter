# Security Policy

## Supported versions

| Version | Support |
| --- | --- |
| 4.x | Active security support |
| 3.x | Maintenance security support |
| 1.x and 2.x | End of life |

See the [support policy](docs/support-policy.adoc) for details.

## Reporting a vulnerability

Do not report a suspected vulnerability in a public GitHub Issue.

Send the report to `esanchezros@yahoo.es` with the subject `QuickFIX/J Spring Boot security report`. Include:

- The affected version and configuration.
- Reproduction steps or a minimal reproducer.
- The expected and observed impact.
- Any suggested mitigation.
- Whether the issue is already public or has been reported elsewhere.

You should receive an acknowledgement within seven days. The maintainer will investigate, agree on a disclosure plan, and coordinate a fix when the report is confirmed.

Please avoid accessing data that is not yours, disrupting production systems, or publishing the issue before a fix and disclosure plan are available.

## Security fixes

Confirmed vulnerabilities are fixed on supported lines when affected. Releases will include appropriate credit unless the reporter requests anonymity. Published Maven artifacts are immutable, so a corrected patch release will replace a vulnerable release in the recommended-version documentation.
