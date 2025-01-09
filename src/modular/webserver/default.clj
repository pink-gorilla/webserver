(ns modular.webserver.default)





(def http-default
  {:port 8080
   :ip "0.0.0.0"})

(def https-default
  {:port 443
   :ip "0.0.0.0"
   :certificate ".https-certificates/keystore.p12"
   :password "123456789"})

(def letsencrypt-default
  {:path ".letsencrypt"})


