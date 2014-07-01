(ns clj-bugsnag.core
  (:require [clj-stacktrace.core :refer [parse-exception]]
            [clojure.java.shell :refer [sh]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(defn transform-stacktrace
  [trace-elems]
  (vec (cons :backtrace
         (for [{:keys [file line method]} trace-elems]
           [:line {:file file :lineNumber line :method method}]))))

(defn post-data
  [exception data]
  (let [ex (parse-exception exception)]
    {:apiKey (:api-key data)
     :notifier {:name "clj-bugsnag"
                :version "0.1.0"
                :url "http://github.com/6wunderkinder/clj-bugsnag"}
     :events [{:payloadVersion "2"
               :exceptions [{:errorClass (.getName (:class ex))
                             :message (:message ex)
                             :stacktrace (transform-stacktrace (:trace-elems ex))}]
               :groupingHash (or (:group data) (:class ex))
               :severity (or (:severity data) "error")
               :app {:version (:out (sh "git" "rev-parse" "HEAD"))
                     :releaseStage (or (:environment data) "production")}
               :device {:hostname (.. java.net.InetAddress getLocalHost getHostName)}
               :metaData (or (:meta data) [])}]}))


(defn notify
  [exception data]
  (let [params (post-data exception data)]
    @(http/post "https://notify.bugsnag.com/" (json/write-str params))


    ))
