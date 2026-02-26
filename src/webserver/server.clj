(ns webserver.server
  (:require
   [clojure.set :refer [rename-keys]]
   [clojure.string :as str]
   [taoensso.timbre :as timbre :refer [info error]]
   [babashka.fs :as fs]
   [missionary.core :as m]
   [modular.writer :refer [write-edn-private]]
   [webserver.server.jetty :as jetty]
   [webserver.https.letsencrypt :refer [renew-cert convert-cert]]
   [webserver.https.proxy :refer [start-proxy]]
   [webserver.scheduler :refer [daily-at-cet scheduler-ap]]
   [webserver.default :refer [http-default letsencrypt-default https-default]])
  (:import [missionary Cancelled]))

 ; https

(defn- https-creds? [{:keys [certificate]}]
  (fs/exists? certificate))

(defn- start-https [{:keys [handler https https-a] :as _this}]
  (let [opts (-> https
                 (rename-keys {:port :ssl-port
                               :certificate :keystore
                               :password :key-password})
                 (assoc :ssl? true
                        :port 0)) ; port 0 means dont listen on http.
        j (if (= (:port https) 0)
            (info "https server disabled.")
            (if (https-creds? https)
              (jetty/start-jetty handler opts)
              (info "no https certficiate found: " (:certificate https))))]
    (reset! https-a j)))

(defn- stop-https [{:keys [https-a] :as _this}]
  (when @https-a
    (jetty/stop-jetty @https-a)
    (reset! https-a nil)))

(defn restart-https [this]
  (info "restarting https server..")
  (stop-https this)
  (start-https this))

(defn renew-letsencrypt-certificate [{:keys [_http https letsencrypt] :as this}]
  (when (and (not (= (:port https) 0))
             (:domain letsencrypt))
    (let [r (renew-cert letsencrypt)
          line1 (-> r :out str/split-lines first)]
        ; first line:  Account registered.
        ; first line:  Certificate not yet due for renewal
        ;  Successfully  received certificat
       ;  Renewing an existing certificate for test.crbclean.com
      (info "full result: " (:out r))
      (info "renewal result: " line1)
      (when (or (str/includes? line1 "Account registered") ; first time
                (str/includes? line1 "renewed")
                (str/includes? line1 "received")
                (str/includes? line1 "Renewing an existing certificate"))
        (info "new certificate received.. converting")
        (convert-cert letsencrypt https)
        (restart-https this)))))

(defn start-scheduler [this]
  (let [dt-seq  (daily-at-cet 22)
        scheduler (scheduler-ap dt-seq)
        processor (m/ap
                   (let [dt (m/?> scheduler)]
                     (info "renewing letsencrypt certificate.. " dt)
                     (renew-letsencrypt-certificate this)
                     (info "renewing letsencrypt certificate.. done.")))
        task (m/reduce (fn [_ _] nil) nil processor)]
    (info "starting letsencrypt renewal scheduler. ")
    (task
     #(info "scheduler completed: " %)
     #(if (instance? Cancelled %)
        (info "scheduler was cancelled.")
        (error "scheduler crashed: " %)))))

(defn start-webserver [handler {:keys [http https letsencrypt] :as opts}]
  (info "starting webserver..")
  (let [http (merge http-default http) ; defaults are overwritten by opts
        https (merge https-default https) ; defaults are overwritten by opts
        letsencrypt (merge letsencrypt-default letsencrypt)
        http-h (jetty/start-jetty handler http)
        this {:handler handler
              :http-h http-h
              :http http
              :https https
              :letsencrypt letsencrypt
              :https-a (atom nil)
              :proxy (when-not (= (:port https) 0)
                       (start-proxy opts))}
        scheduler (when-not (= (:port https) 0)
                    (start-scheduler this))
        this (assoc this :scheduler scheduler)]
    ; config
    (write-edn-private "webserver" {:http http :https https :letsencrypt letsencrypt})
    ; start
    (start-https this)
    (renew-letsencrypt-certificate this)
    (info "webserver started!")
    this))

(defn stop-webserver [{:keys [http-h _https proxy scheduler] :as this}]
  (when scheduler
    (info "stopping scheduler.")
    (scheduler))
  (when http-h
    (jetty/stop-jetty http-h))
  (stop-https this)
  (when proxy
    (jetty/stop-jetty proxy)))
