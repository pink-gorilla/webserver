(ns webserver.middleware.exception
  (:require
   [clojure.string]
   [taoensso.timbre :refer [error]]))

(defn wrap-fallback-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (error "handler exception : " e)
        {:status 500 :body "A server-side exception has occured!"}))))