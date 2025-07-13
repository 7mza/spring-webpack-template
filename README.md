# spring + webpack template

another minimalistic spring thymeleaf webpack template

webpack:

* ts to js
* minification
* lint/format
* chunking
* tree shaking
* build cache
* cache busting

spring:

* nonce filter

gradle:

* gradle npm caching

haproxy:

* self-signed https cert
* http to https redirect
* basic CSP

generate self signed tls:

```shell
# apt install openssl
cd ./keystore/
chmod +x ./gen_cert.sh
./gen_cert.sh
```

compile:

```shell
npm i
npm run build
./gradlew clean ktlintFormat ktlintCheck build
./gradlew --stop # if gradle-node-plugin is giving errors
```

run:

```shell
./gradlew bootRun
```

http://localhost:8080

or with docker

```shell
docker compose up --build
```

https://localhost

#### TODO
