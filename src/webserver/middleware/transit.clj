(ns webserver.middleware.transit
  (:require
   [modular.encoding.transit :as e]
   [muuntaja.core :as m]))

(def muuntaja
  (m/create
   (-> m/default-options
       (update-in
        [:formats "application/transit+json" :decoder-opts]
        (partial merge e/decode))
       (update-in
        [:formats "application/transit+json" :encoder-opts]
        (partial merge e/encode)))))