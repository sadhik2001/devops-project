output "repository_urls" {
  value = { for name, repo in aws_ecr_repository.app : name => repo.repository_url }
}

output "registry_id" {
  value = values(aws_ecr_repository.app)[0].registry_id
}
