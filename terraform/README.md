# Terraform — AWS infrastructure

Provisions the minimal AWS footprint to run TicketFlow: a VPC with two public
subnets, security groups, one **ECR** repo per service, an **RDS PostgreSQL**
(`db.t3.micro`, free-tier eligible), and one **EC2** instance running **k3s**
(single-node Kubernetes). The same `k8s/` manifests then deploy onto it.

## Apply

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars   # set ssh_public_key, allowed_ssh_cidr
export TF_VAR_db_password='a-strong-password'   # never commit this
terraform init
terraform plan
terraform apply
terraform output                                # app_url, rds_endpoint, ecr_registry, ...
```

## After apply

1. **Extra databases** (one RDS instance hosts all four DBs):
   ```bash
   psql -h <rds_endpoint> -U ticketflow -d event_db \
     -c "CREATE DATABASE booking_db;" -c "CREATE DATABASE notification_db;" -c "CREATE DATABASE auth_db;"
   ```
2. **kubeconfig**: `scp ubuntu@<ec2_ip>:~/.kube/config ./kubeconfig`, then edit the
   `server:` field to `https://<ec2_ip>:6443` and `export KUBECONFIG=$PWD/kubeconfig`.
3. **ECR pull secret** on the node (instance profile already grants ECR read):
   ```bash
   kubectl -n ticketflow create secret docker-registry ecr \
     --docker-server="<ecr_registry>" \
     --docker-username=AWS \
     --docker-password="$(aws ecr get-login-password --region <region>)"
   ```
   Add `imagePullSecrets: [{name: ecr}]` to the deployments (or a cron to refresh the 12h token).
4. **Deploy**: point the ConfigMap `*_DB_URL` at `<rds_endpoint>`, set the kustomize
   images to your ECR registry, then `kubectl apply -k k8s/`.
5. **GitHub secrets/vars** for CI/CD: `AWS_DEPLOY_ROLE_ARN`, `ECR_REGISTRY`,
   `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY`; variable `AWS_REGION`.

## Destroy (stop paying)

```bash
terraform destroy
```

> The IaC is designed for **apply → demo → destroy**: bring it up for a demo or a
> screen-recording, then tear it down so idle cost is ~$0. See the cost breakdown
> in [../INFRASTRUCTURE.md](../INFRASTRUCTURE.md).
