(ns clj-bugsnag.core-test
  (:require [midje.sweet :refer :all]
            [environ.core :refer [env]]
            [clj-bugsnag.core :as core]))

(fact "includes ExceptionInfo's ex-data"
  (-> (core/post-data (ex-info "BOOM" {:wat "?!"}) {})
    :events first (get-in [:metaData "ex–data" ":wat"]))
  => "?!")

(fact "converts metadata values to strings"
  (-> (core/post-data (ex-info "BOOM" {}) {:meta {:reason println}})
    :events first (get-in [:metaData ":reason"]))
  => (has-prefix "clojure.core$println@"))

(defn make-crash
  "A function that will crash"
  ;; A comment for padding
  []
  (.crash nil)
  ;;    

  ;; /end to check for 3 lines before and after
  )

(fact "includes source code of crash-site"
  (try
    (make-crash)
    (catch Exception ex
      (-> (core/post-data ex nil) :events first :exceptions first :code)
      => {17 "  \"A function that will crash\""
          18 "  ;; A comment for padding"
          19 "  []"
          20 "  (.crash nil)"
          21 "  ;;"
          22 ""
          23 "  ;; /end to check for 3 lines before and after"})))

(fact "falls back to BUGSNAG_KEY environment var for :apiKey"
  (-> (core/post-data (ex-info "BOOM" {}) {}) :apiKey) => ..bugsnag-key..
  (provided
    (env :bugsnag-key) => ..bugsnag-key..))
