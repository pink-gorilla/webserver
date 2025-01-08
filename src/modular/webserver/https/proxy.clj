(ns modular.webserver.https.proxy
  (:require
   [babashka.fs :as fs]
   [taoensso.timbre :as timbre :refer [info error]]
   [ring.util.response :as response]
   [reitit.ring :as ring]
   [modular.webserver.server.jetty :refer [run-jetty-server]]))

(defn redirect-handler [{:keys [port]
                 :or {port 8080}}]
  (fn [{:keys [uri server-name scheme query-string] :as req}]
  (info"redirecting request: " uri)
  (let [redirect-url (str scheme "://" server-name ":" port uri (when query-string (str "?" query-string)))]
    (response/redirect redirect-url))))

(defn static-file-handler [dir]
  (let [acme-dir (str dir "/.well-known/acme-challenge")
        rh  (ring/create-file-handler {:root dir :path "/.well-known/acme-challenge/"})]
    (fs/create-dirs acme-dir)
    (fn [{:keys [uri] :as req}]
      (info "letsencrypt challenge on uri: " uri)
      (rh req))))

(defn handler [letsencrypt-dir]
  (ring/ring-handler
   (ring/router
    [["/ping" (fn [req] (info "ping!") {:status 200, :body "pong"})]
     ["/.well-known/acme-challenge/*" (static-file-handler letsencrypt-dir)]
     ["*" (redirect-handler 443)]]
    {:conflicts (constantly nil)})
   (ring/create-default-handler)))

(defn start-redirect
  "http server on port 80 that redirects all traffic to 443, except
   /ping which will show pong (useful for debugging) and
   /.well-known/acme-challenge (which is serves static files for certbot)"
  [{:keys [path]
    :or {path ".letsencrypt"}}]
  (let [dir (str path "/public")]
  (info "redirecting http(80) -> https (443), letsencrypt public: " dir)
  (run-jetty-server (handler dir) {:port 80})))


