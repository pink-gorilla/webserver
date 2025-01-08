(ns demo.core
  (:require
   [modular.webserver.https.core :refer [start-redirect]]))




(defn start [& _]
  (start-redirect)
  
  )