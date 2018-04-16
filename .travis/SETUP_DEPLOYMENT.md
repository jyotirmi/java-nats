# Deploying NATS maven packages in Travis

This document describes the steps required to setup NATS' maven deployments in Travis.

## Key Generation

Note, gpg 2.2 is not supported in Travis, so you need to use gpg2.0 to generate these keys.

Generate your private and public keys:

```text
gpg --gen-key (The NATS Team, info@nats.io, <choose a gpg password>)
gpg --list-keys

---------------------------------------
pub   4096R/<pub key> 2018-04-16
      Key fingerprint = DB3F 9CD2 5EDF 2FD8 2B06  4927 A585 4FBC 39C7 B4A3
uid       [ultimate] The NATS Team <info@nats.io>
sub   4096R/<sub key> 2018-04-16
```

Add your key to a public keyserver:

```text
gpg --keyserver hkp://pool.sks-keyservers.net --send-key <pub key>
```

You can test with:

```text
gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys <pub key>
```

Save the private key and gpg passphrase someplace safe.

## Create the encoded keyrings

Encode the keys to check in for travis.

```text
openssl aes-256-cbc -pass pass:<gpg password> -in ~/.gnupg/trustdb.gpg -out keyrings/secring.gpg.enc
openssl aes-256-cbc -pass pass:<gpg password> -in ~/.gnupg/pubring.gpg -out keyrings/pubring.gpg.enc
```

## Setup Travis

Login to Travis.ci with your Github credentials.

```text
travis login
```

Add the secure variables to Travis.  This adds "secret" tags in .Travis.yml.

```text
travis encrypt --add -r nats-io/java-nats ENCRYPTION_PASSWORD=<gpg encryption password>
travis encrypt --add -r nats-io/java-nats SONATYPE_USERNAME=<username>
travis encrypt --add -r nats-io/java-nats SONATYPE_PASSWORD=<password>
travis encrypt --add -r nats-io/java-nats GPG_KEYNAME=<gpg keyname (ex. 1C06698F)>
travis encrypt --add -r nats-io/java-nats GPG_PASSPHRASE=<gpg passphrase>
```

These are used throughout the pom for signing the deployment jars and for uploading to the sonatype repository.
