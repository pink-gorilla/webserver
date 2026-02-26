(ns webserver.router
  (:require
   [taoensso.timbre :refer [debug info warn error]]
   [clojure.spec.alpha :as s]
   [reitit.ring :as ring]
   [reitit.core :as r]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.util.response :as response]
   [modular.writer :refer [write-edn-private]]
   [webserver.router.ext :as discover]
   [webserver.router.resolver :refer [resolve-handler]]))

;; Static file handler for index.html
#_(def static-handler
    (-> (fn [_] (response/resource-response "index.html" {:root "public"}))
        wrap-resource
        wrap-content-type
        wrap-not-modified))

(def default-routes
  [; ["/" {:handler (fn [_]
   ;                 (response/resource-response "public/index.html"))}]
   ;["/r/*" (ring/create-resource-handler {:root "public"})]
   ["/code/*" (ring/create-resource-handler {:root ""})]
      ;["/r/*" (ring/create-resource-handler {:root "public" :path "/r/"})]
   ])

(defn create-routes [{:keys [exts] :as _services} user-routes]
  (let [discovered-routes (discover/get-routes exts)
        all-routes (->> (concat default-routes user-routes discovered-routes)
                        (into []))
        _ (write-edn-private "routes" all-routes)
        resolved-routes (resolve-handler all-routes)]
    resolved-routes))

(defn validate-route-middleware-specs
  "Validates that all route middleware specs are met before router creation. Throws an exception with details if any route fails validation."
  [routes router-opts]
  (try
    (let [temp-router (r/router routes router-opts)
          route-data (r/routes temp-router)]
      (doseq [rd route-data]
        (let [path (first rd)
              ;_ (info "path: " path)
              data (second rd)]
          (info "CHECKING ROUTE: " path (keys data)) ;(:services-ctx :middleware :handler)
          (when-let [middlewares (:middleware data)]
            (doseq [middleware middlewares]
              (info "CHECKING MIDDLEWARE: " middleware)
              (when-let [spec (:spec middleware)]
                (when-not (s/valid? spec data)
                  (error "Route middleware spec validation failed path: " path "  Middleware:" (:name middleware))
                  (let [explanation (s/explain-data spec data)]
                    ;(error "  Spec explanation:" explanation)
                    (throw (ex-info "Route middleware spec validation failed"
                                    {:path path
                                     :middleware (:name middleware)
                                       ;:route-data data
                                     ;:spec-explanation explanation
                                     })))))))
          ;
          )))
    (catch Exception e
      ;; If router creation fails, it might be due to spec validation
      ;; Re-throw with a clearer message
      (error "Failed to create router - middleware spec validation error.")
      (throw (ex-info "Router creation failed due to middleware spec validation"
                      {})))))

(defn create-router [{:keys [ctx exts] :as _services} routes]
  ; router
  (info "creating reitit router..")
  (let [router-opts {:data {:services-ctx ctx
                            :middleware [;my-middleware
                                          ;parameters/parameters-middleware
                                          ;wrap-keyword-params
                                          ;middleware-db
                                         ]}}]
    ;; Validate all route middleware specs before creating the router
    (info "pre validate")
    (validate-route-middleware-specs routes router-opts)
    (info "post validate")
    (let [rr (ring/router routes router-opts)]
      (info "reitit router created!")
      rr)))

(defn create-handler [services user-routes]
  (let [routes (create-routes services user-routes)
        router (create-router services routes)]
    (ring/ring-handler
     router
   ; default handler
     (ring/routes
      (ring/create-file-handler {:root ".gorilla/public" :path "/r/"})
      (ring/create-resource-handler {:root "public" :path "/r/"})
      (ring/create-file-handler {:root "node_modules" :path "/r/"}) ; for npm lib css loading
      (ring/create-resource-handler {:root "" :path "/code/"}) ; for goldly cljs loader. security issue
      (ring/create-default-handler
       {:not-found (constantly {:status 404 :body "Not found"})})))))
