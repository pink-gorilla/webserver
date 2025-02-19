(ns webserver.middleware.api
  (:require
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.gzip :refer [wrap-gzip]]
   [muuntaja.middleware :refer [wrap-format]] ; 30x faster than ring.middleware.format
   ;[ring.middleware.cors :refer [wrap-cors]]
   ;[ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   ;[ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
   ;[ring.middleware.session :refer [wrap-session]]
   [webserver.middleware.transit :refer [muuntaja]]
   [webserver.middleware.exception :refer [wrap-fallback-exception]]))

(defn wrap-api-handler
  [handler]
  (-> handler ; middlewares execute from bottom -> up
      ;(wrap-anti-forgery)
      ;(wrap-defaults api-defaults)
      (wrap-keyword-params)
      (wrap-params)
      ;(wrap-format) ; muuntaja https://github.com/metosin/muuntaja
      (wrap-format muuntaja)
      ;(wrap-json-response)
      (wrap-gzip)
      wrap-fallback-exception))