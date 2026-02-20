(ns webserver.router
  (:require
   [taoensso.timbre :refer [debug info warn error]]
   [reitit.ring :as ring]
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

(defn create-router [{:keys [ctx exts] :as _services} routes]
  ; router
  (info "creating reitit router..")
  (let [rr (ring/router
            routes
            {:data {:services-ctx ctx
                    :middleware [;my-middleware
                                 ;parameters/parameters-middleware
                                 ;wrap-keyword-params
                                 ;middleware-db
                                 ]}})]
    (info "reitit router created!")
    rr))

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
