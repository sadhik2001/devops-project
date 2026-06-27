variable "cluster_name"          { type = string }
variable "kubernetes_version"    { type = string; default = "1.29" }
variable "environment"           { type = string }
variable "vpc_id"                { type = string }
variable "private_subnet_ids"    { type = list(string) }
variable "public_subnet_ids"     { type = list(string) }
variable "cluster_role_arn"      { type = string }
variable "node_role_arn"         { type = string }
variable "endpoint_public_access"{ type = bool; default = true }
variable "node_instance_types"   { type = list(string); default = ["t3.large"] }
variable "use_spot"              { type = bool; default = false }
variable "desired_nodes"         { type = number; default = 2 }
variable "min_nodes"             { type = number; default = 1 }
variable "max_nodes"             { type = number; default = 5 }
variable "common_tags"           { type = map(string); default = {} }
