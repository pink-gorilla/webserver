#!/bin/sh

# keytool -keysize 2048 -genkey -alias jetty -keyalg RSA -keystore jetty.keystore

mkdir .letsencrypt

sudo certbot certonly \
  --non-interactive --agree-tos -m webmaster@crbclean.com \
  --webroot --webroot-path public \
  -d admin.crbclean.com \
  --work-dir ./letsencrypt \
  --config-dir ./letsencrypt
 
