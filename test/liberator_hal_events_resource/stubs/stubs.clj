(ns liberator-hal-events-resource.stubs.stubs
  (:require [liberator-mixin.json.core :as json]))

(defn call-resource [resource request]
  (-> (resource request)
    (update :body json/wire-json->map)
    ))

(defn stub-events-loader [events]
  (fn [since pick]
    (let [index (.indexOf (map :id events) since)]
      (if (< 0 index)
        (subvec events (inc index) (+ index pick))
        (take pick events)))))