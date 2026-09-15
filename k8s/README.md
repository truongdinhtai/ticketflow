# Kubernetes manifests

Deployment / Service / ConfigMap / Secret for every service, plus an **Ingress**
(single public entry point) and an **HPA** for Booking Service. The same
manifests run on **minikube** (local, free) and on a real cluster (EKS / k3s).

## Run on minikube (local, free)

```bash
# 0. Build the images (from the repo root)
docker compose build

# 1. Start minikube and enable the addons the manifests use
minikube start --memory=8192 --cpus=4
minikube addons enable ingress            # nginx ingress controller
minikube addons enable metrics-server     # required by the HPA

# 2. Make the locally-built images available to the cluster
for s in config-server discovery-server api-gateway auth-service \
         event-service booking-service notification-service frontend; do
  minikube image load ticketflow/$s:latest
done

# 3. Config Server serves backend/config-repo via this ConfigMap
kubectl create namespace ticketflow --dry-run=client -o yaml | kubectl apply -f -
kubectl -n ticketflow create configmap config-repo --from-file=backend/config-repo/

# 4. Apply everything
kubectl apply -k k8s/

# 5. Watch it come up (config-server -> eureka -> services)
kubectl -n ticketflow get pods -w

# 6. Open it
minikube tunnel        # in a second terminal, then browse the Ingress:
kubectl -n ticketflow get ingress
```

Check the HPA:
```bash
kubectl -n ticketflow get hpa
kubectl -n ticketflow describe hpa booking-service
```

## On AWS (EKS) or EC2 + k3s

Same manifests. Two changes:
1. **Images** — point `kustomization.yaml` image `newName` at your ECR registry;
   CI sets the tag per commit SHA (`kubectl set image`).
2. **Database** — delete the in-cluster `postgres` (in `02-datastores.yaml`) and
   change the `*_DB_URL` entries in `01-namespace-config.yaml` to the RDS endpoint.
   Put real values in the Secret via `kubectl create secret` / SealedSecrets /
   External Secrets Operator — never commit them.

## Notes
- Zipkin tracing is omitted here to keep the cluster small; services log a
  harmless warning. Add a `zipkin` Deployment/Service and set `ZIPKIN_ENDPOINT`
  in the ConfigMap to re-enable.
- In-cluster Postgres/Kafka/Redis are single-replica (demo). Production would use
  managed services (RDS, MSK/Confluent, ElastiCache) or StatefulSets with proper
  storage and replication.
