# Maven Central Publishing Guide

This document describes how to automatically publish Maven artifacts to Maven Central via GitHub Actions, including the
complete workflow for secret configuration, token generation, and GPG signing.

## Project Structure

This project contains three independent Java packages, each with a corresponding GitHub Actions Workflow for publishing:

| Java Package Path                 | Description                                 | Workflow File                                                                   |
|-----------------------------------|---------------------------------------------|---------------------------------------------------------------------------------|
| [`e2b/java`](../e2b/java)         | E2B Client — E2B Sandbox API client         | [`publish-e2b-client.yml`](../.github/workflows/publish-e2b-client.yml)         |
| [`k8s/java`](../k8s/java)         | K8s Client — Kubernetes CRD model client    | [`publish-k8s-client.yml`](../.github/workflows/publish-k8s-client.yml)         |
| [`runtime/java`](../runtime/java) | Runtime Client — Sandbox Runtime API client | [`publish-runtime-client.yml`](../.github/workflows/publish-runtime-client.yml) |

Each Workflow is manually triggered and requires a version number to be specified.

## Prerequisites

- A [Sonatype Central account](https://central.sonatype.com)
- GPG tool installed locally (verify with `gpg --version`)

## Configuration Steps

### 1. Generate Sonatype Token

1. Visit [https://central.sonatype.com/usertoken](https://central.sonatype.com/usertoken)
2. Log in with your GitHub account
3. Click **Generate User Token**
4. Record the generated `username` and `password` (to be configured in GitHub Secrets later)

### 2. Generate GPG Key

```bash
gpg --full-generate-key
```

Follow the prompts:

- **Key type**: Enter `1` (RSA and RSA)
- **Keysize**: Enter `4096` (more secure)
- **Expiration**: Enter `0` (never expires) or `1y` (one year validity)
- **Real name**: Enter your name
- **Email address**: Must use your company email `zq01297892@alibaba-inc.com`, as this determines whether your signature
  can be automatically recognized
- **Passphrase**: Set a strong password (used to unlock the key for each code submission)

> ⚠️ **Note**: This Passphrase must be set in the `GPG_PASSPHRASE` variable. Please keep it safe.

After successful generation, output similar to the following will be displayed. Save the **Key ID** (e.g.,
`***`):

```
gpg:  ***
pub   rsa4096 2026-04-07 [SC]
      ***
uid                      agents-client-java (Maven Central Signing) <***@alibaba-inc.com>
sub   rsa4096 2026-04-07 [E]
```

### 3. Export GPG Private Key

Use the Key ID from the previous step to export the private key. Save the complete output to `GPG_PRIVATE_KEY`:

```bash
gpg --armor --export-secret-key <KEY-ID>
# Example:
# gpg --armor --export-secret-key ***
```

The output starts with `-----BEGIN PGP PRIVATE KEY BLOCK-----` and ends with `-----END PGP PRIVATE KEY BLOCK-----`. Copy
the entire content.

### 4. Upload GPG Public Key to Key Server

Upload the public key to a public key server so Maven Central can verify signatures:

```bash
gpg --keyserver hkps://keyserver.ubuntu.com --send-keys <KEY-ID>
# Example:
# gpg --keyserver hkps://keyserver.ubuntu.com --send-keys ***
```

> ⚠️ **Note**: It may take a few minutes to several hours for the public key to propagate on the key server. If
> signature verification fails, please retry later.

After uploading, you can search for the Key ID on [Ubuntu Keyserver](https://keyserver.ubuntu.com/) to verify it has
taken effect.

### 5. Configure GitHub Secrets

Set the following 4 variables in the GitHub repository under **Settings → Secrets and variables → Actions**:

| Variable Name      | Description             | Source                 |
|--------------------|-------------------------|------------------------|
| `CENTRAL_USERNAME` | Sonatype Token username | Generated username     |
| `CENTRAL_PASSWORD` | Sonatype Token password | Generated password     |
| `GPG_PASSPHRASE`   | GPG key passphrase      | The Passphrase you set |
| `GPG_PRIVATE_KEY`  | GPG private key (ASCII) | Full exported content  |

## Publishing Workflow

1. Go to the **Actions** page of the GitHub repository
2. Select the Workflow to publish (e.g., `Publish E2B Client to Maven Central`)
3. Click the **Run workflow** button
4. Enter the version number (e.g., `0.1.0`) and click **Run workflow**
5. Wait for the Workflow to complete
6. After successful publishing, search for the artifact on Maven Central

**Post-publish Verification**:

Visit [Sonatype Publishing Deployments](https://central.sonatype.com/publishing/deployments) to check the publishing
status (may take a few minutes to several hours to sync).

## Important Notes

- **Namespace must match GroupId**: The Namespace owned by your Sonatype Central account must match the `groupId` in
  `pom.xml` (e.g., `io.openkruise`), otherwise publishing will be rejected
- **GPG Email must match Developer Email**: The Email in the GPG key must match the `<email>` in `<developers>` section
  of `pom.xml`, otherwise signature verification will fail
- **Private Key Security**: `GPG_PRIVATE_KEY` is sensitive information — store it only in GitHub Secrets, never commit
  it to the code repository
- **Passphrase Safekeeping**: `GPG_PASSPHRASE` cannot be recovered if lost — keep it safe
- **Token Validity**: Sonatype Tokens are valid indefinitely, but if compromised, regenerate immediately
  at [central.sonatype.com](https://central.sonatype.com) and update GitHub Secrets

## Version History

### Legacy Version (Single Package)

```xml

<dependency>
    <groupId>io.openkruise</groupId>
    <artifactId>agents-client-java</artifactId>
    <version>latest-version</version>
</dependency>
```

**Maven Central
**: [https://central.sonatype.com/artifact/io.openkruise/agents-client-java](https://central.sonatype.com/artifact/io.openkruise/agents-client-java)

### Latest Version

**Configuration Improvements**:

1. Split into three independent packages: `e2b`, `runtime`, `k8s`
2. Removed proxy configuration from config files
3. Added `httpClient` parameter for custom HTTP client support

### Maven Central Links

- **agents-client-e2b
  **: [https://central.sonatype.com/artifact/io.openkruise/agents-client-e2b](https://central.sonatype.com/artifact/io.openkruise/agents-client-e2b)
- **agents-client-runtime
  **: [https://central.sonatype.com/artifact/io.openkruise/agents-client-runtime](https://central.sonatype.com/artifact/io.openkruise/agents-client-runtime)
- **agents-client-k8s
  **: [https://central.sonatype.com/artifact/io.openkruise/agents-client-k8s](https://central.sonatype.com/artifact/io.openkruise/agents-client-k8s)
