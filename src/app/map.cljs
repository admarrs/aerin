(ns app.map
  (:require [reagent.core         :as reagent]
            [reagent.dom          :as dom]
            [re-frame.core        :as rf]))

(set! js/mapboxgl.accessToken "mapbox-access-token")

; .addTo (.setLngLat (js/mapboxgl.Marker.) (-> % :geometry :coordinates)) mapbox
(defn draw-event
  [event]
  (prn "event" event)
  (let [type (-> event :location :type)
        coords (-> event :location :coordinates)]
    (if (= type "Point")
      (.setLngLat (js/mapboxgl.Marker.) (clj->js coords)))))

(defn draw-layers
  [^js/mapboxgl.Map mapbox points]
  (doall (map #(.addTo ^js/mapboxgl.Marker % mapbox) (remove nil? (map #(draw-event %) points)))))

(defn map-component
  [points]
  (let [mapbox-map (atom nil)]
    (reagent/create-class
      {:component-did-mount (fn [this]
                              (if-not @mapbox-map
                                ; (js/L.map (:id spec) (clj->js (merge (dissoc spec :layers :id) {:zoomControl false :attributionControl false})))
                                (let [mapbox (js/mapboxgl.Map. (clj->js {:container "map" 
                                                                        :style "mapbox://styles/mapbox/streets-v11"
                                                                        :center [28.0 51.0]
                                                                        :zoom 5}))]
                                  (reset! mapbox-map mapbox))))
       :component-did-update (fn [this]
                                (let [points (first (rest (reagent/argv this)))]
                                  (prn "component-update" points)
                                  (when @mapbox-map
                                    (draw-layers ^js/mapboxgl.Map @mapbox-map points))))
       :reagent-render (fn []
                        [:div#map])})))

(defn map-wrapper
  [points]
  (fn [points]
    [map-component points]))