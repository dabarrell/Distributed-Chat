#!/bin/bash

# Use this command to execute on remote machine
# ssh ubuntu@<host-name> 'bash -s <password>' < deploy.sh

if [ $# -eq 0 ]; then
  echo "The first argument must be the password to decrypt the private key"
  echo "The seconds argument must be the name of the server"
  exit 1
fi

if [ $(which java | grep java -c ) -eq 0 ]; then
  sudo add-apt-repository ppa:webupd8team/java
  sudo apt-get update --force-yes --yes
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
  sudo apt-get install oracle-java8-installer --force-yes --yes
fi
sudo apt-get install git --force-yes --yes
sudo apt-get install maven --force-yes --yes

keyEnc="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
nbDONFAtuPt1mRqB0IOy1A=="

# Use openssl aes-256-cbc -a -salt -k password -out secrets.txt.enc
# to encrypt new key if required
if [ ! -e ~/.ssh/git ]; then
  echo "$keyEnc" | openssl aes-256-cbc -d -a -k "$1" -out git
  mv git ~/.ssh/
  chmod 400 ~/.ssh/git
fi

# Generate config file if not allready present
if [ ! -e ~/.ssh/config ]; then
  touch ~/.ssh/config
  cat <<EOF > ~/.ssh/config
Host git github.com
  HostName github.com
  User git
  IdentityFile ~/.ssh/git
EOF
fi

# Add github.com to known hosts if not allready added
if [ ! -n "$(grep "^github.com " ~/.ssh/known_hosts)" ]; then ssh-keyscan github.com >> ~/.ssh/known_hosts 2>/dev/null; fi

# Clone repo
if [ -e COMP90015-A1 ]; then rm -rf COMP90015-A1 ; fi
git clone git@github.com:alastairpat/COMP90015-A1.git
cd COMP90015-A1

# Temporary checkout
git checkout mihira/authentication

# execute server
killall java
nohup ./run_deamon.sh "$2" &
exit 0
