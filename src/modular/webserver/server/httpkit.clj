(ns modular.webserver.server.httpkit
  (:require
   [taoensso.timbre :as timbre :refer [info]]))

; http://http-kit.github.io/
;https://github.com/http-kit/http-kit
; this dependency is not part of webly dependencies:  ;{http-kit "2.5.3"}

(defn start-httpkit
  [ring-handler config]
  (let [run-server (requiring-resolve 'org.httpkit.server/run-server)
        default-opts {:allow-null-path-info true ; omit the trailing slash from your URLs
                      :ws-max-idle-time 3600000 ; important for nrepl middleware 
                      }
        {:keys [port]} config]
    (info "Starting Httpkit web server (http:" port ")")
    (run-server ring-handler {:port port
                              :host "0.0.0.0"})))

(defn stop-httpkit [server]
  (info "stopping httpkit server..")
  ;(server) ; Immediate shutdown (breaks existing reqs)
  ;; Graceful shutdown (wait <=100 msecs for existing reqs to complete):
  (server :timeout 100))