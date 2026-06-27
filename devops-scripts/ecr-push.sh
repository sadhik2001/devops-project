#!/usr/bin/env bash
# ecr-push.sh – Build, tag, scan, and push Docker image to ECR
# Usage: ./ecr-push.sh <image-tag> [aws-region] [aws-account-id]
set -euo pipefail

IMAGE_TAG="${1:-latest}"
AWS_REGION="${2:-ap-south-1}"
AWS_ACCOUNT_ID="${3:-$(aws sts get-caller-identity --query Account --output text)}"
REPO_NAME="payg-payment-gateway"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
FULL_IMAGE="${ECR_REGISTRY}/${REPO_NAME}:${IMAGE_TAG}"

echo " Authenticating to ECR..."
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${ECR_REGISTRY}"

echo " Building image: ${FULL_IMAGE}"
docker build \
  --build-arg BUILD_DATE="$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --build-arg GIT_COMMIT="$(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')" \
  -t "${FULL_IMAGE}" \
  -f application/Dockerfile \
  application/

echo " Running Trivy vulnerability scan..."
trivy image \
  --exit-code 1 \
  --severity HIGH,CRITICAL \
  --ignore-unfixed \
  "${FULL_IMAGE}" || {
    echo " Trivy found HIGH/CRITICAL vulnerabilities. Aborting push."
    exit 1
  }

echo " Pushing image to ECR..."
docker push "${FULL_IMAGE}"

# Also tag as 'latest'
docker tag "${FULL_IMAGE}" "${ECR_REGISTRY}/${REPO_NAME}:latest"
docker push "${ECR_REGISTRY}/${REPO_NAME}:latest"

echo " Successfully pushed: ${FULL_IMAGE}"
echo " Image digest: $(docker inspect --format='{{index .RepoDigests 0}}' "${FULL_IMAGE}")"
