(ns demo.fortune)

(def fortune-db
  ["The early bird catches the worm."
   "Better to be wise than to be ignorant."
   "What has a begin has an end."
   "Adam and Eve"])

(defn fortune [db]
  (get db (rand-int (count db))))

(defn fortune-handler  [{:keys [ctx]}]
  {:status 200 :body (fortune (:fortune-db ctx))})

(comment
  (fortune fortune-db)
;
  )
