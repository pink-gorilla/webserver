(ns modular.webserver.handler.not-found
  (:require
   [ring.util.response :as response]))

; not found handler

(def not-found-body "<h1>  bummer, not found </h1")

(defn not-found-handler [_req]
  (response/not-found not-found-body))