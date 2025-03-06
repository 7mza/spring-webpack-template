# spring + webpack template

another minimalistic spring thymeleaf webpack template

webpack:

* ts to js
* minification
* lint/format
* tree shaking

spring:

* nonce filter

haproxy:

* self-signed https cert
* http to https redirect
* basic CSP

generate self signed tls:

```shell
sudo apt install openssl
sudo chmod +x ./keystore/gen_cert.sh
./keystore/gen_cert.sh
```

compile:

```shell
npm i
npm run build
./gradlew clean ktlintFormat ktlintCheck build
./gradlew --stop // if gradle-node-plugin is giving errors
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

* https://github.com/node-gradle/gradle-node-plugin
* gradle npm tasks cache
