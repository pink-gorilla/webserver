(ns modular.webserver.server
   (:require
    [taoensso.timbre :as timbre :refer [info error]]
    [babashka.fs :as fs]
    [clojure.set :refer [rename-keys]]
    [modular.webserver.server.jetty :as jetty]
    [modular.webserver.default :refer [http-default letsencrypt-default https-default]]
    [modular.webserver.https.proxy :refer [start-proxy]]))

 ; https

  (defn https? [{:keys [port]}]
   (not (= port 0)))

 (defn https-creds? [{:keys [certificate]}]
   (fs/exists? certificate))

 (defn start-https [{:keys [https handler] :as _this}]
   (when (https-creds? https)
     (let [opts (-> https
                    (assoc :port (or (:port https)
                                     (:port https-default))
                           :ip (or (:ip https)
                                   (:ip https-default)))
                    (rename-keys {:certificate :keystore
                                  :password :key-password}))]
       (jetty/start-jetty handler opts))))

 (defn stop-https [{:keys [https-a] :as _this}]
   (when @https-a
     (jetty/stop-jetty @https-a)
     (reset! https-a nil)))

 (defn restart-https [this]
   (stop-https this)
   (start-https this))
 
 (defn start-webserver [handler {:keys [http https letsencrypt] :as this}]
   (let [http (jetty/start-jetty handler (assoc http
                                                :port (or (:port http)
                                                               (:port http-default))
                                                :ip (or (:ip http)
                                                           (:ip http-default))))
         https-a (atom (start-https https))]
     {:handler handler
      :http http
      :https https
      :https-a https-a
      :proxy (when (https? https) (start-proxy this))
      }))

 (defn stop [{:keys [http https] :as this}]
   (when http
     (jetty/stop-jetty http))
   (stop-https this))
