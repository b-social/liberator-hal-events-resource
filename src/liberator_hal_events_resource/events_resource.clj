(ns liberator-hal-events-resource.events-resource
  (:require
   [clojure.walk :as walk]
   [halboy.resource :as hal]
   [hype.core :as hype]
   [liberator-mixin.core :refer [build-resource]]
   [liberator-mixin.hal.core :refer [with-hal-mixin]]
   [liberator-mixin.hypermedia.core :refer [with-hypermedia-mixin]]
   [liberator-mixin.json.core :refer [with-json-mixin]]
   [liberator-mixin.validation.core :refer [with-validation-mixin]]))

(defn with-unauthorised-handling
  []
  {:handle-forbidden
   (fn [_]
     (hal/new-resource))})

(defn load-and-transform-events 
  [events-loader-fn events-transformer-fn]
  (let [events (events-loader-fn)
        event-resources (pmap events-transformer-fn events)
        event-links (map #(hal/get-link % :self) event-resources)]
    [events event-resources event-links]))

(defn events-link-for
  [request routes query-params options]
  {:href (hype/absolute-url-for request routes
                                (:route-key options :events)
                                {:query-params query-params})})

(defn self-link-for
  [request routes since page-size options]
  (events-link-for request
                   routes
                   (merge
                    (walk/keywordize-keys (:query-params request))
                    {:pick page-size}
                    (when-not (nil? since)
                      {:since since}))
                   options))

(defn next-link-for
  [request routes events page-size options]
  (let [since (:id (last events))]
    (events-link-for request
                     routes
                     (merge
                      (walk/keywordize-keys (:query-params request))
                      {:since  since
                       :pick   page-size})
                     options)))

(defn add-next-link
  [resource request routes events page-size options]
  (if-not (empty? events)
    (hal/add-link resource
                  :next
                  (next-link-for request routes events page-size options))
    resource))

(defprotocol EventsLoader
  (load-events [this parameters]))

(defn build-events-resource
  ([dependencies
    default-page-size
    events-loader
    events-transformer-fn]
   (build-events-resource
    dependencies
    default-page-size
    events-loader
    events-transformer-fn
    {}))
  ([dependencies
    default-page-size
    events-loader
    events-transformer-fn
    options]
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
       (fn [{:keys [request] :as context}]
         (let [params (:params request)
               since (get params :since nil)
               order (.toUpperCase (get params :order "ASC"))
               page-size (get params :pick default-page-size)
               [events event-resources event-links]
               (load-and-transform-events
                #(load-events
                  events-loader
                  (merge context params {:since since :pick page-size :order order}))
                #(events-transformer-fn dependencies request routes %))]
           
             (->
              (hal/new-resource)
              (hal/add-links
               {:self   (self-link-for request routes since page-size options)
                :events event-links})
              (add-next-link request routes events page-size options)
              (hal/add-resource :events event-resources))))}
      (when (:overrides options)
        (:overrides options))))))