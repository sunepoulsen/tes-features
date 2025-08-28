#!/bin/bash

ARG_OUTPUT_DIR=$1
ARG_PASSWORD_LENGTH=$2

SUPER_USER_PASSWORD_FILE=database-super-user-password.txt

mkdir -p $ARG_OUTPUT_DIR
cd $ARG_OUTPUT_DIR

# Delete previous password files
rm -rf $SUPER_USER_PASSWORD_FILE

# Generate a passphrase for super user
openssl rand -base64 $ARG_PASSWORD_LENGTH > $SUPER_USER_PASSWORD_FILE

cd -
