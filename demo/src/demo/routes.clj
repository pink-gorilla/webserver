(ns demo.routes
   (:require
    [hiccup.page :as page]
    [extension :refer [discover]]
    [modular.webserver.handler.not-found :refer [not-found-handler]]
    [modular.webserver.handler.files :refer [->FilesMaybe ->ResourcesMaybe]]
    [modular.webserver.handler.html :refer [html-response]]
    [modular.webserver.middleware.exception :refer [wrap-fallback-exception]]
    [modular.webserver.router :as router]
    [demo.fortune :as fc] ; needed to create the context
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
    ;"time"   {:get demo.handler/time-handler}
   ])

(def exts (discover))

(router/create-routes user-routes exts)

(def r (router/create-router ctx user-routes exts))