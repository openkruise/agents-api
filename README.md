# Agents-api
The canonical location of the Kruise Agents API definition.

## Purpose

This library is the canonical location of the Kruise Agents API definition and client.

We recommend using the go types in this repo. You may serialize them directly to JSON.

## What's included
* The `client` package contains the go clientset to access Kruise Agents API.
* The `agents` packages contain api definition in go
* The `clients` contain clients for other languages including Python and Java

## Versioning
For each `v0.x.y` Kruise Agents release, the corresponding agents-api will `v0.x.z`.

Bugfixes in agents-api will result in the patch version (third digit `z`) changing. PRs that are cherry-picked into an older Kruise Agents release branch will result in an update to the corresponding branch in client-go, with a corresponding new tag changing the patch version.

## Where does it come from?

`agents-api` is synced from [https://github.com/openkruise/agents/tree/master/api](https://github.com/openkruise/agents/tree/master/api).
Code changes are made in that location, merged into `openkruise/agents` and later synced here.


### How to get it

To get the latest version, use go1.16+ and fetch using the `go get` command. For example:

```
go get github.com/openkruise/agents-api@latest
```

To get a specific version, use go1.11+ and fetch the desired version using the `go get` command. For example:

```
go get github.com/openkruise/agents-api@v0.1.0
```

### How to use it

please refer to the [example](examples/sandboxclaim-example)


## Things you should NOT do

[https://github.com/openkruise/agents/tree/master/api](https://github.com/openkruise/agents/tree/master/api) is synced to here.
All changes must be made in the former. The latter is read-only.
