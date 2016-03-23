# brave-ring

Ring middleware with Server Request, Response interceptors

## Usage

Add brave-middleware to your ring handlers.

```clojure
(ns foo
  (:require [compojure
             [core :refer [routes]]]
            [immutant.web :as web]
            [immutant.web.undertow :as undertow]
            [brave-ring.core :as br]))

(defn build-handler
  [handler]
  (-> handler
      brave-middleware))

(defn start-server []
  (web/run (build-handler (routes)) {:port 8080 :host "0.0.0.0"}))
```

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
