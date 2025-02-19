(ns modular.webserver.router.ext
  (:require
   [extension :refer [get-extensions]]))



(defn get-api-routes [exts]
  (->> (get-extensions exts {:api-routes {}})
       (map :api-routes)
       (apply merge)))

(defn get-routes [exts]
  (->> (get-extensions exts {:web/routes []})
       (map :web/routes)
       (apply concat)
       (into [])
       ))

 