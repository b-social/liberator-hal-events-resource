(ns liberator-hal-events-resource.events-resource

  (:require [liberator-mixin.core :refer [build-resource]]
            [liberator-mixin.json.core :refer [with-json-mixin]]
            [liberator-mixin.validation.core :refer [with-validation-mixin]]
            [liberator-mixin.hypermedia.core :refer [with-hypermedia-mixin]]
            [liberator-mixin.hal.core :refer [with-hal-mixin]]
            [liberator-mixin.hypermedia.core :as urls]
            [halboy.resource :as hal]))

(defn with-unauthorised-handling
  []
  {:handle-forbidden
   (fn [_]
     (hal/new-resource))})

(defn events-url-for [request routes]
  (urls/absolute-url-for request routes :events))


(defn load-and-transform-events [events-loader-fn events-transformer-fn]
  (let [events (events-loader-fn)
        event-resources (map events-transformer-fn events)
        event-links (map #(hal/get-link % :self) event-resources)]
    [events event-resources event-links]))

(defn events-link-for [request routes query-params]
  {:href  (events-url-for request routes)
   :query query-params})

(defn self-link-for [request routes since page-size]
  (events-link-for request routes
    (merge
      {:pick page-size}
      (when-not (nil? since)
        {:since since}))))

(defn next-link-for [request routes events page-size]
  (let [since (:id (last events))]
    (events-link-for request routes
      {:since since
       :pick  page-size})))

(defn build-events-resource [dependencies
                       default-page-size
                       events-loader-fn
                       events-transformer-fn]
  (let [routes (:routes dependencies)]
    (build-resource
      (with-json-mixin dependencies)
      (with-validation-mixin dependencies)
      (with-hypermedia-mixin dependencies)
      (with-hal-mixin dependencies)
      (with-unauthorised-handling)
      {:allowed-methods
       [:get]
       :handle-ok
       (fn [{:keys [request]}]
         (let [since (get-in request [:params "since"] nil)
               
               page-size (Integer/parseInt
                           (get-in request [:params "pick"]
                             default-page-size))]
           (let [[events event-resources event-links]
                 (load-and-transform-events
                   #(events-loader-fn since page-size)
                   #(events-transformer-fn request routes %))]
             (->
               (hal/new-resource)
               (hal/add-links
                 {:self   (self-link-for request routes since page-size)
                  :events event-links
                  :next   (if-not (empty? events)
                            (next-link-for
                              request routes events page-size)
                            (self-link-for request routes since page-size))})
               (hal/add-resource :events event-resources)))))})))