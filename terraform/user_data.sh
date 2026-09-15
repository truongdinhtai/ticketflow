#!/bin/bash
set -euo pipefail

# Single-node Kubernetes (k3s). Lightweight, runs the same manifests as minikube/EKS.
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--write-kubeconfig-mode 644" sh -

# AWS CLI — used to mint an ECR pull secret for Kubernetes (refresh via cron; the
# EC2 instance profile grants ECR read, so no static keys are needed).
apt-get update -y
apt-get install -y unzip
curl -s "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscli.zip
cd /tmp && unzip -q awscli.zip && ./aws/install

# kubeconfig for the ubuntu user
mkdir -p /home/ubuntu/.kube
cp /etc/rancher/k3s/k3s.yaml /home/ubuntu/.kube/config
chown -R ubuntu:ubuntu /home/ubuntu/.kube

# k3s already ships an nginx-like ingress (Traefik). If you prefer ingress-nginx,
# install it and set ingressClassName accordingly.
