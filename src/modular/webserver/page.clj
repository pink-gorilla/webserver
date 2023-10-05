(ns modular.webserver.page
  (:require
   [clojure.string :as str]
   [hiccup.page :as page]
   [taoensso.timbre :refer [debug info error]]))


(defn head 
  ([config]
    (head config []))
  ([{:keys [prefix title icon description author charset]
             :or {prefix "/r"
                  title "my-app"
                  icon "/webly/icon/pinkgorilla32.png"
                  description "webly app"
                  author "pink-gorilla"
                  charset "utf-8"}
             }
     more-head-elements]
  (let [head [:head
              [:meta {:http-equiv "Content-Type"
                      :content (str "text/html; charset=" charset)}]
              [:meta {:name "viewport"
                      :content "width=device-width, initial-scale=1.0"}]
              [:meta {:name "description"
                      :content description}]
              [:meta {:name "author"
                      :content author}]
              ; <meta name= "keywords" content= "keywords,here" >
              [:title title]
              [:link {:rel "shortcut icon" :href (str prefix icon)}]
              ]]
    (into head more-head-elements))))


(defn page [config body]
  (page/html5
    {:mode :html}
      (head config)
      body))