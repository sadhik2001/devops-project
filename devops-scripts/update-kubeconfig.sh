#!/usr/bin/env bash
# update-kubeconfig.sh – Update kubeconfig for a given EKS cluster
set -euo pipefail

CLUSTER_NAME="${1:-payg-eks-dev}"
AWS_REGION="${2:-ap-south-1}"

echo " Updating kubeconfig for cluster: ${CLUSTER_NAME}"
aws eks update-kubeconfig \
  --name "${CLUSTER_NAME}" \
  --region "${AWS_REGION}" \
  --alias "${CLUSTER_NAME}"

echo " kubeconfig updated. Current context:"
kubectl config current-context
kubectl get nodes
