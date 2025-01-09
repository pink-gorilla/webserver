(ns demo.https
  (:require
   [hiccup.page :as page]
   [modular.webserver.handler.not-found :refer [not-found-handler]]
   [modular.webserver.handler.files :refer [->FilesMaybe ->ResourcesMaybe]]
   [modular.webserver.handler.html :refer [html-response]]
   [modular.webserver.middleware.bidi :refer [wrap-bidi]]
   [modular.webserver.middleware.exception :refer [wrap-fallback-exception]]
   [modular.webserver.server :refer [start-webserver]]))

(defn main-page [_]
  (println "rendering main page..")
  (html-response
   (page/html5
    {:mode :html}
    [:div
     [:h1 "hello, world!"]
     [:a {:href "/r/demo.txt"} [:p "demo.txt"]]
     [:a {:href "/big-void"} [:p "big-void (unknown route)"]]])))

(def routes
  ["/" {"" main-page
        "r/" (->ResourcesMaybe {:prefix "public"})
        #{"r" "public"} (->FilesMaybe {:dir "public-web"})
        true not-found-handler}])

(defn run-webserver [opts]
  (println "opts: " opts)
  (let [ring-handler (-> (wrap-bidi routes)
                         (wrap-fallback-exception))]
    (start-webserver ring-handler opts)))


