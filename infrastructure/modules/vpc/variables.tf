variable "vpc_cidr"      { type = string; default = "10.0.0.0/16" }
variable "cluster_name"  { type = string }
variable "az_count"      { type = number; default = 2 }
variable "common_tags"   { type = map(string); default = {} }
