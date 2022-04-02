(ns app.util
  (:require [cemerick.url         :as url]
            [clojure.string       :as string]
            [cljs-time.format     :refer [parse unparse formatter]]
            [goog.crypt.base64    :as b64]
            [goog.string          :as gstring]
            [goog.string.format]
            [re-frame.core        :as re-frame]))

(def <== (comp deref re-frame/subscribe))
(def ==> re-frame/dispatch)

(defn decode-jwt-payload [s]
  (prn s)
  (-> s
      (string/split #"\.")
      second
      (b64/decodeString true)
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn jwt-expired?
  "`s` is a jwt-token as string"
  [s]
  (let [now (-> (js/Date.) .getTime (/ 1000))
        exp (-> s decode-jwt-payload :exp)]
    (> now exp)))

(defn round-15
  [angle]
  (let [val (int (* (Math/round (/ angle 15)) 15))]
    (if (= val 360)
      0
      val)))

(defn time-str
  [time]
  (unparse (formatter "HH:mm:ssZ") (parse (formatter "YYYY-MM-DD'T'HH:mm:ssZ") time)))

(defn date-str
  [time]
  (unparse (formatter "DD MMM YYYY") (parse (formatter "YYYY-MM-DD'T'HH:mm:ssZ") time)))

(defn date-time-str
  [time]
  (if time
    (unparse (formatter "DD MMM YYYY - HH:mm:ss") (parse (formatter "YYYY-MM-DD'T'HH:mm:ssZ") time))))

(defn auth-header [db]
  "Get user token and format for API authorization"
  (let [token (get-in db [:user :token])]
    ;(prn "Token" token)
    ;(prn "db" (-> db :user))
    (if token
      [:Authorization (str "Token " token)]
      nil)))

(defn current-path []
  (let [url (-> js/window .-location .-href url/url)]
    (if-let [anchor (:anchor url)]
      (str "/#" anchor)
      (:path url))))

(defn file-list->vec [file-list]
  (vec (mapv #(.item file-list %) (range (.-length file-list)))))

(defn allow-drop [e]
  (.preventDefault e))

(defn on-drop
  ([] (on-drop #(js/console.log %)))
  ([dfn]
   {:on-drag-over #(allow-drop %)
    :on-drop dfn
    :on-drag-enter #(allow-drop %)}))