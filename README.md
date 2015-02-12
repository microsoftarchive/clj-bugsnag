

# clj-bugsnag

A fully fledged Bugsnag client for Clojure.


## Features

 - Automatically exposes ex-info data as metadata
 - Ring middleware included, attaches ring request map as metadata
 - Mark project stack traces
 - Used in production at [6 Wunderkinder](http://www.6wunderkinder.com/)


## Releases and Dependency Information

clj-bugsnag is released via [Clojars](https://clojars.org/clj-bugsnag). The Latest stable release is 0.1.2

[Leiningen](https://github.com/technomancy/leiningen) dependency information:

```clojure
[clj-bugsnag "0.1.2"]
```

Maven dependency information:

```xml
<dependency>
  <groupId>clj-bugsnag</groupId>
  <artifactId>clj-bugsnag</artifactId>
  <version>0.1.2</version>
</dependency>
```


## Example Usage

```clojure
(require '[clj-bugsnag.core :as bugsnag]
         '[clj-bugsnag.ring :as bugsnag.ring])

;; Ring middleware:
(bugsnag.ring/wrap-bugsnag handler {:api-key "Project API key"
                                    :environment "production"
                                    :project-ns "your-project-ns-prefix"})

;; Manual reporting:
(try
  (some-function-that-could-crash some-input)
  (catch Exception ex
    (bugsnag/notify ex
      {:api-key "Project API key"
       ;; Attach custom metadata to create tabs in Bugsnag:
       :meta {:input some-input}})
```


## License

Copyright © 2014-2015 6 Wunderkinder GmbH.

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
