(ns clj-bugsnag.ring
  (:require [clj-bugsnag.core :as core]))

(defn- catch-call-map?
  "Catches exceptions thrown by user-fn and
   ensure the return value is a map."
  [user-fn req]
  (try
    (let [user (user-fn req)]
      (if (map? user)
        user
        {:id user}))
    (catch Throwable e
      nil)))

(defn wrap-bugsnag
  "Ring middleware, catches exceptions, reports them to Bugsnag
   and re-throws the exception."
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
                                    :user (catch-call-map? user-fn req)} req-data))
            (throw ex)))))))
