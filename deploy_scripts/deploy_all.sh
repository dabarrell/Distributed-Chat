#!/bin/bash

echo "Setting up 115.146.95.8"
ssh ubuntu@115.146.95.8 "bash -s $1 mihiraNectar && exit" < deploy.sh
echo "Setting up 115.146.89.137"
ssh ubuntu@115.146.89.137 "bash -s $1 mihira2Nectar && exit" < deploy.sh
