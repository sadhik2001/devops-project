# Git Branching Strategy – PayG Plus Platform

## Overview

We follow a **simplified Gitflow** adapted for a CI/CD-first, GitOps-driven workflow. The strategy balances release control with delivery speed for a FinTech payment platform.

---

## Branch Structure

```
main            ← production-ready; triggers Jenkins deploy to production (manual Argo CD sync)
develop         ← integration branch; auto-deploys to dev via Argo CD
feature/*       ← individual features (short-lived)
release/*       ← release candidates (hardening only)
hotfix/*        ← emergency production fixes
```

---

## Branch Rules (enforced via GitHub Branch Protection)

### `main`
- ✅ Requires PR with **minimum 2 reviewers**
- ✅ PR validation workflow must pass (Trivy, SonarQube, Terraform, Helm)
- ✅ Linear history enforced (rebase merge only)
- ❌ Direct push blocked for all (including admins)

### `develop`
- ✅ Requires PR with **minimum 1 reviewer**
- ✅ PR validation workflow must pass
- ❌ Direct push blocked

### `release/*`
- ✅ Only bug fixes and version bump commits allowed
- ✅ Created from `develop`, merged to both `main` and `develop`

---

## Workflow – Feature Development

```
git checkout develop
git pull origin develop
git checkout -b feature/JIRA-123-add-refund-api

# ... develop, commit ...
git push origin feature/JIRA-123-add-refund-api

# Open PR → develop
# GitHub Actions runs PR validation:
#   ✅ mvn test + JaCoCo
#   ✅ SonarQube quality gate (80% coverage min)
#   ✅ Trivy image scan (no CRITICAL)
#   ✅ Helm lint + kubeval
#   ✅ Terraform fmt + validate

# After approval: Squash merge into develop
# Jenkins builds → pushes to ECR → updates gitops-manifests/environments/dev/values.yaml
# Argo CD auto-syncs → deploys to payg-dev namespace
```

## Workflow – Release

```
git checkout -b release/v1.2.0 develop

# Update version in pom.xml + Chart.yaml
# Final QA on staging / dev
# Only bug fixes in this branch

# Merge release → main (PR, 2 reviewers)
# Merge release → develop (sync)

# Tag on main:
git tag -a v1.2.0 -m "Release v1.2.0 – Refund API"
git push origin v1.2.0

# Jenkins builds → ECR → opens PR to gitops-manifests/environments/prod/values.yaml
# Team reviews GitOps PR → merges
# Argo CD Production App requires MANUAL SYNC (intentional – human approval)
```

## Workflow – Hotfix

```
git checkout -b hotfix/JIRA-456-payment-timeout main

# Fix, test, commit
git push origin hotfix/JIRA-456-payment-timeout

# PR → main (emergency: 1 reviewer minimum, expedited review)
# After merge: Jenkins auto-deploys to production
# Backport: PR → develop
```

---

## Commit Message Convention (Conventional Commits)

```
<type>(<scope>): <short description>

type: feat | fix | ci | chore | docs | refactor | test | hotfix
scope: payment | infra | helm | gitops | ansible | docker

Examples:
feat(payment): add UPI refund API endpoint
fix(payment): handle timeout on processor callback
ci(jenkins): add Trivy CRITICAL gate before ECR push
chore(helm): bump chart version to 1.2.0
hotfix(payment): fix null pointer on empty merchant ID
```

---

## Environment Mapping

| Branch | Environment | Deploy Trigger | Argo CD Sync |
|---|---|---|---|
| `feature/*` | — (CI only) | PR validation only | None |
| `develop` | dev | Jenkins auto-build on merge | Auto |
| `release/*` | dev/staging | Jenkins auto-build | Auto (dev), Manual (staging) |
| `main` | production | Jenkins auto-build → GitOps PR | **Manual** |
| `hotfix/*` | production | Jenkins auto-build → GitOps PR | Manual |

---

## GitHub Secrets Required

Configure these in each repository under **Settings → Secrets and variables → Actions**:

| Secret | Description |
|---|---|
| `AWS_ACCOUNT_ID` | AWS account number |
| `AWS_DEPLOY_ROLE_ARN` | OIDC role ARN for GitHub Actions to assume |
| `SONAR_TOKEN` | SonarQube authentication token |
| `SONAR_HOST_URL` | SonarQube server URL |
| `JENKINS_URL` | Jenkins server URL |
| `JENKINS_USER` | Jenkins API user |
| `JENKINS_API_TOKEN` | Jenkins API token |
| `GITOPS_GITHUB_TOKEN` | GitHub PAT for gitops-manifests repo writes |
