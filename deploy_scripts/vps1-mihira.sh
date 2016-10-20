#!/bin/bash

echo "Setting up 115.146.85.146"
ssh -t ubuntu@115.146.85.146 "bash -s $1 s1 $2 root" < deploy.sh
