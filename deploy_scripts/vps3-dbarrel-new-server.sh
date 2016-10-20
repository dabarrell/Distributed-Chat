#!/bin/bash

echo "Setting up 43.240.99.123"
ssh ubuntu@43.240.99.123 "bash -s $1 s3 $2 root" < deploy.sh
