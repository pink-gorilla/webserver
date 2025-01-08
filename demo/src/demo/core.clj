(ns demo.core
  (:require
   [modular.webserver.https.letsencrypt :refer [renew-cert convert-cert]]
   [modular.webserver.https.proxy :refer [start-redirect]]))


(def config {:letsencrypt {:domain "admin.crbclean.com"
                           :email "webadmin@crbclean.com"}
             :https {}})



(defn renew [& _]
  (renew-cert (:letsencrypt config)))

(defn convert [& _]
  (convert-cert (:letsencrypt config)
                (:https config)))

(defn start [& _]
  (start-redirect (:letsencrypt config)))





