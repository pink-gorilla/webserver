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

 (defn start-https [{:keys [handler https https-a] :as _this}]
  (let [https (merge https-default https) ; defaults are overwritten by opts
        opts (-> https
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

 (defn stop-https [{:keys [https-a] :as _this}]
   (when @https-a
     (jetty/stop-jetty @https-a)
     (reset! https-a nil)))

 (defn restart-https [this]
   (stop-https this)
   (start-https this))
 
 (defn start-webserver [handler {:keys [http https letsencrypt] :as opts}]
   (let [http (merge http-default http) ; defaults are overwritten by opts
         http (jetty/start-jetty handler http)
         this {:handler handler
               :http http
               :https https
               :https-a (atom nil)
               :proxy (when-not (= (:port https) 0) 
                        (start-proxy opts))}]
     (start-https this)
     this))

 (defn stop [{:keys [http https] :as this}]
   (when http
     (jetty/stop-jetty http))
   (stop-https this))
