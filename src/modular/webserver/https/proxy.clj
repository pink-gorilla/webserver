(ns modular.webserver.https.proxy
  (:require
   [babashka.fs :as fs]
   [taoensso.timbre :as timbre :refer [info error]]
   [ring.util.response :as response]
   [reitit.ring :as ring]
   [ring.adapter.jetty :refer [run-jetty]]
   [modular.webserver.https.letsencrypt :refer [renew-cert convert-cert]]
   ))

(defn redirect-handler [port]
  (fn [{:keys [uri server-name scheme query-string] :as req}]
  (info"redirecting request: " uri)
  (let [redirect-url (str scheme "://" server-name ":" port uri (when query-string (str "?" query-string)))]
    (response/redirect redirect-url))))

(defn static-file-handler [path]
  (let [acme-dir (str path "/.well-known/acme-challenge")
        _ (fs/create-dirs acme-dir)
        rh  (ring/create-file-handler {:root acme-dir :path "/.well-known/acme-challenge/"})]
    (fn [{:keys [uri] :as req}]
      (info "letsencrypt challenge on uri: " uri)
      (rh req))))

(defn certificate-get-handler [{:keys [letsencrypt https] :as config}]
  (fn [_req]
    (info "certificate-get started..")
    (renew-cert letsencrypt)
    (response/response {:body "certificate-get started!"})))

(defn certificate-import-handler [{:keys [letsencrypt https] :as config}]
  (fn [_req]
   (info "certificate-convert started..")
    (convert-cert letsencrypt https)
    (response/response {:body "certificate-import started!"})))
   

(defn start-proxy 
   "http server on port 80 that redirects all traffic to 443, except
     /.well-known/acme-challenge (which is serves static files for certbot) and
     /.well-known/ping which will show pong (useful for debugging)"
  [{:keys [letsencrypt https]
    :as config}]
  (let [{:keys [path]
         :or {path ".letsencrypt"}} letsencrypt
        public-dir (str path "/public")
        handler (ring/ring-handler
                 (ring/router
                  [["/.well-known/ping" (fn [req] (info "ping!") {:status 200, :body "pong"})]
                   ["/.well-known/acme-challenge/*" (static-file-handler public-dir)]
                   ["/.well-known/trigger/certificate-get" (certificate-get-handler config)]
                   ["/.well-known/trigger/certificate-import" (certificate-import-handler config)]
                   ["*" (redirect-handler 443)]]
                  {:conflicts (constantly nil)})
                 (ring/create-default-handler))]
     (info "redirecting http(80) -> https (443), letsencrypt public: " public-dir)
     (run-jetty handler {:port 80
                         :allow-null-path-info true ; omit the trailing slash from your URLs
                         })))


