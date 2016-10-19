#!/bin/bash

echo "Setting up 115.146.89.137"
ssh ubuntu@115.146.89.137 "bash -s $1 dev3 $2 new" < deploy.sh
