(ns clj-bugsnag.core-test
  (:require [midje.sweet :refer :all]
            [environ.core :refer [env]]
            [clj-bugsnag.core :as core]))

(fact "includes ExceptionInfo's ex-data"
  (-> (core/exception->json (ex-info "BOOM" {:wat "?!"}) {})
    :events first (get-in [:metaData "ex–data" ":wat"]))
  => "?!")

(fact "converts metadata values to strings"
  (-> (core/exception->json (ex-info "BOOM" {}) {:meta {:reason println}})
    :events first (get-in [:metaData ":reason"]))
  => (has-prefix "clojure.core$println@"))

(defn make-crash
  "A function that will crash"
  []
  (let [closure (fn []
                  (.crash nil))]

  ;;    
  ;; /end to check for 3 lines before and after

    (closure)))

(fact "includes source in stack traces"
  (try
    (make-crash)
    (catch Exception ex
      (-> (core/exception->json ex nil) :events first :exceptions first :stacktrace second :code)
      => {17 "  \"A function that will crash\""
          18 "  []"
          19 "  (let [closure (fn []"
          20 "                  (.crash nil))]"
          21 ""
          22 "  ;;"
          23 "  ;; /end to check for 3 lines before and after"}
      (-> (core/exception->json ex nil) :events first :exceptions first :stacktrace (nth 2) :code)
      => {22 "  ;;"
          23 "  ;; /end to check for 3 lines before and after"
          24 ""
          25 "    (closure)))"})))

(fact "falls back to BUGSNAG_KEY environment var for :apiKey"
  (-> (core/exception->json (ex-info "BOOM" {}) {}) :apiKey) => ..bugsnag-key..
  (provided
    (env :bugsnag-key) => ..bugsnag-key..))

(fact "includes nested exceptions"
  (let [e1 (Exception. "Inner")
        e2 (Exception. "Middle" e1)
        e3 (Exception. "Outer"  e2)]
    (->> (core/exception->json e3 {}) :events first :exceptions (map :message))
  => ["Inner" "Middle" "Outer"]))
