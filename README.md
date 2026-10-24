# Werkgroep Computerhuis Oost - portaal

## Configuration

The needed environment variables in docker:

```env
DATABASE_HOST="localhost"
DATABASE_NAME="placeholder"
DATABASE_USERNAME="placeholder"
DATABASE_PASSWORD="placeholder"
SECURITY_REMEMBER_ME_KEY="something"
KEY_STORE_PASSWORD="something"
```

## Build docker image

```shell
mvn clean package spring-boot:build-image
```

Export image via podman:

```shell
podman save -o computerhuis-portaal.tar com.github.computerhuis:portaal:1.0.0-SNAPSHOT
docker save -o computerhuis-portaal.tar portaal:1.0.0-SNAPSHOT
```

## Create a self-signed certificate

Goto the folder `src/main/resources` and create a keystore:

```shell
keytool -genkeypair -alias springboot -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore keystore.p12 -validity 3650 -dname "C=NL,ST=Noord-Brabant,L='s-Hertogenbosch,O=Werkgroep Computerhuis Oost,OU=IT,CN=computerhuis"
```
