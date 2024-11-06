(ns liberator-hal-events-resource.events-resource
  (:require

   [hype.core :as hype]

   [liberator-mixin.json.core :refer [with-json-mixin]]
   [liberator-mixin.validation.core :refer [with-validation-mixin]]
   [liberator-mixin.hypermedia.core :refer [with-hypermedia-mixin]]
   [liberator-mixin.hal.core :refer [with-hal-mixin]]
   [liberator-hal-events-resource.base :as base]))

(defn events-url-for [request routes]
  (base/events-url-for request routes hype/absolute-url-for))

(def load-and-transform-events base/load-and-transform-events)

(defn events-link-for [request routes query-params]
  (base/events-link-for request routes query-params hype/absolute-url-for))

(defn self-link-for [request routes since page-size]
  (base/self-link-for request routes since page-size hype/absolute-url-for))

(defn next-link-for [request routes events page-size]
  (base/next-link-for request routes events page-size hype/absolute-url-for))

(def EventsLoader base/EventsLoader)

(defn build-events-resource [dependencies
                             default-page-size
                             events-loader
                             events-transformer-fn]
  (base/build-events-resource
   dependencies
   default-page-size
   events-loader
   events-transformer-fn
   hype/absolute-url-for
   [(with-json-mixin dependencies)
    (with-validation-mixin dependencies)
    (with-hypermedia-mixin dependencies)
    (with-hal-mixin dependencies)
    (base/with-unauthorised-handling)]))