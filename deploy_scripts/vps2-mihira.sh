#!/bin/bash

echo "Setting up 43.240.99.121"
ssh ubuntu@115.146.85.217 "bash -s $1 s2 $2 root" < deploy.sh
