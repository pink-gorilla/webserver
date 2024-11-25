(ns modular.webserver.server.jetty
  (:require
   [clojure.java.io :as io]
   [taoensso.timbre :as timbre :refer [info]]
   ;[ring.adapter.jetty9 :refer [run-jetty]]
   [ring.adapter.jetty :refer [run-jetty]]))

(defn run-jetty-server [ring-handler {:keys [port ssl-port keystore] :as user-opts}]
  (let [default-opts {:allow-null-path-info true ; omit the trailing slash from your URLs
                      :ws-max-idle-time 3600000 ; important for nrepl middleware 
                      }
        https? (and
                ssl-port
                keystore
                (.exists (io/file keystore)))
        user-opts (if https?
                    user-opts
                    (dissoc user-opts :keystore :ssl-port :key-password))

        opts (merge default-opts ;ws-opts 
                    user-opts)]
  ; https://github.com/sunng87/ring-jetty9-adapter  
    (if https?
      (info "Starting Jetty web server (http:" port "https:" ssl-port ")")
      (info "Starting Jetty web server (http:" port "https: none)"))
    (run-jetty ring-handler opts)))

(defn start-jetty
  [ring-handler config]
  (run-jetty-server ring-handler
                    (assoc config :join? false)))

(defn stop-jetty
  [server]
  ;https://github.com/dharrigan/websockets/blob/master/src/online/harrigan/api/router.clj
  (info "stopping jetty-server..")
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!