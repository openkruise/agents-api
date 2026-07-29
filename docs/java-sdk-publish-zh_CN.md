# Maven 中央仓库发布配置指南

本文档说明如何通过 GitHub Actions 自动将 Maven 构件推送到 Maven Central 中央仓库，包括密钥配置、Token 生成和 GPG 签名的完整流程。

## 项目结构

本项目包含三个独立的 Java 包，每个包都有对应的 GitHub Actions Workflow 负责发布：

| Java 包路径                          | 说明                                       | Workflow 文件                                                                     |
|-----------------------------------|------------------------------------------|---------------------------------------------------------------------------------|
| [`e2b/java`](../e2b/java)         | E2B Client — E2B Sandbox API 客户端         | [`publish-e2b-client.yml`](../.github/workflows/publish-e2b-client.yml)         |
| [`k8s/java`](../k8s/java)         | K8s Client — Kubernetes CRD 模型客户端        | [`publish-k8s-client.yml`](../.github/workflows/publish-k8s-client.yml)         |
| [`runtime/java`](../runtime/java) | Runtime Client — Sandbox Runtime API 客户端 | [`publish-runtime-client.yml`](../.github/workflows/publish-runtime-client.yml) |

每个 Workflow 均为手动触发，需要指定版本号。

## 前置条件

- 拥有 [Sonatype 中央仓库账号](https://central.sonatype.com)
- 本地已安装 GPG 工具（`gpg --version` 验证）

## 配置步骤

### 1. 生成 Sonatype Token

1. 访问 [https://central.sonatype.com/usertoken](https://central.sonatype.com/usertoken)
2. 使用 GitHub 账号登录
3. 点击 **Generate User Token** 生成 Token
4. 记录生成的 `username` 和 `password`（后续配置到 GitHub Secrets）

### 2. 生成 GPG 密钥

```bash
gpg --full-generate-key
```

按提示依次输入：

- **Key type**: 输入 `1` (RSA and RSA)
- **Keysize**: 输入 `4096`（安全性更高）
- **Expiration**: 输入 `0`（永不过期）或 `1y`（有效期一年）
- **Real name**: 输入你的中文名或英文名
- **Email address**: 必须填写你的公司邮箱 `zq01297892@alibaba-inc.com`，这决定了能否自动识别你的签名
- **Passphrase**: 设置一个复杂的密码（以后每次提交代码都会通过它来解锁密钥）

> ⚠️ **注意**：这个 Passphrase 密码需要设置到 `GPG_PASSPHRASE` 变量中，请妥善保管。

生成成功后会输出类似以下内容，保存其中的 **Key ID**（如 `***`）：

```
gpg:  ***
pub   rsa4096 2026-04-07 [SC]
      ***
uid                      agents-client-java (Maven Central Signing) <***@alibaba-inc.com>
sub   rsa4096 2026-04-07 [E]
```

### 3. 导出 GPG 私钥

使用上一步记录的 Key ID 导出私钥，将完整输出保存到 `GPG_PRIVATE_KEY`：

```bash
gpg --armor --export-secret-key <KEY-ID>
# 示例：
# gpg --armor --export-secret-key ***
```

输出以 `-----BEGIN PGP PRIVATE KEY BLOCK-----` 开头，以 `-----END PGP PRIVATE KEY BLOCK-----` 结尾，需完整复制。

### 4. 上传 GPG 公钥到密钥服务器

将公钥上传到公共密钥服务器，以便 Maven Central 验证签名：

```bash
gpg --keyserver hkps://keyserver.ubuntu.com --send-keys <KEY-ID>
# 示例：
# gpg --keyserver hkps://keyserver.ubuntu.com --send-keys ***
```

> ⚠️ **注意**：公钥上传后可能需要几分钟到几小时才能在密钥服务器上生效，如签名验证失败请稍后重试。

上传后可在 [Ubuntu Keyserver](https://keyserver.ubuntu.com/) 搜索 Key ID 验证是否生效。

### 5. 配置 GitHub Secrets

将以下 4 个变量设置到 GitHub 仓库的 **Settings → Secrets and variables → Actions** 中：

| 变量名                | 说明                 | 获取方式           |
|--------------------|--------------------|----------------|
| `CENTRAL_USERNAME` | Sonatype Token 用户名 | 生成的 username   |
| `CENTRAL_PASSWORD` | Sonatype Token 密码  | 生成的 password   |
| `GPG_PASSPHRASE`   | GPG 密钥登录密码         | 设置的 Passphrase |
| `GPG_PRIVATE_KEY`  | GPG 私钥（ASCII 格式）   | 导出的完整内容        |

## 发布流程

1. 进入 GitHub 仓库的 **Actions** 页面
2. 选择要发布的 Workflow（如 `Publish E2B Client to Maven Central`）
3. 点击 **Run workflow** 按钮
4. 输入版本号（如 `0.1.0`），点击 **Run workflow**
5. 等待 Workflow 执行完成
6. 发布成功后，可在 Maven Central 搜索对应的 artifact

**发布后验证**：

访问 [Sonatype Publishing Deployments](https://central.sonatype.com/publishing/deployments) 查看发布状态（可能需要几分钟到几小时同步）。

## 注意事项

- **Namespace 与 GroupId 一致**：Sonatype Central 账号拥有的 Namespace 必须与 `pom.xml` 中的 `groupId` 一致（如
  `io.github.openkruise`），否则发布会被拒绝
- **GPG Email 与 Developer Email 一致**：GPG 密钥的 Email 必须与 `pom.xml` 中 `<developers>` 的 `<email>` 一致，否则签名验证会失败
- **私钥安全**：`GPG_PRIVATE_KEY` 是敏感信息，务必仅存储在 GitHub Secrets 中，不要提交到代码仓库
- **Passphrase 保管**：`GPG_PASSPHRASE` 丢失后无法恢复，建议妥善保存
- **Token 有效期**：Sonatype Token 长期有效，但如果泄露应立即在 [central.sonatype.com](https://central.sonatype.com)
  重新生成并更新 GitHub Secrets

## 版本演进

### 使用版本（单一包依赖）

```xml

<dependency>
    <groupId>io.github.zhaoqing7892</groupId>
    <artifactId>agents-client-java</artifactId>
    <version>最新版本</version>
</dependency>
```

**Maven Central
**: [https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-java](https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-java)

### 最新版本

**配置优化**：

1. 目录功能拆分为 `e2b`、`runtime`、`k8s` 三个独立包
2. 配置文件移除了代理配置
3. 新增了 `httpClient` 参数，支持自定义 HTTP 客户端

### Maven Central 链接

- **agents-client-e2b
  **: [https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-e2b](https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-e2b)
- **agents-client-runtime
  **: [https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-runtime](https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-runtime)
- **agents-client-k8s
  **: [https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-k8s](https://central.sonatype.com/artifact/io.github.zhaoqing7892/agents-client-k8s)
