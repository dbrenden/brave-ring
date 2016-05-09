(ns brave-ring.impl.adapters
  (:require [brave-ring.impl.headers :as headers])
  (:import [com.github.kristofa.brave ServerRequestAdapter ServerResponseAdapter TraceData SpanId IdConversion]
           [java.util Collection Collections Arrays]))

(defn get-span-id
  [^String trace-id ^String span-id ^String parent-span-id]
  (SpanId/create (IdConversion/convertToLong trace-id)
                 (IdConversion/convertToLong span-id)
                 (when parent-span-id (IdConversion/convertToLong parent-span-id))))

(deftype RingServerRequestAdapter [request get-span-name request-annotations-fn]
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
    (request-annotations-fn request)))

(defn ring-server-request-adapter
  [request span-provider request-annotations-fn]
  (RingServerRequestAdapter. request span-provider request-annotations-fn))

(deftype RingResponseAdapter [response response-annotations-fn]
  ServerResponseAdapter
  (^Collection responseAnnotations [_]
    (response-annotations-fn response)))

(defn ring-server-response-adapter
  [response response-annotations-fn]
  (RingResponseAdapter. response response-annotations-fn))
