(ns webserver.https.letsencrypt
  (:require
   ;[clojure.string :as str]
   [taoensso.timbre :as timbre :refer [info error]]
   [babashka.fs :as fs]
   [babashka.process :refer [shell]]
   [webserver.default :refer [letsencrypt-default https-default]]))

(defn renew-cert [{:keys [path domain email force-renewal]
                   :or {path (:path letsencrypt-default)
                        force-renewal false}}]
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
  ; When using the webroot method the Certbot client places a challenge response inside domain.com/.well-known/acme-challenge/ 
  ; which is used for validation. When validation is complete, challenge file is removed from the target directory
    (let [r (if force-renewal
              (shell {:out :string}
                     "certbot" "certonly"
                     "--non-interactive" "--agree-tos"
                     "-m" email
                     "--webroot" "--webroot-path" webroot-path
                     "-d" domain
                     "--work-dir" work-path
                     "--config-dir" config-path
                     "--logs-dir" log-path
                     "--force-renewal")
              (shell {:out :string}
                   "certbot" "certonly"
                   "--non-interactive" "--agree-tos"
                   "-m" email
                   "--webroot" "--webroot-path" webroot-path
                   "-d" domain
                   "--work-dir" work-path
                   "--config-dir" config-path
                   "--logs-dir" log-path))]
      ;(info "renewal out: " (-> r :out))
      ;(info "first line: " (-> r :out str/split-lines first))
      ; first line:  Account registered.
      ; first line:  Certificate not yet due for renewal
      r)))

(defn convert-cert
  "converts a letsencrypt certificate to a jetty certificate.
   throws on failure"
  [{:keys [path domain _email]
    :or {path (:path letsencrypt-default)}
    :as letsencrypt_opts}
   {:keys [certificate password]
    :or {certificate (:certificate https-default)
         password (:password https-default)}
    :as https_opts}]
  (assert (map? letsencrypt_opts) "letsencrypt_opts needs to be a map")
  (assert (map? https_opts) "https_opts needs to be a map")
  (assert domain ":domain key needs to be passed")
  (assert (string? domain) ":domain key needs to be a string")
  (let [letsencrypt-config-path (str path "/config")
        certificate-path (-> certificate fs/parent str)
        letsencrypt-domain-cert-path (str letsencrypt-config-path "/live/" domain "/")
        letsencrypt-chain-pem (str letsencrypt-domain-cert-path "chain.pem")
        letsencrypt-fullchain-pem (str letsencrypt-domain-cert-path "fullchain.pem")
        letsencrypt-privkey-pem (str letsencrypt-domain-cert-path "privkey.pem")]
    (assert (fs/exists? letsencrypt-domain-cert-path)
            (str "letsencrypt domain dir does not exist: " letsencrypt-domain-cert-path))
    (assert (fs/exists? letsencrypt-chain-pem)
            (str "letsencrypt file does not exist: " letsencrypt-chain-pem))
    (assert (fs/exists? letsencrypt-fullchain-pem)
            (str "letsencrypt file does not exist: " letsencrypt-fullchain-pem))
    (assert (fs/exists? letsencrypt-privkey-pem)
            (str "letsencrypt file does not exist: " letsencrypt-privkey-pem))
    (info "creating certificate path: " certificate-path)
    (fs/create-dirs certificate-path)
    (shell "openssl" "pkcs12"
           "-export"
           "-CAfile" letsencrypt-chain-pem
           "-caname" "root"
           "-in" letsencrypt-fullchain-pem
           "-inkey" letsencrypt-privkey-pem
           "-out" certificate
           "-name" "something" ; can be anything
           "-passout" (str "pass:" password))
    (shell
     "chmod" "a+r" certificate)))
