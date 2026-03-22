# Create Sandbox Claim

This example program demonstrates the fundamental operations for creating sandbox using
[SandboxClaim][1] resources.

You can adopt the source code from this example to write programs that manage
other types of resources through the Kubernetes API.

## Running this example

1. Make sure you have a Kubernetes cluster with sandbox controller installed and `kubectl` is configured
```
kubectl create ns demo
```


2. Prepare a sandbox warm-pool using [SandboxSet][2] resources.
```
kubectl apply -f - << YAML
apiVersion: agents.kruise.io/v1alpha1
kind: SandboxSet
metadata:
  name: demo
  namespace: demo
spec:
  # Size of the warm pool, recommended to be slightly larger than estimated request burst
  replicas: 10
  # Content to be preserved during sandbox hibernation and wake-up
  persistentContents:
    - ip
  # Sandbox template, consistent with Sandbox CRD
  template:
    # Add metadata for the finally created Pod
    metadata:
      annotations:
        foo: bar
    # Final Pod Spec
    spec:
      containers:
        - name: nginx
          image: nginx:alpine
YAML
```
3. Compile this example on your workstation:

```
cd sandboxclaim-example
go build -o ./example
```

Now, run this application on your workstation with your local kubeconfig file:

```
./example
# or specify a kubeconfig file with flag
./example -kubeconfig=$HOME/.kube/config
```

Running this command will execute the following operations on your cluster:

1. **Create SandboxClaim:** This will create a claim for 2 sandbox. Verify with
   `kubectl get sandboxclaim`.
2. **Check SandboxClaim status:** This will poll the status of SandboxClaim until sandoxclaim completes.
3. **List Sandbox:** This will retrieve claimed sandboxes in the `demo`
   namespace and print their names.

You should see an output like the following:

```
Created sandboxclaim "demo8bjfh".
Listing sandboxes in namespace "demo":
 claimed sandboxes demo/demo-lp8mc
 claimed sandboxes demo/demo-sn5lx
```


## Cleanup

Successfully running this program will clean the created artifacts. If you
terminate the program without completing, you can clean up the created
deployment with:
```
    kubectl delete ns demo
```


## Troubleshooting

[1]: https://openkruise.io/kruiseagents/user-manuals/sandbox-claim#claiming-sandboxes-via-sandboxclaim
[2]: https://openkruise.io/kruiseagents/user-manuals/warmpool-management#creating-warm-pool-via-sandboxset