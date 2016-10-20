#!/bin/bash

echo "Setting up 43.240.99.123"
ssh ubuntu@115.146.86.164 "bash -s $1 s3 $2 root" < deploy.sh
