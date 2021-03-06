(ns brave-ring.core
  (:require [brave-ring.impl.adapters :as adapters]
            [brave-ring.impl.span-name-providers :as snp]
            [brave-ring.impl.annotations :as ann])
  (:import [com.github.kristofa.brave Brave$Builder Brave]))

(defn ring-server-request-adapter
  ([request span-name-provider request-annotations-fn]
   (adapters/ring-server-request-adapter request span-name-provider request-annotations-fn))
  ([request] (ring-server-request-adapter request (snp/default-span-name-provider-fn) (ann/request-annotations-fn :server))))

(defn ring-server-response-adapter
  [response]
  (adapters/ring-server-response-adapter response (ann/response-annotations-fn)))

(def brave-singleton
  (memoize
   (fn []
     (.. (Brave$Builder.)
         build))))

(defn brave-middleware
  ([] (brave-middleware (brave-singleton)))
  ([handler ^Brave brave]
   (fn [request]
     (let [server-request-interceptor (.serverRequestInterceptor ^Brave brave)
           request-adapter (ring-server-request-adapter request)]
       (.handle server-request-interceptor request-adapter)
       (let [response (handler request)
             server-response-interceptor (.serverResponseInterceptor ^Brave brave)
             response-adapter (ring-server-response-adapter response)]
         (.handle server-response-interceptor response-adapter)
         response)))))
