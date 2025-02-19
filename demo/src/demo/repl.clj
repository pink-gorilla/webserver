(ns demo.repl 
  (:require 
   [reitit.core :as r]
    [reitit.ring :as ring]
   [ring.util.response :as response]
    [modular.webserver.middleware.ctx :refer [ctx-middleware]]
   ;[modular.webserver.router :as router]
   [demo.fortune :as fc]
   [modular.webserver.router.ext :refer [get-api-routes get-routes]]
    [extension :refer [discover get-extensions]]
   ))


(def exts (discover))

exts

(get-api-routes exts)

(get-routes exts)

(defn convert-legacy-format [routes]
  (if (map? routes)
    (->> routes
         (map (fn [[route handler]]
                [route handler]))
         (into []))
    routes))

(convert-legacy-format 
{"time" {:get 'demo.handler/time-handler},
 "timejava" {:get 'demo.handler/time-java-handler-wrapped},
 "biditest" {:get 'demo.handler/bidi-test-handler-wrapped},
 "test" {:get 'demo.handler.test/test-handler, :post 'demo.handler.test/test-handler},
 "snippet" {:get 'demo.handler/snippet-handler-wrapped},
 "bindata" 'demo.handler.binary/binary-handler} 
 
 )

[["time" {:get demo.handler/time-handler}]
 ["timejava" {:get demo.handler/time-java-handler-wrapped}]
 ["biditest" {:get demo.handler/bidi-test-handler-wrapped}]
 ["test" {:get demo.handler.test/test-handler, :post demo.handler.test/test-handler}]
 ["snippet" {:get demo.handler/snippet-handler-wrapped}]
 ["bindata" demo.handler.binary/binary-handler]]



;(create-router ctx)



