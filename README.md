![](https://raw.githubusercontent.com/nats-io/nats-site/master/src/img/large-logo.png)
# NATS - Java client
A [Java](http://www.java.com) client for the [NATS messaging system](https://nats.io).

[![License Apache 2.0](https://img.shields.io/badge/License-Apache2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/nats-io/java-nats.svg?branch=master)](http://travis-ci.org/nats-io/java-nats)
[![Coverage Status](https://coveralls.io/repos/nats-io/java-nats/badge.svg?branch=master&service=github)](https://coveralls.io/github/nats-io/java-nats?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.nats/jnats/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.nats/jnats)
[![Javadoc](http://javadoc.io/badge/io.nats/jnats.svg)](http://javadoc.io/doc/io.nats/jnats)

[![Dependency Status](https://www.versioneye.com/user/projects/57c07fac968d640039516937/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57c07fac968d640039516937)
[![Reference Status](https://www.versioneye.com/java/io.nats:jnats/reference_badge.svg?style=flat-square)](https://www.versioneye.com/java/io.nats:jnats/references)

## Installation

### Maven Central

#### Releases

Current stable release (click for pom info): [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.nats/jnats/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.nats/jnats)

#### Snapshots

Snapshot releases from the current `master` branch are uploaded to Sonatype OSSRH (OSS Repository Hosting) with each successful Travis CI build. 
If you don't already have your pom.xml configured for using Maven snapshots, you'll need to add the following repository to your pom.xml:

```xml
<profiles>
  <profile>
     <id>allow-snapshots</id>
        <activation><activeByDefault>true</activeByDefault></activation>
     <repositories>
       <repository>
         <id>snapshots-repo</id>
         <url>https://oss.sonatype.org/content/repositories/snapshots</url>
         <releases><enabled>false</enabled></releases>
         <snapshots><enabled>true</enabled></snapshots>
       </repository>
     </repositories>
   </profile>
</profiles>

```
#### Building from source code (this repository)
To clone, compile, and install in your local maven repository (or copy the artifacts from the `target/` directory to wherever you need them):
```
git clone git@github.com:nats-io/java-nats.git
cd java-nats
mvn install
```

## Platform Notes
### Linux
We use RNG to generate unique inbox names. A peculiarity of the JDK on Linux (see [JDK-6202721] (https://bugs.openjdk.java.net/browse/JDK-6202721) and [JDK-6521844](https://bugs.openjdk.java.net/browse/JDK-6521844)) causes Java to use `/dev/random` even when `/dev/urandom` is called for. The net effect is that successive calls to `newInbox()`, either directly or through calling `request()` will become very slow, on the order of seconds, making many applications unusable if the issue is not addressed. A simple workaround would be to use the following jvm args.

`-Djava.security.egd=file:/dev/./urandom`

## Basic Usage

```java
import io.nats.client.*;

// ...

// Connect to default URL ("nats://localhost:4222")
Connection nc = Nats.connect();

// Simple Publisher
nc.publish("foo", "Hello World".getBytes());

// Simple Async Subscriber
nc.subscribe("foo", m -> {
    System.out.printf("Received a message: %s\n", new String(m.getData()));
});

// Simple Sync Subscriber
int timeout = 1000;
SyncSubscription sub = nc.subscribeSync("foo");
Message msg = sub.nextMessage(timeout);

// Unsubscribing
sub = nc.subscribe("foo");
sub.unsubscribe();

// Requests
msg = nc.request("help", "help me".getBytes(), 10000);

// Replies
nc.subscribe("help", message -> {
    try {
        nc.publish(message.getReplyTo(), "I can help!".getBytes());
    } catch (Exception e) {
        e.printStackTrace();
    }
});

// ...

// Close connection
nc.close();
```

## TLS

TLS/SSL connections may be configured through the use of an [SSLContext](https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLContext.html).
 
```java
	// Set up and load the keystore
	final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	final char[] keyPassPhrase = "password".toCharArray();
	final KeyStore ks = KeyStore.getInstance("JKS");
	ks.load(classLoader.getResourceAsStream("keystore.jks"), keyPassPhrase);
	kmf.init(ks, keyPassPhrase);

	// Set up and load the trust store
	final char[] trustPassPhrase = "password".toCharArray();
	final KeyStore tks = KeyStore.getInstance("JKS");
	tks.load(classLoader.getResourceAsStream("cacerts"), trustPassPhrase);
	final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	tmf.init(tks);

	// Get and initialize the SSLContext
	SSLContext c = SSLContext.getInstance(Constants.DEFAULT_SSL_PROTOCOL);
	c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

	// Create NATS options
	Options opts = new Options.Builder()
		.secure()       // Set the secure option, indicating that TLS is required
		.tlsDebug()     // Set TLS debug, which will produce additional console output
		.sslContext(c)  // Set the context for this factory
		.build();

	// Create a new SSL connection
	try (Connection connection = Nats.connect("nats://localhost:1222", opts)) {
		connection.publish("foo", "Hello".getBytes());
		connection.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
```

## Wildcard Subscriptions

```java

// "*" matches any token, at any level of the subject.
nc.subscribe("foo.*.baz", m -> {
    System.out.printf("Msg received on [%s] : %s\n", m.getSubject(), new String(m.getData()));
});

nc.subscribe("foo.bar.*", m -> {
    System.out.printf("Msg received on [%s] : %s\n", m.getSubject(), new String(m.getData()));
});

// ">" matches any length of the tail of a subject, and can only be the last token
// E.g. 'foo.>' will match 'foo.bar', 'foo.bar.baz', 'foo.foo.bar.bax.22'
nc.subscribe("foo.>", m -> {
    System.out.printf("Msg received on [%s] : %s\n", m.getSubject(), new String(m.getData()));
});

// Matches all of the above
nc.publish("foo.bar.baz", "Hello World");

```

## Queue Groups

```java
// All subscriptions with the same queue name will form a queue group.
// Each message will be delivered to only one subscriber per queue group,
// using queuing semantics. You can have as many queue groups as you wish.
// Normal subscribers will continue to work as expected.

nc.subscribe("foo", "job_workers", m -> {
    received += 1;
});

```

## Advanced Usage

```java

// Flush connection to server, returns when all messages have been processed.
nc.flush()
System.out.println("All clear!");

// flush can also be called with a timeout value.
try {
    flushed = nc.flush(1000);
    System.out.println("All clear!");
} catch (TimeoutException e) {
    System.out.println("Flushed timed out!");
}

// Auto-unsubscribe after MAX_WANTED messages received
final static int MAX_WANTED = 10;
...
sub = nc.subscribe("foo");
sub.autoUnsubscribe(MAX_WANTED);

// Multiple connections
nc1 = Nats.connect("nats://host1:4222");
nc2 = Nats.connect("nats://host2:4222");

nc1.subscribe("foo", m -> {
    System.out.printf("Received a message: %s\n", new String(m.getData()));
});

nc2.publish("foo", "Hello World!");

```

## Clustered Usage

```java

String[] servers = new String[] {
	"nats://localhost:1222",
	"nats://localhost:1223",
	"nats://localhost:1224",
};

// Setup options to include all servers in the cluster
ConnectionFactory cf = new ConnectionFactory();
cf.setServers(servers);

// Optionally set ReconnectWait and MaxReconnect attempts.
// This example means 10 seconds total per backend.
cf.setMaxReconnect(5);
cf.setReconnectWait(2000);

// Optionally disable randomization of the server pool
cf.setNoRandomize(true);

Connection nc = cf.createConnection();

// Setup callbacks to be notified on disconnects and reconnects
nc.setDisconnectedCallback(event -> {
    System.out.printf("Got disconnected from %s!\n", event.getConnection().getConnectedUrl());
});

// See who we are connected to on reconnect.
nc.setReconnectedCallback(event -> {
    System.out.printf("Got reconnected to %s!\n", event.getConnection().getConnectedUrl());
});

// Setup a callback to be notified when the Connection is closed
nc.setClosedCallback( event -> {
    System.out.printf("Connection to %s has been closed.\n", event.getConnection().getConnectedUrl());
});

```

## License

Unless otherwise noted, the NATS source files are distributed
under the Apache Version 2.0 license found in the LICENSE file.
