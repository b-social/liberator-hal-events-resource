(ns liberator-hal-events-resource.stubs.data
  (:require
    [clj-time.core :as time]
    [faker.lorem :as lorem])
  (:import
    [java.util UUID]))

(defn random-uuid []
  (str (UUID/randomUUID)))

(defn random-created-at []
  (time/now))

(def url-template "https://%s.com/%s/%s")

(defn random-url []
  (let [words (take 2 (lorem/words))
        id (random-uuid)]
    (format url-template (first words) (last words) id)))

(defn make-random-event
  ([] (make-random-event {}))
  ([overrides]
    (merge {:id         (random-uuid)
            :type       (rand-nth [:test-event-1 :test-event-2])
            :payload    {:id       (random-uuid)
                         :status   (random-uuid)
                         :customer (random-uuid)}
            :creator    (random-url)
            :created-at (random-created-at)}
      overrides)))