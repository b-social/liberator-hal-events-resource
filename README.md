# liberator-hal-events-resource
[![Clojars Project](https://img.shields.io/clojars/v/b-social/liberator-hal-events-resource.svg)](https://clojars.org/b-social/liberator-hal-events-resource)

A Clojure library for liberator to load and transform events to HAL resources.

This library uses [Halboy](https://github.com/jimmythompson/halboy) for creating and rendering HAL resources.

```clj
[b-social/liberator-hal-events-resource "0.0.11"]
```

## Usage

You'll need to provide 2 function
- Event loader: how to load your events from their store. (for example your database)
- Event transformer (how to map those events into HAL resources)

``` clojure
(defn events-resource-handler-for [{:keys [events-store] :as dependencies}]
  (events-resource dependencies default-page-size
    (partial db-events-loader events-store) #event-loader-function
    event-mapping/event->resource)) #event-transformer function
```
## License

Copyright B-Social Limited © 2019

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
