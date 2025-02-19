(ns demo.webserver
  (:require
   [hiccup.page :as page]
   [modular.webserver.handler.not-found :refer [not-found-handler]]
   [modular.webserver.handler.files :refer [->FilesMaybe ->ResourcesMaybe]]
   [modular.webserver.handler.html :refer [html-response]]
   [modular.webserver.middleware.exception :refer [wrap-fallback-exception]]
   [modular.webserver.server :refer [start-webserver]]
   [modular.webserver.router :as router]
    [modular.webserver.middleware.ctx :refer [ctx-middleware]]
     [demo.fortune :as fc]
   ))

(defn main-page [_]
  (println "rendering main page..")
  (html-response
   (page/html5
    {:mode :html}
    [:div
     [:h1 "hello, world!"]
     [:a {:href "/r/demo.txt"} [:p "demo.txt"]]
     [:a {:href "/big-void"} [:p "big-void (unknown route)"]]])))

;(def routes
;  ["/" {"" main-page
;        "r/" (->ResourcesMaybe {:prefix "public"})
;        #{"r" "public"} (->FilesMaybe {:dir "public-web"})
;        true not-found-handler}])

(def ctx {:fortune-db fc/fortune-db})

(def user-routes 
[["/ping" {:get (fn [_] {:status 200 :body "pong"})}]
 ["/cookie" {:get (fn [{:keys [ctx]}]
                    {:status 200 :body (fc/fortune (:fortune-db ctx))})
             :middleware [ctx-middleware]
             :services #{:fortune-db}}]
     ;"time"   {:get demo.handler/time-handler}
 ]  
  )


(def r (router/create-router ctx user-routes))

(def h (router/create-handler r))


(defn run-webserver [opts]
  (println "opts: " opts)
  (let [;ring-handler (-> (wrap-bidi routes)
        ;                 (wrap-fallback-exception))
        ]
    (start-webserver h opts)))


;(defn handler [{{db :db} :data request}]
;  {:status 200 :body (str "Connected to " db)})

