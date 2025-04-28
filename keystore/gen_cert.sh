#!/bin/bash
rm dev.pem dev.p12
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 3650 -nodes -config openssl.cnf -extensions v3_req
cat cert.pem key.pem > dev.pem
openssl pkcs12 -export -in dev.pem -inkey key.pem -out dev.p12 -name "dev" -passout pass:123456789
rm key.pem cert.pem
