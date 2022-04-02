(ns app.routes
  (:require [clojure.string              :as string]
            [re-frame.core               :as re-frame]
            [reitit.coercion.spec        :as rss]
            [reitit.frontend             :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy        :as rfe]
            [app.views                 :as views]
            [app.util                  :as util]))

(def router (atom nil))

(defn resolve-href
  [to path-params query-params]
  (if (keyword? to)
    (rfe/href to path-params query-params)
    (let [match  (rf/match-by-path @router to)
          route  (-> match :data :name)
          params (or path-params (:path-params match))
          query  (or query-params (:query-params match))]
      (if match
        (rfe/href route params query)
        to))))

(defn Link
  [{:keys [to path-params query-params active]} & children]
  (let [href (resolve-href to path-params query-params)]
    (into
     [:a {:href href} (when active "> ")] ;; Apply styles or whatever
     children)))

(defn- name-matches?
  [name path-params match]
  (and (= name (-> match :data :name))
       (= (not-empty path-params)
          (-> match :parameters :path not-empty))))

(defn- url-matches?
  [url match]
  (= (-> url (string/split #"\?") first)
     (:path match)))

(defn NavLink
  [current-route {:keys [to path-params] :as props} & children]
  (let [active (or (name-matches? to path-params current-route)
                   (url-matches? to current-route))]
    [Link (assoc props :active active) children]))

;(defn navigate-async! [url]
;  (==> [:navigate url]))

(def routes
  (into []
        (concat [["/"
                  {:name        :main
                   :view        views/map-view
                   :controllers [{
                                  :start (fn [& params]
                                           (re-frame/dispatch [:fetch-events]))
                   }]}]]
                [["login"
                  {:name        :login
                   :view        views/login
                   :link-text   "Login"
                   :public      true
                   :controllers [{;; Do whatever initialization needed for home page
                    ;; I.e (re-frame/dispatch [::events/load-something-with-ajax])
                                  :start (fn [& params]
                                           (js/console.log "Entering Login page")
                                           (if @(re-frame/subscribe [:logged-in?])
                                             (rfe/push-state :main)))
                    ;; Teardown can be done here.
                                  :stop  (fn [& params] (js/console.log "Leaving login page"))}]}]]
                )))

(defn match-by-path [path]
  (let [path (string/replace path #"/#" "")]
    (rf/match-by-path routes path)))

(defn navigate!
  ([path]
   (navigate! path nil))
  ([path & args]
   (prn "===> navigate!" path args)
   (cond (and (string? path) (or (string/starts-with? path "http")
                                 (string/starts-with? path "mailto:")))
         ; External link
         (set! (.-location js/window) path)

         ; Internal link
         :else
         (let [match (when (string? path) (-> path match-by-path))
               kw    (cond
                       (keyword? path) path
                       (string? path) (-> match :data :name))
               args (conj args (-> match :parameters :path))]
           (apply rfe/push-state (into [kw] (remove nil?) args))))))

(defn on-navigate [new-match]
  ;(let [old-match (re-frame/subscribe [:current-route])]
  ;  (when new-match
  ;    (let [cs (rfc/apply-controllers (:controllers @old-match) new-match)
  ;          m  (assoc new-match :controllers cs)]
  ;      (re-frame/dispatch [:navigated m]))))
  (prn "new-match" new-match)
  (let [current-path  (util/current-path)
        logged-in?    @(re-frame/subscribe [:logged-in?])]
    (prn "logged-in?" logged-in? (-> new-match :data :name) (or logged-in? (= (-> new-match :data :name) :login)))
    (if (or logged-in? (= (-> new-match :data :name) :login))
      (re-frame/dispatch [:navigated new-match])
      (do (prn "dispatch login") (re-frame/dispatch [:navigate [:login]])))))

(defn init!
  []
  (rfe/start!
   (reset! router (rf/router
                   routes
                   {:data {:coercion rss/coercion}
                    :controllers [{:start (prn "start root-controller")
                                   :stop (prn "stop root-controller")}]}))
   on-navigate
   {:use-fragment true}))