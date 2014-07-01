(ns clj-bugsnag.core)

(defn notify
  [exception data]
  (let [params {:apiKey ""
                :notifier {:name "clj-bugsnag"
                           :version "0.1.0"
                           :url "http://github.com/6wunderkinder/clj-bugsnag"}
                :event {:payloadVersion "2"
                        :exceptions [{:errorClass ""
                                      :message ""
                                      :stacktrace [{:file ""
                                                    :lineNumber 1
                                                    :columnNumber 1
                                                    :method ""
                                                    :inProject true}]}]
                        :context ""
                        :groupingHash ""
                        :severity "error"
                        :user {:id 1
                               :name ""
                               :email ""}
                        :app {:version ""
                              :releaseStage "production"}
                        :device {:osVersion ""
                                 :hostname ""}
                        :metaData {}}
                }])
  )

(foo "Ben")
