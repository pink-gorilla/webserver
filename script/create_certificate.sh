#!/bin/sh

# keytool -keysize 2048 -genkey -alias jetty -keyalg RSA -keystore jetty.keystore

certbot certonly \
 -w ./certs \
 -d panama.crbclean.com \
 --logs-dir /tmp \
 --config-dir ./certs \
 --work-dir ./certs \
 --standalone
