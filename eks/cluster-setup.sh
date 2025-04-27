#!/bin/bash

set -euo pipefail  # Enable strict error handling

# Variables
CLUSTER_NAME="my-cluster"
REGION="us-west-2"
NODEGROUP_NAME="linux-nodes"
NODE_TYPE="t3.medium"
NODES=2
NODES_MIN=1
NODES_MAX=3

# Function to log messages
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# Create EKS cluster
log "Creating EKS cluster: $CLUSTER_NAME in region: $REGION"
eksctl create cluster \
  --name "$CLUSTER_NAME" \
  --region "$REGION" \
  --nodegroup-name "$NODEGROUP_NAME" \
  --node-type "$NODE_TYPE" \
  --nodes "$NODES" \
  --nodes-min "$NODES_MIN" \
  --nodes-max "$NODES_MAX" \
  --managed

# Update kubeconfig
log "Updating kubeconfig for cluster: $CLUSTER_NAME"
aws eks update-kubeconfig --region "$REGION" --name "$CLUSTER_NAME"

log "EKS cluster setup completed successfully."
