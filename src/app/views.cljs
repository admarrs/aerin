(ns app.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [app.login.views    :as login]
            [app.map :refer [map-wrapper]]))

(defn login
  []
  [:div {:class "p-4 flex-1 content-center"}
   [login/form]])

(defn map-view
  []
  (let [events @(rf/subscribe [:events])]
    [map-wrapper events]))

(defn app
  []
  (let [logged-in?    @(rf/subscribe [:logged-in?])
        user          @(rf/subscribe [:user])
        current-route @(rf/subscribe [:current-route])]
    ;[map-wrapper points]
    [:div {:class "flex flex-col h-screen"}
     ;(if-not (and user (= (-> current-route :data :name) :mission))
     ;  [nav {:current-route current-route :user user}])
     (when current-route
       [(-> current-route :data :view) (-> current-route :data)])]))
