(ns demo.webserver
  (:require
   [modular.webserver.server :refer [start-webserver]]
   [modular.webserver.router :as router]
   [demo.routes :refer [r]]))


(def h (router/create-handler r))


(defn run-webserver [opts]
  (println "opts: " opts)
  (let [;ring-handler (-> (wrap-bidi routes)
        ;                 (wrap-fallback-exception))
        ]
    (start-webserver h opts)))


;(defn handler [{{db :db} :data request}]
;  {:status 200 :body (str "Connected to " db)})

