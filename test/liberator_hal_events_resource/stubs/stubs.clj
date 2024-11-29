(ns liberator-hal-events-resource.stubs.stubs
  (:require
    [jason.convenience :refer [<-wire-json]]

    [liberator-hal-events-resource.events-resource
     :refer [EventsLoader]]))

(defn call-resource 
  [resource request]
  (-> (resource request)
    (update :body <-wire-json)))

(defrecord StubEventsLoader [events]
  EventsLoader
  (load-events [_ {:keys [since order pick]}]
    (let [ids (map :id events)
          index (.indexOf ids since)
          events (if (= order "DESC")
                   (reverse events)
                   events)]
      (cond
        (nil? since)
        (take pick events)

        (pos? index)
        (subvec events (inc index) (+ index pick))

        :else
        []))))