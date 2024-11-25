(ns modular.webserver.server
  (:require
   [taoensso.timbre :as timbre :refer [info error]]
   [modular.webserver.server.jetty :as jetty]
   [modular.webserver.server.httpkit :as httpkit]))

(defn start [webserver-config ring-handler server-type]
  (let [server (case server-type
                 :jetty (jetty/start-jetty ring-handler webserver-config)
                 :httpkit (httpkit/start-httpkit ring-handler webserver-config)
                 (do (error "start-server failed: server type not found: " type)
                     nil))]
    {:server-type server-type
     :server server}))

(defn stop [{:keys [server server-type]}]
  (when server
    (case server-type
      :jetty (jetty/stop-jetty server)
      :httpkit (httpkit/stop-httpkit server)
      (info "there was no server started."))))
