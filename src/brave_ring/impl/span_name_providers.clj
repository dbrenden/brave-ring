(ns brave-ring.impl.span-name-providers)

(def request-method-map
  {:get "GET"
   :post "POST"
   :delete "DELETE"
   :put "PUT"})

(defn default-span-name-provider-fn
  []
  (fn [request]
    ((:request-method request) request-method-map)))
