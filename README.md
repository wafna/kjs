# Demo Kotlin/JS Project

* Server using Netty in Ktor.
* Browser using React in Kotlin/JS.
* Client in Ktor.
* REST API
* H2 database with Flyway.

Some run configurations for IDEA are included in `.run`.
One starts the server, another runs the browser client in continuous update mode.

## Projects

* **server**

Hosts an HTTP endpoint on port 8081 that serves the production build of the web app
and serves the API.

* **browser**

A single page web app that consumes the API.

* **domain**

Data definitions shared among projects.

* **client**

A KTor client for the server API.

* **util**

Non-project specific shared code and dependencies.

### References

Overview, benefits, motivation.

* [Kotlin for JavaScript](https://kotlinlang.org/docs/js-overview.html)

An excellent tutorial and, when completed, you get to watch a bunch of interesting videos.

* [Build a web application with React and Kotlin/JS — tutorial](https://kotlinlang.org/docs/js-react.html)
