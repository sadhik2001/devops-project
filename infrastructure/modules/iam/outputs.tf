output "cluster_role_arn"          { value = aws_iam_role.eks_cluster.arn }
output "node_role_arn"             { value = aws_iam_role.eks_nodes.arn }
output "lb_controller_role_arn"    { value = aws_iam_role.lb_controller.arn }
output "external_secrets_role_arn" { value = aws_iam_role.external_secrets.arn }
