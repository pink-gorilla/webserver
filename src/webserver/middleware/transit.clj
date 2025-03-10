(ns webserver.middleware.transit
  (:require
   [transit.io :refer [encode decode]]
   [muuntaja.core :as m]))

(def muuntaja
  (m/create
   (-> m/default-options
       (update-in
        [:formats "application/transit+json" :decoder-opts]
        (partial merge decode))
       (update-in
        [:formats "application/transit+json" :encoder-opts]
        (partial merge encode)))))