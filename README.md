# webserver [![GitHub Actions status |pink-gorilla/webserver](https://github.com/pink-gorilla/webserver/workflows/CI/badge.svg)](https://github.com/pink-gorilla/webserver/actions?workflow=CI)[![Clojars Project](https://img.shields.io/clojars/v/org.pinkgorilla/webserver.svg)](https://clojars.org/org.pinkgorilla/webserver)


Core functions

- web router with reitit (that is extensible)
- jetty ring handler with letsencrypt https certificates
- depends on modular for transit encoding


# demo https:

```
clj -X:webserver
```