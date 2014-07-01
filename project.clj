(defproject clj-bugsnag "0.1.0"
  :description "Bugsnag client"
  :url "http://github.com/6wunderkinder/clj-bugsnag"
  :min-lein-version "2.3.3"
  :dependencies [
    [clj-stacktrace "0.2.7"]
    [http-kit "2.1.16"]
    [org.clojure/data.json "0.2.4"]
    [org.clojure/clojure "1.6.0"]]
  :profiles {
    :dev {
      :dependencies [[midje "1.6.3"]]
      :plugins      [[lein-midje "3.1.3"]]}})
