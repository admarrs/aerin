(ns app.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :current-route
 (fn [db]
   (:current-route db)))

(reg-sub
  :events
  (fn [db _]
    (:events db)))