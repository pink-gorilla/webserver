(ns modular.webserver.server
   (:require
    [taoensso.timbre :as timbre :refer [info error]]
    [babashka.fs :as fs]
    [clojure.set :refer [rename-keys]]
    [modular.webserver.server.jetty :as jetty]
    [modular.webserver.default :refer [http-default letsencrypt-default https-default]]
    [modular.webserver.https.proxy :refer [start-proxy]]))

 ; https

 (defn https-creds? [{:keys [certificate]}]
   (fs/exists? certificate))

 (defn start-https [{:keys [https handler https-a] :as _this}]
  (let [https  (assoc https :port (or (:port https)
                                      (:port https-default))
                            :ip (or (:ip https)
                                      (:ip https-default)))
        opts (rename-keys https {:certificate :keystore
                                 :password :key-password})
       j (if (= (:port https) 0)
           (info "https server disabled.")
           (if (https-creds? https)
             (jetty/start-jetty handler opts)
             (info "no https certficiate found: " (:certificate https))))]
         (reset! https-a j)
       ))

 (defn stop-https [{:keys [https-a] :as _this}]
   (when @https-a
     (jetty/stop-jetty @https-a)
     (reset! https-a nil)))

 (defn restart-https [this]
   (stop-https this)
   (start-https this))
 
 (defn start-webserver [handler {:keys [http https letsencrypt] :as opts}]
   (let [http (jetty/start-jetty handler (assoc http
                                                :port (or (:port http)
                                                          (:port http-default))
                                                :ip (or (:ip http)
                                                        (:ip http-default))))
         this {:handler handler
               :http http
               :https https
               :https-a (atom (start-https https))
               :proxy (when-not (= (:port https) 0) 
                        (start-proxy opts))}]
     (start-https this)
     this))

 (defn stop [{:keys [http https] :as this}]
   (when http
     (jetty/stop-jetty http))
   (stop-https this))
