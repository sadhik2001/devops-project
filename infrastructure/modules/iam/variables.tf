variable "cluster_name"       { type = string }
variable "aws_region"         { type = string }
variable "oidc_provider_arn"  { type = string; default = "" }
variable "oidc_provider_url"  { type = string; default = "" }
variable "common_tags"        { type = map(string); default = {} }
