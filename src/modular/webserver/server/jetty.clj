(ns modular.webserver.server.jetty
  (:require

   [taoensso.timbre :as timbre :refer [info]]
   ;[ring.adapter.jetty9 :refer [run-jetty]]  ; https://github.com/sunng87/ring-jetty9-adapter  
   [ring.adapter.jetty :refer [run-jetty]]))

(defn start-jetty [handler {:keys [port] :as opts}]
  (info "Starting Jetty web server port:" port  "..")
  (let [default-opts {:allow-null-path-info true ; omit the trailing slash from your URLs
                      :ws-max-idle-time 3600000 ; important for nrepl middleware 
                      }
        opts (merge default-opts
                    opts
                    {:join? false})]
    (run-jetty handler opts)))

(defn stop-jetty
  [server]
  ;https://github.com/dharrigan/websockets/blob/master/src/online/harrigan/api/router.clj
  (info "stopping jetty-server..")
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!