(ns demo.routes
   (:require
    [hiccup.page :as page]
    [extension :refer [discover]]
    [modular.webserver.handler.html :refer [html-response]]
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
     [:a {:href "/r/demo.txt"} [:p "demo.txt (file-handler)"]]
     [:a {:href "/r/bongo.edn"} [:p "bongo.edn (resource-handler)"]]
     [:a {:href "/r/maya.html#willy"} [:p "maya.html (# router test)"]]
     [:a {:href "/big-void"} [:p "big-void (unknown route)"]]
     [:img {:src "/r/moon.jpg"
            :width "200px"
            :height "200px"}]
     ])))

;(def routes
;  ["/" {"" main-page
;        "r/" (->ResourcesMaybe {:prefix "public"})
;        #{"r" "public"} (->FilesMaybe {:dir "public-web"})
;        true not-found-handler}])

(def ctx {:fortune-db fc/fortune-db})

(def user-routes
  [["/" {:get main-page}]
   ["/ping" {:get (fn [_] {:status 200 :body "pong"})}]
    ;"time"   {:get demo.handler/time-handler}
   ])

(def exts (discover))

(router/create-routes user-routes exts)

(def r (router/create-router ctx user-routes exts))