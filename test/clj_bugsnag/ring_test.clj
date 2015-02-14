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

(def user-fn identity)

(facts "about :user-from-request"
  (let [handler (fn [req] (throw (ex-info "BOOM" {})))
        wrapped (ring/wrap-bugsnag handler {:user-from-request #'user-fn})]
    (fact "middleware uses user-from-request function"
      (wrapped {}) => (throws #"BOOM")
      (provided
        (user-fn {}) => {:id ..user-id..}
        (core/notify anything (contains {:user {:id ..user-id..}})) => nil))
    
    (fact "creates map when function returns string"
      (wrapped {}) => (throws #"BOOM")
      (provided
        (user-fn {}) => "..user-id.."
        (core/notify anything (contains {:user {:id "..user-id.."}})) => nil))))
