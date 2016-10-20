#!/bin/bash

echo "Setting up 43.240.99.124"
ssh -t ubuntu@43.240.99.124 "bash -s $1 s1 $2 root" < deploy.sh
