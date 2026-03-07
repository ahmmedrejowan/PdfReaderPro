# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 2.x.x   | :white_check_mark: |
| 1.x.x   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability in PDF Reader Pro, please report it responsibly:

1. **Do NOT** open a public issue
2. Email the maintainer directly or use GitHub's private vulnerability reporting
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

## Security Measures

PDF Reader Pro implements the following security measures:

- **No network access** except for optional update checks to GitHub
- **Encrypted password storage** using Android Keystore and DataStore
- **FileProvider** for secure file sharing between apps
- **ProGuard/R8** obfuscation in release builds
- **No analytics or tracking**
- **No third-party SDKs** that collect user data

## Response Timeline

- **Acknowledgment**: Within a week
- **Initial Assessment**: Within 2 weeks
- **Fix Timeline**: Depends on severity and availability
  - Critical: As soon as possible
  - Others: Next release

## Scope

The following are in scope for security reports:

- Remote code execution
- Data leakage
- Authentication/authorization bypass
- Privilege escalation
- Denial of service

Out of scope:

- Issues requiring physical device access
- Social engineering attacks
- Issues in third-party libraries (report to respective maintainers)
