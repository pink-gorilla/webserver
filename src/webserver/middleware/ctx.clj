(ns webserver.middleware.ctx
  (:require
   [taoensso.timbre :as timbre :refer [error]]
   [clojure.set :refer [superset?]]
   [clojure.spec.alpha :as s]))

(defn wrap-ctx
  [handler ctx]
  (fn
    ([request]
     (let [request (assoc request :ctx ctx)]
       (handler request)))
    ([request respond raise]
     (let [request (assoc request :ctx ctx)]
       (handler request respond raise)))))

(defn spec-ok [{:keys [services services-ctx]}]
  (and services-ctx
       (map? services-ctx)
       services
       (set? services)
       (let [provided (into #{} (keys services-ctx))]
         (superset? provided services))))

;; Clojure spec definitions
(s/def ::services
  (s/and set?
         (s/every keyword? :kind set?)))

(s/def ::services-ctx
  (s/and map?
         (s/map-of keyword? any?)))

(s/def ::ctx-route-data
  (s/and (s/keys :req-un [::services ::services-ctx])
         (fn [{:keys [services services-ctx]}]
           (let [provided (into #{} (keys services-ctx))]
             (superset? provided services)))))


(comment 
   (s/valid? ::ctx-route-data {:services #{:db :cache} :services-ctx nil})  
  
  ;; Test the spec:
   (s/valid? ::ctx-route-data {:services #{:db :cache} :services-ctx {:db {} :cache {}}})  
  ; => true
 (s/valid? ::ctx-route-data {:services #{:db :cache} :services-ctx {:db {}}})
 ; => false (missing :cache)
(s/valid? ::ctx-route-data {:services #{:db} :services-ctx {:db {} :cache {}}}) 
; => true (superset is ok)
 (s/valid? ::ctx-route-data {:services #{:db} :services-ctx {}}) 
 ; => false (missing :db)
 (s/valid? ::ctx-route-data {:services #{:db} :services-ctx nil}) 
 ; => false (services-ctx must be a map)
 (s/explain ::ctx-route-data {:services #{:db} :services-ctx nil})

 ; => shows why it's invalid 
 ; 
  )

;; problem is that inside compile I cannot do an assert or throw an exception.
;; this will lead to endless printing

(def ctx-middleware
  {:name ::ctx
   :spec ::ctx-route-data
   :compile
   (fn [{:keys [services-ctx] :as route-data} _router-opts]
     (if (spec-ok route-data)
       (fn [handler] (wrap-ctx handler services-ctx))
       ;; return empty map just to enforce spec
       ;; The middleware (and associated spec) will still be part of the chain, but will not process the request.
       (do 
         ; (:services-ctx :middleware :handler :services)
         (error " context-middleware handler " (:handler route-data) " does not meet the required spec.")
         {})))})
