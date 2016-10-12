#!/bin/bash

echo "Setting up 115.146.95.8"
ssh -t ubuntu@115.146.95.8 "bash -s $1 mihiraNectar $2" < deploy.sh
