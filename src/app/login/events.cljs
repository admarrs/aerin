(ns app.login.events
  (:require ;[ajax.core            :refer [json-request-format json-response-format]]
   [cljs.reader]
   [re-frame.core        :as rf]
   [superstructor.re-frame.fetch-fx]
   [app.local-storage  :as local-storage]
   [app.util           :as util]))

;; -- POST Login @ /api/users/login -------------------------------------------
;;
(rf/reg-event-fx
 :login
 (fn [{:keys [db]} [_ credentials]]
   (prn "credentials" credentials db)
   {:fetch {:method :post
            :url                    (str (:api-url db) "/rpc/login")
            :mode                   :cors
            :body                   credentials
                 ;:format          (json-request-format)
                 ;:response-format (json-response-format {:keywords? true})
            :request-content-type   :json
            :response-content-types {#"application/.*json" :json}
            :on-success             [:login-success :login]
            :on-failure             [:api-request-error :login]}}))

(rf/reg-event-fx
 :login-success
 (fn [{:keys [db]} [_ login-type body]]
   (prn "Login success" login-type body)
   (let [refresh-interval-s 1800 ; 30 minutes
         token              (get-in body [:body :token])
         user               (util/decode-jwt-payload token)
         return-map         {:db       (-> db
                                           (assoc-in [:logged-in?] true)
                                           (assoc-in [:user :login] user)
                                           (assoc-in [:user :token] token))

                             ::local-storage/set! [:login-data (get-in body [:body :token])]

                             :dispatch-later
                             [{:ms       (* 1000 refresh-interval-s)
                               :dispatch [:refresh-login]}]}]
     (prn "user" user)

     (if (= login-type :login)
       (merge return-map {:dispatch-n [[:complete-request :login]
                                       [:navigate [:main]]]})
       return-map))))

(rf/reg-event-fx
 :logout
 ;; This interceptor, defined above, makes sure
 ;; that we clean up localStorage after logging-out
 ;; the user.
 ;remove-user-interceptor
 (fn [{:keys [db]} _]
   (prn "**** Logout ****")
   {:db (-> db
            (assoc-in [:user] nil)
            (assoc-in [:logged-in?] false))

    ::local-storage/remove! :login-data

    :dispatch [:navigate [:login]]}))

(rf/reg-event-fx
 :login-refresh-failure
 (fn [_ [_ {:keys [status] :as resp}]]
   (if (#{401 403} status)
     {:dispatch [::logout]}
     {})))

(rf/reg-event-fx
 :refresh-login
 [(rf/inject-cofx ::local-storage/get :login-data)]
 (fn [{local-storage :local-storage db :db} _]
   (prn "Refresh login")
   (let [login-data (:login-data local-storage)]
     (if (or (empty? login-data) (not (:logged-in? db)))
       {}
       (let [token (-> login-data :token)]
         (if (util/jwt-expired? token)
           {:dispatch [:logout]}
           {:fetch
            {:method                 :get
             :url                    (str (:api-url db) "/rpc/refresh")
             :headers                {"Authorization" (str "Token " token)}
             ;:format          (json-request-format)
             ;:response-format (json-response-format {:keywords? true})
             :response-content-types {#"application/.*json" :json}
             :on-success             [:login-success :refresh]
             :on-failure             [:login-refresh-failure]}}))))))