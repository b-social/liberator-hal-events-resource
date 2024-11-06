(ns liberator-hal-events-resource.base
  (:require
   [halboy.resource :as hal]

   [liberator-mixin.core :refer [build-resource]]
   [liberator-hal-events-resource.base :as base]))

(defn with-unauthorised-handling
  []
  {:handle-forbidden
   (fn [_]
     (hal/new-resource))})

(defn events-url-for [request routes absolute-url-for]
  (absolute-url-for request routes :events))

(defn load-and-transform-events [events-loader-fn events-transformer-fn]
  (let [events (events-loader-fn)
        event-resources (map events-transformer-fn events)
        event-links (map #(hal/get-link % :self) event-resources)]
    [events event-resources event-links]))

(defn events-link-for [request routes query-params absolute-url-for]
  {:href  (events-url-for request routes absolute-url-for)
   :query query-params})

(defn self-link-for [request routes since page-size absolute-url-for]
  (events-link-for request routes
                   (merge
                    {:pick page-size}
                    (when-not (nil? since)
                      {:since since}))
                   absolute-url-for))

(defn next-link-for [request routes events page-size absolute-url-for]
  (let [since (:id (last events))]
    (events-link-for request routes
                     {:since since
                      :pick  page-size}
                     absolute-url-for)))

(defprotocol EventsLoader
  (load-events [this parameters]))

(defn build-events-resource [dependencies
                             default-page-size
                             events-loader
                             events-transformer-fn
                             absolute-url-for
                             mixins]
  (let [routes (:routes dependencies)]
    (apply build-resource
           (conj mixins
                 {:allowed-methods
                  [:get]
                  :handle-ok
                  (fn [{:keys [request]}]
                    (let [since (get-in request [:params :since] nil)
                          order (.toUpperCase (get-in request [:params :order] "ASC"))
                          page-size (get-in request [:params :pick] default-page-size)]
                      (let [[events event-resources event-links]
                            (load-and-transform-events
                             #(load-events events-loader
                                           {:since since :pick page-size :order order})
                             #(events-transformer-fn dependencies request routes %))]
                        (->
                         (hal/new-resource)
                         (hal/add-links
                          {:self   (self-link-for request routes since page-size absolute-url-for)
                           :events event-links
                           :next   (if-not (empty? events)
                                     (next-link-for
                                      request routes events page-size absolute-url-for)
                                     (self-link-for request routes since page-size absolute-url-for))})
                         (hal/add-resource :events event-resources)))))}))))