(ns app.index
  (:require [reagent.dom   :as rd]
            [re-frame.core :refer [dispatch]]
            [app.views     :refer [app]]
            [app.routes    :as r]
            [app.effects]
            [app.events]
            [app.local-storage]
            [app.subs]))

(defn render
  []
  (rd/render [app] (js/document.getElementById "app")))

(defn ^:export main!
  []
  (dispatch [:initialise-db])
  (r/init!)
  (render))

(main!)