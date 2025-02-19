(ns demo.webserver
  (:require
   [modular.webserver.server :refer [start-webserver]]
   [modular.webserver.router :as router]
   [demo.routes :refer [r]]))


(def h (router/create-handler r))


(defn run-webserver [webserver-opts]
  (println "opts: " webserver-opts)
  (let [;ring-handler (-> (wrap-bidi routes)
        ;                 (wrap-fallback-exception))
        ]
    (start-webserver h webserver-opts)))