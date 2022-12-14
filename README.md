## Kafka mTLS Client Demo

This project provides a very simple demonstration of a Java Kafka client that connects to a Kafka broker using mutual TLS (mTLS).

It depends on the *spring-kafka* library to provide the plumbing needed to connect to the Kafka broker and to send and receive messages.

It also uses the *openshift-maven-plugin* to simplify deployment of the demo application to an Openshift project.

### Assumptions

The demo assumes that the _AMQ Streams_ operator is used to manage Kafka, a Kafka instance is up and running on Openshift, and that the developer can login to Openshift with access to Kafka resources.

The demo application is deployed into its own project. In this case we use *kafka-client* for the project name. Kafka is deployed in the *kafka* project.

The demo application uses a topic named *foobar*. This topic needs to be created in Kafka using the _AMQ Streams_ operator.

### Kafka Configuration

In order to allow access to Kafka from outside Openshift, routes need to be defined. To create the necessary routes, add the following snippet to the *listeners* section of the YAML file for the cluster. In this case, the cluster is named *my-cluster*.

    - name: external
      authentication:
        type: tls
      port: 9094
      tls: true
      type: route

The name is arbitrary. Here we use *external* to indicate that the listener is for clients that are external to Openshift.

The cluster also needs to have a Kafka User defined by the _AMQ Streams_ operator, if one does not already exist. The name is unimportant, and the default values assigned by the operator are sufficient. In this case, the user is named *my-user*.

The cluster will also have a certificate defined in a Secret. This demo assumes that the default self-signed certificate is used. In this case, the secret is named _my-cluster-cluster-ca-cert_.

### Truststore and Keystore

Use the following commands to extract the cluster truststore and associated password.

    oc get secret my-cluster-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.p12}' | base64 -d > truststore.p12
    
    oc get secret my-cluster-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.password}' | base64 -d

Use the following commands to extract the user keystore and associated password.

    oc get secret my-user -n kafka -o jsonpath='{.data.user\.p12}' | base64 -d > keystore.p12
    oc get secret my-user -n kafka -o jsonpath='{.data.user\.password}' | base64 -d

When running the demo on a local machine, the truststore and keystore files need be accessible. For this demo, they are placed in the `src/main/resources/certs` directory and the paths are configured for Kafka in `application.yaml`. The passwords that we extracted are also configured in `application.yaml`. Note that in a production environment, those passwords would be stored in a Secret or retrieved from a secure application like Vault.

In order to deploy the client application on Openshift, we need to make the truststore and keystore available via a Secret. Luckily, the openshift-maven-plugin makes this easy. Use the following commands to convert the truststore and keystore files into secrets that can be configured in a template for use by the openshift-maven-plugin:

    oc create secret generic dontcare --from-file=./src/main/resources/certs/truststore.p12 -o yaml --dry-run=client
    oc create secret generic dontcare --from-file=./src/main/resources/certs/keystore.p12 -o yaml --dry-run=client

Use the YAML output from the commands to create the template for a Secret named *kafka-client-secret*, which can be found in the file `src/main/jkube/secret.yaml`.

### Openshift Deployment

The openshift-maven-plugin uses the file `src/main/jkube/deploymentconfig.yaml` to extract the data from the _kafka-client-secret_ and mount a truststore file and a keystore file at a filesystem location where they are accessible by the demo client application.

The demo client application is deployed using the following command:

    mvn clean package oc:deploy

This command will run an S2I build on Openshift to compile the code, build a JAR file, create an image, push the image to Openshift, and deploy the image.

Once the application is running, it can be tested using a command similar to the following:

    curl http://client-kafka-client.apps.cluster-lb1614.lb1614.sandbox2566.opentlc.com/api/send?message=hello

If the message is sent, the response should be `<h2>Message sent</h2>`.

### Local Deployment

The demo client application is deployed using the following command:

    mvn clean spring-boot:run

This command will compile the code, build a JAR file, and run the JAR file.

Once the application is running, it can be tested using a command similar to the following:

    curl http://localhost:8080/api/send?message=hello

If the message is sent, the response should be `<h2>Message sent</h2>`.

