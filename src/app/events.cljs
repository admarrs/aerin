(ns app.events
  (:require [re-frame.core :as rf]
            [ajax.core :refer [json-request-format json-response-format]]
            [reitit.frontend.controllers :as rfc]
            [day8.re-frame.http-fx] ;; ensure :http-xhrio effect handler self-registers with re-frame
            ))

(def default-db
  {:api-url       "http://localhost:8082"
   :logged-in?    false
   :current-route nil})

(rf/reg-event-fx
 :navigate
 (fn [{:keys [db]} [_ route]] ;db [_ route]]
   (prn "Navigate" route)
   ;; See `navigate` effect in routes.cljs
   {:navigate! route
    :db db}))

(rf/reg-event-fx
 :navigated
 (fn [{:keys [db]} [_ {:keys [path] :as new-match}]]
   (if new-match
     (let [old-match (:current-route db)
           ctrls     (rfc/apply-controllers (:controllers old-match) new-match)]
       {:db      (assoc db :current-route (assoc new-match :controllers ctrls))})
     {:db db})))



(rf/reg-event-fx
 :initialise-db

 ;; the interceptor chain (a vector of interceptors)
 [(rf/inject-cofx :app.local-storage/get :login-data)]

 (fn [{:keys [local-storage]} _]
   (if-let [login-data (:login-data local-storage)]
     {:db (-> default-db
              (assoc-in [:user] login-data)
              (assoc :logged-in? true))}
     {:db default-db})))

(rf/reg-event-fx
  :fetch-events
  (fn [{:keys [db]} [_ _]]
    (prn "Fetch-events" (-> db :user :token))
    (let [token (-> db :user :token)]
      {:http-xhrio {:method           :get
                    :uri              (str (:api-url db) "/events")
                    :headers          {:Authorization (str "Bearer " token)}
                    :response-format  (json-response-format {:keywords? true})
                    :on-success       [:fetch-events-success]
                    :on-failure       [:api-request-error :fetch-points]}
       :db (-> db
               (assoc-in [:loading :points] true))})))

(rf/reg-event-fx
  :fetch-events-success
  (fn [{:keys [db]} [_ result]]
    (prn result)
    {:db (assoc-in db [:events] result)}))

(rf/reg-event-fx
 :api-request-error  ;; triggered when we get request-error from the server
 (fn [{:keys [db]} [_ request-type response]]
  {:db (assoc-in db [:errors request-type] response)}))

(rf/reg-event-db
 :complete-request         ;; when we complete a request we need to clean up
 (fn [db [_ request-type]] ;; few things so that our ui is nice and tidy
   (assoc-in db [:loading request-type] false)))