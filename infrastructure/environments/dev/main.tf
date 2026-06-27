terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
  }

  backend "s3" {
    bucket         = "payg-terraform-state-dev"
    key            = "dev/terraform.tfstate"
    region         = "ap-south-1"
    dynamodb_table = "payg-terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.common_tags
  }
}

locals {
  cluster_name = "payg-eks-${var.environment}"
  common_tags = {
    Project     = "PayG Plus"
    Environment = var.environment
    ManagedBy   = "Terraform"
    Owner       = "DevOps"
    CostCenter  = "engineering"
  }
}

module "vpc" {
  source       = "../../modules/vpc"
  cluster_name = local.cluster_name
  vpc_cidr     = var.vpc_cidr
  az_count     = var.az_count
  common_tags  = local.common_tags
}

module "iam" {
  source           = "../../modules/iam"
  cluster_name     = local.cluster_name
  aws_region       = var.aws_region
  oidc_provider_arn = module.eks.oidc_provider_arn
  oidc_provider_url = module.eks.oidc_provider_url
  common_tags      = local.common_tags
}

module "ecr" {
  source            = "../../modules/ecr"
  repository_names  = ["payg-payment-gateway"]
  node_role_arn     = module.iam.node_role_arn
  common_tags       = local.common_tags
}

module "eks" {
  source               = "../../modules/eks"
  cluster_name         = local.cluster_name
  environment          = var.environment
  kubernetes_version   = var.kubernetes_version
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  public_subnet_ids    = module.vpc.public_subnet_ids
  cluster_role_arn     = module.iam.cluster_role_arn
  node_role_arn        = module.iam.node_role_arn
  node_instance_types  = var.node_instance_types
  use_spot             = var.use_spot
  desired_nodes        = var.desired_nodes
  min_nodes            = var.min_nodes
  max_nodes            = var.max_nodes
  common_tags          = local.common_tags
}
