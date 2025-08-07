# spring + webpack template

minimalistic spring thymeleaf webpack template

webpack

* ts to js
* minification
* lint/format
* chunking
* tree shaking
* build cache
* cache busting

spring

* nonce filter

gradle

* gradle npm caching

haproxy

* self-signed https cert
* http to https redirect
* basic CSP

## generate self signed tls

```shell
# apt install openssl
cd ./keystore/
chmod +x ./gen_cert.sh
./gen_cert.sh
```

## build

```shell
npm i

# webpack production mode
npm run build
./gradlew clean ktlintFormat ktlintCheck build

# webpack development mode
npm run build:dev
./gradlew clean ktlintFormat ktlintCheck build -x test -Pmode=development

# if gradle-node-plugin is giving errors
./gradlew --stop
```

## run

```shell
./gradlew bootRun
```

http://localhost:8080

or with docker

```shell
docker compose up --build
```

https://localhost
