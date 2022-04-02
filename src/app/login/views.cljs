(ns app.login.views
  (:require [reagent.core  :as r]
            [re-frame.core :as rf]
            [app.login.events]
            [app.login.subs]))

(defn form
  []
  (let [form (r/atom {})
        validation (r/atom {})]
    (fn []
      [:div {:class "flex items-center justify-center bg-white py-12 px-4 sm:px-6 lg:px-8"}
       [:div {:class "max-w-md w-full space-y-8"}
        [:div
         [:h2 {:class "mt-6 text-center text-3xl font-extrabold text-gray-900"} "Sign in"]]
        [:form {:class "mt-8"
                :on-submit (fn [e]
                             (.preventDefault e)
                             (if-not (:email @form) (swap! validation assoc :email true))
                             (if-not (:password @form) (swap! validation assoc :password true))
                             (if (empty? @validation)
                               (rf/dispatch [:login @form])))}
         [:input {:id "email-address"
                  :name "email"
                  :type "email"
                  :auto-complete "email"
                  :class "appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 
                          placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 
                          focus:border-indigo-500 focus:z-10 sm:text-sm"
                  :placeholder "Email address"
                  :on-focus #(swap! validation dissoc :email)
                  :on-change #(swap! form assoc :email (-> % .-target .-value))}]
         [:input {:id "password"
                  :name "password"
                  :type "password"
                  :auto-complete "current-password"
                  :class "appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 
                          placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 
                          focus:border-indigo-500 focus:z-10 sm:text-sm"
                  :placeholder "Password"
                  :on-focus #(swap! validation dissoc :password)
                  :on-change #(swap! form assoc :password (-> % .-target .-value))}]
         [:div {:class "mt-1 h-6 flex flex-col"}
          [:label {:class "text-xs text-red-500"} (if (:email @validation) "Email is required ")]
          [:label {:class "text-xs text-red-500"} (if (:password @validation) "Password is required ")]]
         [:div {:class "mt-4"}
          [:button {:type "submit"
                    :class "group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm
                           font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none 
                           focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"}
           [:span {:class "absolute left-0 inset-y-0 flex items-center pl-3"}]
           "Sign in"]]]]])))