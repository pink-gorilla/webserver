(ns demo.webserver
  (:require
   [webserver.server :refer [start-webserver]]
   [demo.routes :refer [h]]))


(defn run-webserver [webserver-opts]
  (println "opts: " webserver-opts)
  (let [;ring-handler (-> (wrap-bidi routes)
        ;                 (wrap-fallback-exception))
        ]
    (start-webserver h webserver-opts)))