(ns webserver.scheduler
  (:require
   [tick.core :as t]
   [missionary.core :as m]))

(defn periodic-seq [start duration]
  (iterate #(t/>> % duration) start))

(defn scheduler-ap
  "returns a missionary flow that returns dt at the dt given."
  [dt-seq]
  (m/ap
   (let [input (m/seed dt-seq)
         next-time (m/?> input)
         time (t/now)
         diff (- (t/long next-time) (t/long time))
         diff-ms (* 1000 diff)]
     (when (> diff-ms 0)
       ;(println "scheduler sleeping for ms: " diff-ms " until: " next-time)
       (m/? (m/sleep diff-ms next-time)))
     next-time)))

;; sequences

(defn- next-daily-est [h]
  (let [now (-> (t/zoned-date-time)
                (t/in  "America/New_York"))
        dt (-> now
               (t/date)
               (t/at (t/new-time h 0 0)))]
    ;(println "now: " now)
    ;(println "dt: " dt)
    (if (t/> now dt)
      (t/>> dt (t/new-duration 1 :days))
      dt)))

(comment
  (next-daily-est 7)
  ;; => #time/date-time "2025-01-01T07:00"

  (next-daily-est 19)
  ;; => #time/date-time "2024-12-31T19:00"

  (next-daily-est 22)
  ;; => #time/date-time "2024-12-31T22:00"
; 
  )

(defn daily-at-cet [h]
  (periodic-seq
   (-> (next-daily-est h) (t/instant))
   (t/new-duration 1 :days)))

(comment
  (t/instant)
  ;; => #inst "2024-12-31T17:49:52.857993710-00:00"

  (take 2 (daily-at-cet 17))
  ;; => (#inst "2024-12-31T22:00:00.000000000-00:00" #inst "2025-01-01T22:00:00.000000000-00:00")

;  
  )

