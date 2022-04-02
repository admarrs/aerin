(ns app.login.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :user
 (fn [db _]
   (:user db)))

(rf/reg-sub
 :logged-in?
 (fn [db _]
   (:logged-in? db)))