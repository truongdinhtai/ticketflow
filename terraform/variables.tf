variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "project" {
  type    = string
  default = "ticketflow"
}

variable "instance_type" {
  description = "EC2 size for the k3s node. t3.large (8GB) fits the full stack; t3.medium (4GB) fits a trimmed subset."
  type        = string
  default     = "t3.large"
}

variable "ssh_public_key" {
  description = "Your SSH public key (contents of ~/.ssh/id_ed25519.pub) for EC2 access"
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "CIDR allowed to SSH (use your.ip/32). Default is open — tighten it!"
  type        = string
  default     = "0.0.0.0/0"
}

variable "db_username" {
  type    = string
  default = "ticketflow"
}

variable "db_password" {
  description = "RDS master password. Pass via TF_VAR_db_password / a secret, never commit."
  type        = string
  sensitive   = true
}

variable "services" {
  description = "Service names -> one ECR repository each"
  type        = list(string)
  default = [
    "config-server", "discovery-server", "api-gateway", "auth-service",
    "event-service", "booking-service", "notification-service", "frontend",
  ]
}
