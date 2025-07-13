#!/bin/bash
# shellcheck disable=SC1091

set -euo pipefail

DAYS=3650
CERTS_DIR=./certs
EXT_FILE=openssl.cnf
PWD=123456789

mkdir -p "${CERTS_DIR:?}"
rm -rf "${CERTS_DIR:?}"/*

openssl req -x509 \
        -newkey rsa:4096 \
        -days $DAYS \
        -nodes \
        -subj "/CN=rootCA" \
        -keyout "${CERTS_DIR:?}/rootCA.key" \
        -out "${CERTS_DIR:?}/rootCA.pem"

generate_x509() {
        local name=$1
        local cn=$2
        local outfile=$3
        local concat=${4:-false}

        openssl req \
                -newkey rsa:4096 \
                -nodes \
                -keyout "${CERTS_DIR:?}/${outfile}.key" \
                -out "${CERTS_DIR:?}/${name}.csr" \
                -subj "/CN=${cn}"

        openssl x509 -req \
                -in "${CERTS_DIR:?}/${name}.csr" \
                -CA "${CERTS_DIR:?}/rootCA.pem" \
                -CAkey "${CERTS_DIR:?}/rootCA.key" \
                -CAcreateserial \
                -out "${CERTS_DIR:?}/${outfile}.pem" \
                -days "${DAYS}" \
                -extensions v3_req \
                -extfile "${EXT_FILE}"

        if [[ "${concat}" == true ]]; then
                cat "${CERTS_DIR:?}/${outfile}.pem" "${CERTS_DIR:?}/${outfile}.key" >"${CERTS_DIR:?}/${outfile}.combined.pem"
                rm -f "${CERTS_DIR:?}/${outfile}".{key,pem}
        fi
}

generate_x509 external localhost external true

rm -f "${CERTS_DIR:?}"/{*.{csr,srl},rootCA.key,rootCA.pem}
