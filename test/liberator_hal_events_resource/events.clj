(ns liberator-hal-events-resource.events
  (:require
    [halboy.resource :as hal]))

(defmulti event->resource (fn [_ _ e] (:type e)))

(defmethod event->resource :test-event-1
  [request routes event]
  (->
    (hal/new-resource)
    (hal/add-properties
      {:event event})))

(defmethod event->resource :test-event-2
  [request routes event]
  (->
    (hal/new-resource)
    (hal/add-properties
      {:event event})))
