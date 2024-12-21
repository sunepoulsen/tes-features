#!/bin/bash

ARG_OUTPUT_DIR=$1
ARG_DATABASE_NAME=$2
ARG_PASSWORD_LENGTH=$3

ADM_PASSWORD_FILE=$ARG_DATABASE_NAME-database-admin-password.txt
APP_PASSWORD_FILE=$ARG_DATABASE_NAME-database-application-password.txt

mkdir -p $1
cd $1

# Delete previous password files
rm -rf $ADM_PASSWORD_FILE $APP_PASSWORD_FILE

# Generate a passphrase for adm user
openssl rand -base64 $ARG_PASSWORD_LENGTH > $ADM_PASSWORD_FILE

# Generate a passphrase for app user
openssl rand -base64 $ARG_PASSWORD_LENGTH > $APP_PASSWORD_FILE

cd -
