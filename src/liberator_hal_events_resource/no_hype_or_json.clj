(ns liberator-hal-events-resource.no-hype-or-json
  (:require

   [liberator-mixin.validation.core :refer [with-validation-mixin]]
   [liberator-mixin.hypermedia.core :refer [with-hypermedia-mixin]]
   [liberator-mixin.hal.core :refer [with-hal-mixin]]
   [liberator-hal-events-resource.base :as base]))

(def EventsLoader base/EventsLoader)

(defn build-events-resource [dependencies
                             default-page-size
                             events-loader
                             events-transformer-fn
                             absolute-url-for]
  (base/build-events-resource
   dependencies
   default-page-size
   events-loader
   events-transformer-fn
   absolute-url-for
   [(with-validation-mixin dependencies)
    (with-hypermedia-mixin dependencies)
    (with-hal-mixin dependencies)
    (base/with-unauthorised-handling)]))