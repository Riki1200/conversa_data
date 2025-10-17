# conversa_data

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                      | Description                                                                        |
| ---------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Authentication](https://start.ktor.io/p/auth)                            | Provides extension point for handling the Authorization header                     |
| [Routing](https://start.ktor.io/p/routing)                                | Provides a structured routing DSL                                                  |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                    | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [Firebase authentication](https://start.ktor.io/p/firebase-auth-provider) | Authentication provider for Firebase Auth module                                   |
| [Task Scheduling](https://start.ktor.io/p/ktor-server-task-scheduling)    | Manages scheduled tasks across instances of your distributed Ktor server           |
| [WebSockets](https://start.ktor.io/p/ktor-websockets)                     | Adds WebSocket protocol support for bidirectional client connections               |
| [Koin](https://start.ktor.io/p/koin)                                      | Provides dependency injection                                                      |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)        | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization)    | Handles JSON serialization using kotlinx.serialization library                     |
| [Metrics](https://start.ktor.io/p/metrics)                                | Adds supports for monitoring several metrics                                       |
| [Authentication OAuth](https://start.ktor.io/p/auth-oauth)                | Handles OAuth Bearer authentication scheme                                         |
| [Swagger](https://start.ktor.io/p/swagger)                                | Serves Swagger UI for your project                                                 |
| [OpenAPI](https://start.ktor.io/p/openapi)                                | Serves OpenAPI documentation                                                       |
| [Compression](https://start.ktor.io/p/compression)                        | Compresses responses using encoding algorithms like GZIP                           |
| [Default Headers](https://start.ktor.io/p/default-headers)                | Adds a default set of headers to HTTP responses                                    |
| [CORS](https://start.ktor.io/p/cors)                                      | Enables Cross-Origin Resource Sharing (CORS)                                       |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
| -----------------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

