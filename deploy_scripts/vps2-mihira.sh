#!/bin/bash

echo "Setting up 115.146.89.137"
ssh ubuntu@115.146.89.137 "bash -s $1 mihira2Nectar $2" < deploy.sh
