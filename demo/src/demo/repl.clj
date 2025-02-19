(ns demo.repl 
  (:require 
   [reitit.core :as r]
    [reitit.ring :as ring]
   [ring.util.response :as response]
    [modular.webserver.middleware.ctx :refer [ctx-middleware]]
   ;[modular.webserver.router :as router]
   [demo.fortune :as fc]
   ))


(def ctx {:fortune-db fc/fortune-db})

(defn create-router [ctx]
  ; router
  (ring/router
   [["/" {:handler (fn [_]
                     (response/resource-response "public/index.html"))}]
    ["/ping" {:get (fn [_] {:status 200 :body "pong"})}]
    ["/cookie" {:get (fn [{:keys [ctx]}]
                       {:status 200 :body (fc/fortune (:fortune-db ctx))})
                :middleware [ctx-middleware]
                :services #{:fortune-db}}]
     ;"time"   {:get demo.handler/time-handler}
    ["/r/*" (ring/create-resource-handler)]
     ;["/r/*" (ring/create-resource-handler {:path "public" :root "/r/"})]
    ]
   {:data {:services-ctx ctx
           :middleware [;my-middleware
                         ;parameters/parameters-middleware
                         ;wrap-keyword-params
                         ;middleware-db
                        ]}}))


(create-router ctx)



