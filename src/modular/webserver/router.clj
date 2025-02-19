(ns modular.webserver.router
  (:require
   [reitit.ring :as ring]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.util.response :as response]
   [modular.webserver.router.ext :as discover]
   [modular.webserver.router.resolver :refer [resolve-handler]]
   
   ))


;; Static file handler for index.html
#_(def static-handler
    (-> (fn [_] (response/resource-response "index.html" {:root "public"}))
        wrap-resource
        wrap-content-type
        wrap-not-modified))

(def default-routes 
  [["/" {:handler (fn [_]
                    (response/resource-response "public/index.html"))}]
   ["/r/*" (ring/create-resource-handler)]
         ;["/r/*" (ring/create-resource-handler {:path "public" :root "/r/"})]
   ])

(defn create-routes [user-routes exts]
  (let [discovered-routes (discover/get-routes exts)
        discovered-routes (resolve-handler discovered-routes)]
    (concat default-routes user-routes discovered-routes)))


  (defn create-router [ctx user-routes exts]
  ; router
    (ring/router
     (create-routes user-routes exts)
     {:data {:services-ctx ctx
             :middleware [;my-middleware
                         ;parameters/parameters-middleware
                         ;wrap-keyword-params
                         ;middleware-db
                          ]}}))

(defn create-handler [router]
  (ring/ring-handler
   router
   ; default handler
   (ring/routes
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))
