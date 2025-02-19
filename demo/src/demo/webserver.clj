(ns demo.webserver
  (:require
   [webserver.server :refer [start-webserver]]
   [demo.routes :refer [h]]))


(defn run-webserver [webserver-opts]
  (println "opts: " webserver-opts)
  (start-webserver h webserver-opts))