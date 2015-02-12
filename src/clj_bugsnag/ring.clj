(ns clj-bugsnag.ring
  (:require [clj-bugsnag.core :as core]))

(defn wrap-bugsnag
  ([handler-fn]
    (wrap-bugsnag handler-fn nil))
  ([handler-fn data]
    (fn [req]
      (try
        (handler-fn req)
        (catch Throwable ex
          (let [user-fn (get data :user-from-request (constantly nil))
                req-data (update-in data [:meta] merge {:request (dissoc req :body)})
                verb-path (str (-> req (get :request-method :unknown) name .toUpperCase)
                               " "
                               (:uri req))]
            (core/notify ex (merge {:context verb-path
                                    :user (user-fn req)} req-data))
            (throw ex)))))))
