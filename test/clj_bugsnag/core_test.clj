(ns clj-bugsnag.core-test
  (:require [midje.sweet :refer :all]
            [clj-bugsnag.core :as core]))

(fact "includes ExceptionInfo's ex-data"
  (-> (core/post-data (ex-info "BOOM" {:wat "?!"}) {})
    :events first (get-in [:metaData "ex–data" ":wat"]))
  => "?!")

(fact "converts metadata values to strings"
  (-> (core/post-data (ex-info "BOOM" {}) {:meta {:reason println}})
    :events first (get-in [:metaData ":reason"]))
  => (has-prefix "clojure.core$println@"))
