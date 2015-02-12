(ns clj-bugsnag.ring
  (:require [clj-bugsnag.core :as core]))

(defn wrap-bugsnag
  [handler-fn data]
  (fn [req]
    (try
      (handler-fn req)
      (catch Throwable ex
        (let [req-data (update-in data [:meta] merge {:request (dissoc req :body)})
              verb-path (str (-> req (get :request-method :unknown) name .toUpperCase)
                             " "
                             (:uri req))]
          (core/notify ex (merge {:context verb-path} req-data))
          (throw ex))))))
