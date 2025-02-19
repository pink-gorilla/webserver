(ns webserver.server
   (:require
    [clojure.set :refer [rename-keys]]
    [clojure.string :as str]
    [taoensso.timbre :as timbre :refer [info error]]
    [babashka.fs :as fs]
    [modular.writer :refer [write-edn-private]]
    [webserver.server.jetty :as jetty]
    [webserver.https.letsencrypt :refer [renew-cert convert-cert]]
    [webserver.https.proxy :refer [start-proxy]]
    [webserver.default :refer [http-default letsencrypt-default https-default]]
    ))

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
         (reset! https-a j)
       ))

 (defn- stop-https [{:keys [https-a] :as _this}]
   (when @https-a
     (jetty/stop-jetty @https-a)
     (reset! https-a nil)))

 (defn restart-https [this]
   (info "restarting https server..")
   (stop-https this)
   (start-https this))
 
 (defn renew-letsencrypt-certificate [{:keys [http https letsencrypt] :as this}]
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

 (defn start-webserver [handler {:keys [http https letsencrypt] :as opts}]
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
                        (start-proxy opts))}]
     (write-edn-private "webserver" {:http http :https https :letsencrypt letsencrypt})
     (start-https this)
     (renew-letsencrypt-certificate this)
     this))

 (defn stop [{:keys [http-h https proxy] :as this}]
   (when http-h
     (jetty/stop-jetty http-h))
   (stop-https this)
   (when proxy 
     (jetty/stop-jetty proxy)))
