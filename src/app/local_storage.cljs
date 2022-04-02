(ns app.local-storage
  (:require [cljs.reader   :as reader]
            [re-frame.core :as rf]
            [app.util    :as util]))

;; -- Local Storage  ----------------------------------------------------------
;;
;; Part of the conduit challenge is to store a user in localStorage, and
;; on app startup, reload the user from when the program was last run.
;;
;(def tempo-user-key "tempo-user")  ;; localstore key
;
;(defn set-user-ls
;  "Puts user into localStorage"
;  [user]
;  (prn "user-ls" user)
;  (.setItem js/localStorage tempo-user-key (str user)))  ;; sorted-map written as an EDN map

;; Removes user information from localStorge when a user logs out.
;;
;(defn remove-user-ls
;  "Removes user from localStorage"
;  []
;  (.removeItem js/localStorage tempo-user-key))

;; -- cofx Registrations  -----------------------------------------------------
;;
;; Use `reg-cofx` to register a "coeffect handler" which will inject the user
;; stored in localStorge.
;;
;; To see it used, look in `events.cljs` at the event handler for `:initialise-db`.
;; That event handler has the interceptor `(inject-cofx :local-store-user)`
;; The function registered below will be used to fulfill that request.
;;
;; We must supply a `sorted-map` but in localStorage it is stored as a `map`.
;;
;(rf/reg-cofx
; :local-store-user
; (fn [cofx _]
;   (assoc cofx :local-store-user  ;; put the local-store user into the coeffect under :local-store-user
;          (let [user (into (sorted-map)      ;; read in user from localstore, and process into a sorted map
;                           (some->> (.getItem js/localStorage tempo-user-key)
;                                    (cljs.reader/read-string)))] ;; EDN map -> map
;            (if (empty? user)
;              nil
;              user)))))

(defn ls-set! [k v]
  (prn (pr-str k) (pr-str v))
  (js/console.log js/localStorage)
  (.setItem js/localStorage (pr-str k) (pr-str v)))

(defn ls-get [k]
  (when-let [s (.getItem js/localStorage (pr-str k))]
    (let [token   (reader/read-string s)
          payload (util/decode-jwt-payload token)]
      (prn "local-storage/get" token payload)
      {:login payload :token token})))

(defn ls-remove! [k]
  (.removeItem js/localStorage k))

(rf/reg-cofx
 ::get
 (fn [cofx k]
   (assoc-in cofx [:local-storage k] (ls-get k))))

(rf/reg-fx
 ::remove!
 (fn [k]
   (ls-remove! k)))

(rf/reg-fx
 ::set!
 (fn [[k v]]
   (prn "set" k v)
   (ls-set! k v)))