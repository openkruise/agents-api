# Agents-api

The canonical location of the Kruise Agents API definition.

## Purpose

This library is the canonical location of the Kruise Agents API definition and client.

We recommend using the go types in this repo. You may serialize them directly to JSON.

## What's included

* The `client` package contain the go clientset to access Kruise Agents API.
* The `agents` packages contain api definition in go
* The `k8s` contain k8s clients in other languages including Python and Java
* The `e2b` contain e2b clients in Golang
* The `runtime` contain golang clients to access agent-runtime directly (command and file operations)

## Documentation

| Document                  | English                                         | Chinese                                                     |
|---------------------------|-------------------------------------------------|-------------------------------------------------------------|
| K8s SDK Code Generation   | [k8s-sdk-codegen.md](docs/k8s-sdk-codegen.md)   | [k8s-sdk-codegen-zh_CN.md](docs/k8s-sdk-codegen-zh_CN.md)   |
| E2B SDK Code Generation   | [e2b-sdk-codegen.md](docs/e2b-sdk-codegen.md)   | [e2b-sdk-codegen-zh_CN.md](docs/e2b-sdk-codegen-zh_CN.md)   |
| Java SDK Maven Publishing | [java-sdk-publish.md](docs/java-sdk-publish.md) | [java-sdk-publish-zh_CN.md](docs/java-sdk-publish-zh_CN.md) |

## Versioning

For each `v0.x.y` Kruise Agents release, the corresponding agents-api will `v0.x.z`.

Bugfixes in agents-api will result in the patch version (third digit `z`) changing. PRs that are cherry-picked into an
older Kruise Agents release branch will result in an update to the corresponding branch in client-go, with a
corresponding new tag changing the patch version.

## Where does it come from?

`agents-api` is synced
from [https://github.com/openkruise/agents/tree/master/api](https://github.com/openkruise/agents/tree/master/api).
Code changes are made in that location, merged into `openkruise/agents` and later synced here.

### How to get it

To get the latest version, use go1.16+ and fetch using the `go get` command. For example:

```
go get github.com/openkruise/agents-api@latest
```

To get a specific version, use go1.11+ and fetch the desired version using the `go get` command. For example:

```
go get github.com/openkruise/agents-api@v0.3.0
```

### How to use it

please refer to the [example](examples/sandboxclaim-example)

## Things you should NOT do

[https://github.com/openkruise/agents/tree/master/api](https://github.com/openkruise/agents/tree/master/api) is synced
to here.
All changes must be made in the former. The latter is read-only.
