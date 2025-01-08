(ns modular.webserver.https.letsencrypt
  (:require
   [babashka.fs :as fs]
   [babashka.process :refer [shell]]))

(defn renew-cert [{:keys [path domain email]
                   :or {path ".letsencrypt"}}]
  (let [config-path (str path "/config")
        work-path (str path "/work")
        log-path (str path "/log")
        webroot-path (str path "/public")]
  (fs/create-dirs config-path)
  (fs/create-dirs work-path)
  (fs/create-dirs log-path)
  (fs/create-dirs webroot-path)
  (assert domain "domain needs to be a string and a valid domain (www.demo.com)")  
  (assert email "email needs to be a string and a valid email (webmaster@demo.com)")
  ; certbot either needs to run as root, or set --config-dir, --work-dir, and --logs-dir to writeable paths.
  (shell "certbot" "certonly" ; "sudo"
         "--non-interactive" "--agree-tos"
         "-m" email
         "--webroot" "--webroot-path" webroot-path
         "-d" domain
         "--work-dir" work-path
         "--config-dir" config-path
         "--logs-dir" log-path
         )))

(defn convert-cert
  "converts a letsencrypt certificate to a jetty certificate.
   throws on failure"
  [{:keys [path domain _email]
    :or {path ".letsencrypt"}
    :as _letsencrypt_opts}
   {:keys [certificate-path
           password]
    :or {certificate-path ".https-certificates"
         password "123456789"}
    :as opts}]
  (assert (map? opts) "parameter needs to be a map")
  (assert domain ":domain key needs to be passed")
  (assert (string? domain) ":domain key needs to be a string")
  (let [letsencrypt-config-path (str path "/config")
        dir (str letsencrypt-config-path "/live/" domain)
        https-cert-filename (str certificate-path "/keystore.p12")
        letsencrypt-domain-cert-path (str letsencrypt-config-path "/live/" domain "/")
        letsencrypt-chain-pem (str letsencrypt-domain-cert-path "chain.pem")
        letsencrypt-fullchain-pem (str letsencrypt-domain-cert-path "/" "fullchain.pem")
        letsencrypt-privkey-pem (str letsencrypt-domain-cert-path "/" "privkey.pem")]
    (assert (fs/exists? letsencrypt-domain-cert-path)
            (str "letsencrypt domain dir does not exist: " letsencrypt-domain-cert-path))
    (assert (fs/exists? letsencrypt-chain-pem)
            (str "letsencrypt file does not exist: " letsencrypt-chain-pem))
    (assert (fs/exists? letsencrypt-fullchain-pem)
            (str "letsencrypt file does not exist: " letsencrypt-fullchain-pem))
    (assert (fs/exists? letsencrypt-privkey-pem)
            (str "letsencrypt file does not exist: " letsencrypt-privkey-pem))
    (fs/create-dirs certificate-path)
    (shell {:dir dir}
           "openssl" "pkcs12"
           "-export"
           "-CAfile" "chain.pem"
           "-caname" "root"
           "-in" "fullchain.pem"
           "-inkey" "privkey.pem"
           "-out" https-cert-filename
           "-name" "something" ; can be anything
           "-passout" (str "pass:" password))
    (shell
     "chmod" "a+r" https-cert-filename)))

