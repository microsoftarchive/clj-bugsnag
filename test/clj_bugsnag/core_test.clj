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
      => {16 "  \"A function that will crash\""
          17 "  ;; A comment for padding"
          18 "  []"
          19 "  (.crash nil)"
          20 "  ;;"
          21 ""
          22 "  ;; /end to check for 3 lines before and after"})))
