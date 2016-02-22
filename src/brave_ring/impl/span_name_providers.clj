(ns brave-ring.impl.span-name-providers)

(def request-methods
  {:get "GET"
   :post "POST"
   :delete "DELETE"
   :put "PUT"
   :options "OPTIONS"
   :head "HEAD"})

(defn default-span-name-provider-fn
  []
  (fn [request]
    ((:request-method request) request-methods)))
