(ns liberator-hal-events-resource.events-resource-test
  (:require [clojure.test :refer :all]
            [liberator-hal-events-resource.events-resource :refer :all]
            [ring.mock.request :as ring]
            [org.bovinegenius.exploding-fish :refer [absolute?]]
            [halboy.resource :as hal]
            [ring.middleware.params :as params]
            [clojure.string :refer [ends-with?]]
            [liberator-hal-events-resource.stubs.data :as data]
            [liberator-hal-events-resource.events :as events]
            [liberator-hal-events-resource.stubs.stubs :as stubs]
            ))

(deftest events-resource-GET-on-success
  (let [routes [["/events" :events]]
        event-1 (data/make-random-event)
        event-2 (data/make-random-event)
        events-resource (build-events-resource
                          {:routes routes} "10"
                          (stubs/->StubEventsLoader [event-1 event-2])
                          events/event->resource)
        result (stubs/call-resource
                 events-resource
                 (ring/request :get "/events"))
        resource (halboy.json/map->resource (:body result))]

    (testing "transform the event correctly"
      (is (= [(:id event-1) (:id event-2)]
            (map #(:id (hal/get-property % :event))
              (hal/get-resource resource :events)))))))

(deftest events-resource-GET-on-no-events-found
  (let [routes [["/events" :events]]
        events-resource (build-events-resource
                          {:routes routes} "10"
                          (stubs/->StubEventsLoader [])
                          events/event->resource)
        result (stubs/call-resource
                 events-resource
                 (ring/request :get "/events"))
        resource (halboy.json/map->resource (:body result))]
    (testing "the list of event links is empty"
      (is (= [] (hal/get-link resource :events))))

    (testing "the list of embedded events is empty"
      (is (= [] (hal/get-resource resource :events))))))

(deftest events-resource-GET-on-events-found
  (let [routes [["/events" :events]]
        first-event-id (data/random-uuid)
        second-event-id (data/random-uuid)
        third-event-id (data/random-uuid)
        event-1 (data/make-random-event {:id first-event-id})
        event-2 (data/make-random-event {:id second-event-id})
        event-3 (data/make-random-event {:id third-event-id})
        events-resource (build-events-resource
                          {:routes routes} "10"
                          (stubs/->StubEventsLoader [event-1
                                                     event-2
                                                     event-3])
                          events/event->resource)
        result (stubs/call-resource
                 events-resource
                 (ring/request :get "/events"))
        resource (halboy.json/map->resource (:body result))
        events (hal/get-resource resource :events)]

    (testing "returns links to those events"
      (is (= [first-event-id second-event-id third-event-id]
            (map #(-> %
                    (hal/get-property :event)
                    :id) events))))))
(deftest events-resource-GET-on-page-size-specified
  (let [routes [["/events" :events]]
        first-event-id (data/random-uuid)
        second-event-id (data/random-uuid)
        third-event-id (data/random-uuid)
        event-1 (data/make-random-event {:id first-event-id})
        event-2 (data/make-random-event {:id second-event-id})
        event-3 (data/make-random-event {:id third-event-id})
        events-resource (params/wrap-params
                          (build-events-resource
                            {:routes routes} "10"
                            (stubs/->StubEventsLoader [event-1
                                                       event-2
                                                       event-3])
                            events/event->resource))
        page-size 2
        first-result (stubs/call-resource
                       events-resource
                       (ring/request :get "/events" {:pick page-size}))
        first-resource (halboy.json/map->resource (:body first-result))
        first-page (hal/get-resource first-resource :events)

        query-params (-> first-resource
                       (hal/get-link :next)
                       (:query))

        second-result (stubs/call-resource
                        events-resource
                        (ring/request :get "/events" query-params))
        second-page (halboy.json/map->resource (:body second-result))]
    (testing "returns ids to those events"
      (is (= [first-event-id second-event-id]
            (->>
              first-page
              (map #(hal/get-property % :event))
              (map :id)))))


    (testing "provides a next link which goes to the next page"
      (let [event-resources (->
                              second-page
                              (hal/get-resource :events))
            event-ids (->> event-resources
                        (map #(:id (hal/get-property % :event))))]
        (is (= [third-event-id] event-ids))))))

(deftest events-resource-GET-on-order-specified
  (let [routes [["/events" :events]]
        first-event-id (data/random-uuid)
        second-event-id (data/random-uuid)
        third-event-id (data/random-uuid)
        event-1 (data/make-random-event {:id first-event-id})
        event-2 (data/make-random-event {:id second-event-id})
        event-3 (data/make-random-event {:id third-event-id})
        events-resource (params/wrap-params
                          (build-events-resource
                            {:routes routes}
                            "10"
                            (stubs/->StubEventsLoader [event-1
                                                       event-2
                                                       event-3])
                            events/event->resource))
        first-result (stubs/call-resource
                       events-resource
                       (ring/request :get "/events" {:order "DESC"}))

        resource (halboy.json/map->resource (:body first-result))
        page (hal/get-resource resource :events)]

    (testing "returns ids to those events"
      (is (= [third-event-id second-event-id first-event-id]
            (->>
              page
              (map #(hal/get-property % :event))
              (map :id)))))))

(deftest events-resource-GET-on-last-page
  (let [routes [["/events" :events]]
        first-event-id (data/random-uuid)
        event-1 (data/make-random-event {:id first-event-id})
        events-resource (params/wrap-params
                          (build-events-resource
                            {:routes routes} "10"
                            (stubs/->StubEventsLoader [event-1])
                            events/event->resource))
        page-size 1

        first-result (stubs/call-resource
                       events-resource
                       (ring/request :get "/events" {:pick page-size}))
        first-resource (halboy.json/map->resource (:body first-result))]
    (testing "the next link points to the same page"
      (is (= (get-in (hal/get-link first-resource :next) [:query :since])
            first-event-id)))))
