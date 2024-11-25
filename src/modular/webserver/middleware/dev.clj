(ns modular.webserver.middleware.dev
  (:require
   [taoensso.timbre :refer [warn]]
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.reload :refer [wrap-reload]]))

; stolen from gorilla-notebook env/dev

(defn wrap-dev [handler]
  (warn "dev-mode: wrapping ring-handler with reload and exceptions")
  (-> handler
      wrap-exceptions
      wrap-reload))


