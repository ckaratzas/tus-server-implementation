# TUS Server Java Implementation
An implementation of the Tus Resumable Upload protocol [https://tus.io/protocols/resumable-upload.html] in java. Vertx-Web is used for the http stack part
and redis as the backend for upload information management.
Extensions supported are creation,checksum,termination,concatenation. The purpose of this repository is to provide a Tus protocol implementation agnostic
of the underlying storage provider thus allowing implementors to focus on their business logic and specific needs.
An issue that is not a concern of the protocol but could be an extension is the handling of the locks during a patch operation. So when a patch is initiated
a lock is acquired (for most obvious cases) to ensure consistency. If the process triggered from patch fails and lock is not released there is a 
phantom lock remaining. This edge case could be mitigated by either making the server sticky and keeping the locks in-process or by perhaps issuing a 
release request with a lock token obtained by the initiator of the upload.
No authentication valves are implemented here also.

# Instructions
To just build the tus-server-implementation just:

```cd <root>```

```mvn clean install```

To build the docker image for the tus-server-implementation just (docker edge release [https://docs.docker.com/edge/] is required!):

```cd <root>```

```docker build -t tus_server .```

In order to run tus-server-implementation along with all dependencies just (assuming you already have the docker image built from above):

```cd <root>```

```docker-compose -f tus-server.yml up```

In order to setup the tus-server-implementation for development from your favorite IDE redis running is required:

```cd <root>```

```docker-compose -f redis.yml up```

After that in order to run the tus-server-implementation from inside your favorite IDE just:
Run the com.tus.oss.server.application.Application main class 
with program arguments:
 -c <root>/configuration/ -b tus-server-beans.xml
and VM parameters: -Dlogging.config=file:<root>/configuration/logback.xml
Redis must be live also (see above how to run it)

# Testing
In the test folder:
There is a very simple upload test that uses tus-java-client [https://github.com/tus/tus-java-client]. 
There are also curl tests that test simple upload and partial uploads (concatenation extension)
