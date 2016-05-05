(ns brave-ring.impl.adapters
  (:require [brave-ring.impl.headers :as headers])
  (:import [com.github.kristofa.brave KeyValueAnnotation ServerRequestAdapter ServerResponseAdapter TraceData SpanId IdConversion]
           [java.util Collection Collections Arrays]))

(defn get-span-id
  [^String trace-id ^String span-id ^String parent-span-id]
  (SpanId/create (IdConversion/convertToLong trace-id)
                 (IdConversion/convertToLong span-id)
                 (when parent-span-id (IdConversion/convertToLong parent-span-id))))

(deftype RingServerRequestAdapter [request get-span-name]
  ServerRequestAdapter
  (^TraceData getTraceData [_]
    (let [headers (:headers request)
          sampled (get headers headers/sampled)]
      (cond
        (not sampled) (.. (TraceData/builder)
                          build)
        (or (= "0" sampled) (= "false" sampled))  (.. (TraceData/builder)
                                                      (sample false)
                                                      build)
        :else (let [trace-id (get headers headers/trace-id)
                    span-id (get headers headers/span-id)
                    parent-span-id (get headers headers/parent-span-id)]
                (if (and trace-id span-id)
                  (.. (TraceData/builder)
                      (sample true)
                      (spanId (get-span-id trace-id span-id parent-span-id))
                      build)
                  (.. (TraceData/builder)
                    build))))))
  (^String getSpanName [_]
    (get-span-name request))
  (^Collection requestAnnotations [_]
    (let [{:keys [uri query-string server-port server-name remote-addr scheme protocol content-type]} request]
      (mapv #(KeyValueAnnotation/create (first %) (str (or (second %) "none"))) [["http.uri" uri]
                                                                                 ["http.query-string" query-string]
                                                                                 ["http.server-port" server-port]
                                                                                 ["http.server-name" server-name]
                                                                                 ["http.remote-addr" remote-addr]
                                                                                 ["http.scheme" scheme]
                                                                                 ["http.protocol" protocol]
                                                                                 ["http.content-type" content-type]]))))


(defn ring-server-request-adapter
  [request span-provider]
  (RingServerRequestAdapter. request span-provider))

(deftype RingResponseAdapter [response]
  ServerResponseAdapter
  (^Collection responseAnnotations [_]
    (let [status (:status response)]
      (if (or (< status 200) (> status 299))
        [(KeyValueAnnotation/create "http.responsecode" (str status))]
        Collections/EMPTY_LIST))))

(defn ring-server-response-adapter
  [response]
  (RingResponseAdapter. response))
