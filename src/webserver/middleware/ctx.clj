(ns webserver.middleware.ctx
  (:require
   [clojure.set :refer [superset?]]))

(defn set-required [services]
  (cond
    (set? services)
    services

    (seq? services)
    (into #{} services)

    (vector? services)
    (into #{} services)

    (keyword? services)
    #{services}

    (nil? services)
    #{}
    :else
    (throw (ex-info "required-services needs to be a set" {}))
    ))

(defn wrap-ctx
  [handler ctx]
  (fn
    ([request]
     (let [request (assoc request :ctx ctx)]
       (handler request)))
    ([request respond raise]
     (let [request (assoc request :ctx ctx)]
       (handler request respond raise)))))

(def ctx-middleware
  {:name ::ctx
   ;:spec (s/keys :req-un [::authorize])
   :compile
   (fn [{:keys [services services-ctx]} _router-opts] 
     (when services
       ;(println "route services: " services)
       (assert (map? services-ctx) "ctx-middleware services-ctx needs to be a map")
       ;(println "services ctx: " (keys services-ctx))
       (let [needed (set-required services)
             ;_ (println "needed: " needed)
             provided (into #{} (keys services-ctx))
             ;_ (println "provided: " provided)
             ]
         (assert (superset? provided needed) 
                 (str "web route missing provided services: needed: " needed " provided " provided))
         (fn [handler]
           (wrap-ctx handler services-ctx)))))})