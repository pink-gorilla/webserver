(ns modular.webserver.https.core
  (:require
   [babashka.fs :as fs]
   [taoensso.timbre :as timbre :refer [info error]]
   [ring.util.response :as response]
   [reitit.ring :as ring]
   [modular.webserver.server.jetty :refer [run-jetty-server]]))

(defn redirect-handler [{:keys [port]
                 :or {port 8080}}]
  (fn [request]
  (info"redirecting request: " request)
  (let [host (:server-name request)
        uri (:uri request)
        query-string (:query-string request)
        scheme (name (:scheme request))
        redirect-url (str scheme "://" host ":" port uri (when query-string (str "?" query-string)))]
    (response/redirect redirect-url))))

(defn static-file-handler [dir]
  (let [acme-dir (str dir "/.well-known/acme-challenge")
        rh  (ring/create-file-handler {:root dir :path "/"})]
    (fs/create-dirs acme-dir)
    (fn [req]
      (info "letsencrypt resourse req: " req)
      (rh req))))

(defn handler [letsencrypt-dir]
  (ring/ring-handler
   (ring/router
    [["/ping" (fn [req] (info "ping!") {:status 200, :body "pong"})]
     ["*" (static-file-handler letsencrypt-dir)]
     ["*" (redirect-handler 443)]]
    {:conflicts (constantly nil)})
   (ring/create-default-handler)))

(defn start-redirect
  "http server on port 80 that redirects all traffic to 443, except
   /ping which will show pong (useful for debugging) and
   /.well-known/acme-challenge (which is serves static files for certbot)"
  [{:keys [letsencrypt]
    :or {letsencrypt {:webroot-path "public"}}}]
  (let [dir (or (:webroot-path letsencrypt) "public")]
  (info "starting http -> https redirect server on port 80, redirecting to: 443")
  (run-jetty-server (handler dir) {:port 80})))


