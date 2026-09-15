output "app_url" {
  description = "Public URL of the app (point your DNS here, or use directly)"
  value       = "http://${aws_instance.k3s.public_ip}"
}

output "ec2_public_ip" {
  value = aws_instance.k3s.public_ip
}

output "ec2_public_dns" {
  value = aws_instance.k3s.public_dns
}

output "ssh_command" {
  value = "ssh ubuntu@${aws_instance.k3s.public_ip}"
}

output "rds_endpoint" {
  description = "RDS host — put this in the K8s ConfigMap *_DB_URL entries"
  value       = aws_db_instance.postgres.address
}

output "ecr_registry" {
  description = "ECR registry host (set as the ECR_REGISTRY GitHub secret)"
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

output "ecr_repository_urls" {
  value = { for k, v in aws_ecr_repository.svc : k => v.repository_url }
}

data "aws_caller_identity" "current" {}
