(ns brave-ring.impl.adapters
  (:require [brave-ring.impl.headers :as headers])
  (:import [com.github.kristofa.brave KeyValueAnnotation ServerRequestAdapter ServerResponseAdapter TraceData SpanId IdConversion]
           [java.util Collection Collections Arrays]))

(defn get-span-id
  [^String trace-id ^String span-id ^String parent-span-id]
  (SpanId/create (IdConversion/convertToLong trace-id)
                 (IdConversion/convertToLong span-id)
                 (when parent-span-id (IdConversion/convertToLong parent-span-id))))

(deftype RingServerRequestAdapter [request span-name-provider]
  ServerRequestAdapter
  (^TraceData getTraceData [_]
    (let [headers (:headers request)
          sampled (get headers headers/sampled)]
      (cond
        (or (not sampled)) (.. (TraceData/builder)
                               build)
        (or (= "0" sampled) (= "false" sampled))  (.. (TraceData/builder)
                                                      (sample false)
                                                      build)
        :else (let [trace-id (get headers headers/trace-id)
                    span-id (get headers headers/span-id)
                    parent-span-id (get headers headers/parent-span-id)]
                (if (or trace-id span-id)
                  (.. (TraceData/builder)
                      (sample true)
                      (spanId (get-span-id trace-id span-id parent-span-id)))
                  (.. (TraceData/builder)
                    build))))))
  (^String getSpanName [_]
    (span-name-provider request))
  (^Collection requestAnnotations [_]
    (Collections/emptyList)))

(defn ring-server-request-adapter
  [request span-provider]
  (RingServerRequestAdapter. request span-provider))

(deftype RingResponseAdapter [response]
  ServerResponseAdapter
  (^Collection responseAnnotations [_]
    (let [status (:status response)]
      (if (or (< status 200) (> status 299))
        (Arrays/asList (KeyValueAnnotation/create "http.responsecode" (str status)))
        Collections/EMPTY_LIST))))

(defn ring-server-response-adapter
  [response]
  (RingResponseAdapter. response))
