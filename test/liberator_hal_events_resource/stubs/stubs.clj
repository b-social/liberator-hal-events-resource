(ns liberator-hal-events-resource.stubs.stubs
  (:require
    [jason.core :refer [defcoders]]

    [liberator-hal-events-resource.events-resource
     :refer [EventsLoader]]))

(declare <-wire-json)
(defcoders wire)

(defn call-resource [resource request]
  (-> (resource request)
    (update :body <-wire-json)))

(defrecord StubEventsLoader [events]
  EventsLoader
  (load-events [_ {:keys [since order pick]}]
    (let [index (.indexOf (map :id events) since)
          events (if (= order "DESC")
                   (reverse events)
                   events)]
      (if (< 0 index)
        (subvec events (inc index) (+ index pick))
        (take pick events)))))
