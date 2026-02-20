(ns demo.error)

(defn bad-handler [_req]
  (throw (ex-info "error handler raised an exception" {:data :test})))