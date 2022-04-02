(ns app.interceptors
  (:require [re-frame.core     :as rf]
            [app.local-storage :as local-storage]
            [app.util          :as util]))

(def check-token
  (rf/->interceptor
   :id     ::check-token
   :before (fn [context]
             (let [expired? (some-> (local-storage/ls-get :login-data)
                                    :token
                                    util/jwt-expired?)]
               (prn "Context" context)
               (if expired?
                 (-> context)
                 context)))))