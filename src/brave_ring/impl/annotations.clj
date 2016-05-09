(ns brave-ring.impl.annotations
  (:require [clojure.tools.logging :as log])
  (:import [com.github.kristofa.brave KeyValueAnnotation]))

(defn create-annotation
  [[key value]]
  (KeyValueAnnotation/create key (or (str value) "")))

(defn request-annotations-fn
  [type]
  (fn [request]
    (let [{:keys [uri query-string server-port server-name remote-addr scheme protocol content-type]} request
          request-type (if (= type :client)
                         "Client"
                         "Server")]
      (log/debugf "Brave %s Request contents:\n%s" request-type (with-out-str (clojure.pprint/pprint {:request request})))
      (mapv create-annotation [["http.uri" uri]
                               ["http.query-string" query-string]
                               ["http.server-port" server-port]
                               ["http.server-name" server-name]
                               ["http.remote-addr" remote-addr]
                               ["http.scheme" (name scheme)]
                               ["http.protocol" protocol]
                               ["http.content-type" content-type]]))))

(defn response-annotations-fn
  []
  (fn [response]
    [(create-annotation ["http.responsecode" (str (:status response))])]))
