# spring + webpack template

another minimalistic spring thymeleaf webpack template

I use this in my personal projects when I need to avoid webjars or the other overengineered gas factories around webpack

ts to js, minification, lint/format and tree shaking are configured

a nounce filter(once per request) was added in spring, it will add a nounce header to specified paths (should be applied
only to thymeleaf ctrl calls, and if u really need it)

self-signed https cert, http to https redirect and some basic CSP is configured using haproxy (same principles if u
prefer another *proxy)

generate a self signed tls cert :

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

https://github.com/node-gradle/gradle-node-plugin
