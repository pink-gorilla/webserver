(ns webserver.router.ext
  (:require
   [extension :refer [get-extensions]]))

(defn get-routes [exts]
  (->> (get-extensions exts {:web/routes []})
       (map :web/routes)
       (apply concat)
       (into [])
       ))

 