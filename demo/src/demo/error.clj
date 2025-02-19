(ns demo.error)


(defn error-handler [_req]
  (throw (ex-info "error handler raised an exception" {:data :test}))
  
  )