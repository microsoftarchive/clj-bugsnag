(ns clj-bugsnag.ring-test
  (:require [midje.sweet :refer :all]
            [clj-bugsnag.ring :as ring]
            [clj-bugsnag.core :as core]))

(fact "middleware calls notify on exception"
  (let [handler (fn [req] (throw (ex-info "BOOM" {})))
        wrapped (ring/wrap-bugsnag handler {})]
    (wrapped {}) => (throws #"BOOM")
    (provided
      (core/notify anything anything) => nil)))
