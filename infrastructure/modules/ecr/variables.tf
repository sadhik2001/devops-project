variable "repository_names" {
  description = "List of ECR repository names to create"
  type        = list(string)
  default     = ["payg-payment-gateway"]
}

variable "node_role_arn" {
  description = "IAM role ARN of EKS node group (for ECR pull policy)"
  type        = string
}

variable "common_tags" {
  type    = map(string)
  default = {}
}
