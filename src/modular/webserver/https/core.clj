(ns modular.webserver.https.core
  (:require
   [babashka.fs :as fs]
   [ring.util.response :as response]
   [reitit.ring :as ring]
   [modular.webserver.server.jetty :refer [run-jetty-server]]))

(defn redirect-to-port-8080 [request]
  (println "redirecting request: " request)
  (let [host (:server-name request)
        uri (:uri request)
        query-string (:query-string request)
        scheme (name (:scheme request))
        redirect-url (str scheme "://" host ":8080" uri (when query-string (str "?" query-string)))]
    (response/redirect redirect-url)))

(defn static-file-handler [dir]
  (let [acme-dir (str dir "/.well-known/acme-challenge")
        rh  (ring/create-file-handler {:root dir :path "/"})
        ]
    (fs/create-dirs acme-dir)  
    (fn [req]
      
      (println "res req: " req)
      (rh req)
      )
   
    )
 
  )

(def handler
  (ring/ring-handler
   (ring/router
    [["/ping" (fn [req] (println "ping!") {:status 200, :body "pong"})]
     ["*" (static-file-handler "public")]
     ["*" redirect-to-port-8080]
     ]
    {:conflicts (constantly nil)})
   (ring/create-default-handler)
   ))

(defn start-redirect []
  (run-jetty-server handler {:port 80}))


