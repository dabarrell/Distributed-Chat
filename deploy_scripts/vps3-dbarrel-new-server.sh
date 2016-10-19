#!/bin/bash

echo "Setting up 115.146.91.218"
ssh ubuntu@115.146.91.218 "bash -s $1 dev3 $2 new" < deploy.sh
