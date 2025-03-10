(ns webserver.middleware.transit
  (:require
   [transit.io :refer [read-opts write-opts]]
   [muuntaja.core :as m]))

(def muuntaja
  (m/create
   (-> m/default-options
       (update-in
        [:formats "application/transit+json" :decoder-opts]
        (partial merge (read-opts)))
       (update-in
        [:formats "application/transit+json" :encoder-opts]
        (partial merge (write-opts))))))