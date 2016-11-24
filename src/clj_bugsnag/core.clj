(ns clj-bugsnag.core
  (:require [clj-stacktrace.core :refer [parse-exception]]
            [clj-stacktrace.repl :refer [method-str]]
            [clojure.java.shell :refer [sh]]
            [clj-http.client :as http]
            [environ.core :refer [env]]
            [clojure.data.json :as json]
            [clojure.repl :as repl]
            [clojure.string :as string]
            [clojure.walk :as walk]))

(def git-rev
  (delay
    (try
      (string/trim (:out (sh "git" "rev-parse" "HEAD")))
      (catch Exception ex "git revision not available"))))

(defn- find-source-snippet
  [around, function-name]
  (try
    (let [fn-sym (symbol function-name)
          fn-var (find-var fn-sym)
          source (repl/source-fn fn-sym)
          start (-> fn-var meta :line)
          indexed-lines (map-indexed (fn [i, line]
                                        [(+ i start), (string/trimr line)])
                                     (string/split-lines source))]
      (into {} (filter #(<= (- around 3) (first %) (+ around 3)) indexed-lines)))
    (catch Exception ex
      nil)))

(defn- transform-stacktrace
  [trace-elems project-ns]
  (try
    (vec (for [{:keys [file line ns] :as elem} trace-elems
               :let [project? (.startsWith (or ns "_") project-ns)
                     method (method-str elem)
                     code (when (.endsWith (or file "") ".clj")
                            (find-source-snippet line (.replace (or method "") "[fn]" "")))]]
            {:file file,
             :lineNumber line,
             :method method,
             :inProject project?,
             :code code}))
    (catch Exception ex
      [{:file "clj-bugsnag/core.clj",
        :lineNumber 1,
        :code {1 (str ex)
               2 "thrown while building stack trace."}}])))

(defn- stringify
  [thing]
  (if (or (map? thing) (string? thing) (number? thing) (sequential? thing))
    thing
    (str thing)))

(defn- unroll [ex project-ns]
  (loop [collected []
         current ex]
    (let [class-name (.getName (:class current))
          stacktrace (transform-stacktrace (:trace-elems current) project-ns)
          new-item {:errorClass class-name
                    :message (:message current)
                    :stacktrace stacktrace}
          collected (cons new-item collected)]
      (if-let [next (:cause current)]
        (recur collected next)
        collected))))

(defn exception->json
  [exception options]
  (let [ex (parse-exception exception)
        class-name (.getName (:class ex))
        project-ns (get options :project-ns "\000")
        base-meta (if-let [d (ex-data exception)]
                    {"ex–data" d}
                    {})]
    {:apiKey (:api-key options (env :bugsnag-key))
     :notifier {:name "clj-bugsnag"
                :version "0.2.2"
                :url "https://github.com/wunderlist/clj-bugsnag"}
     :events [{:payloadVersion "2"
               :exceptions (unroll ex project-ns)
               :context (:context options)
               :groupingHash (or (:group options)
                               (if (isa? (type exception) clojure.lang.ExceptionInfo)
                                 (:message ex)
                                 class-name))
               :severity (or (:severity options) "error")
               :user (:user options)
               :app {:version (if (contains? options :version)
                                (:version options)
                                @git-rev)
                     :releaseStage (or (:environment options) "production")}
               :device {:hostname (.. java.net.InetAddress getLocalHost getHostName)}
               :metaData (walk/postwalk stringify (merge base-meta (:meta options)))}]}))

(defn notify
  "Main interface for manually reporting exceptions.
   When not :api-key is provided in options,
   tries to load BUGSNAG_KEY var from enviroment."
  ([exception]
    (notify exception nil))
  ([exception, options]
    (let [params (exception->json exception options)
          url "https://notify.bugsnag.com/"]
      (http/post url {:form-params params
                      :content-type :json}))))
