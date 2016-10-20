#!/bin/bash

echo "Setting up 43.240.99.121"
ssh ubuntu@43.240.99.121 "bash -s $1 s2 $2 root" < deploy.sh
