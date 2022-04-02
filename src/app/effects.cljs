(ns app.effects
  (:require [re-frame.core :as rf]
            [app.routes    :as routes]))

(rf/reg-fx
 :navigate!
 (fn [args]
   (apply routes/navigate! args)))