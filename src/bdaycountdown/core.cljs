(ns bdaycountdown.core
  (:require [cljs-time.core :as t]
            [cljs-time.format :as f]
            [reagent.core :as r]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce state (r/atom {:now   (t/now)
                        :bdays [{:name "Alex"
                                 :bday (t/date-time 2008 12 25)}
                                {:name "Sebastian"
                                 :bday (t/date-time 2011 4 8)}]}))

(js/setInterval #(swap! state assoc :now (t/now)) 200)

(defn birthday-this-year
  [bday]
  (t/date-time (t/year (:now @state))
               (t/month bday)
               (t/day bday)
               (t/hour bday)
               (t/minute bday)
               (t/second bday)))

(defn birthday-next-year
  [bday]
  (t/date-time (inc (t/year (:now @state)))
               (t/month bday)
               (t/day bday)
               (t/hour bday)
               (t/minute bday)
               (t/second bday)))

(defn interval-to-next-birthday
  "Work out a cljs-time interval to the next birthday. Alters the year 
on the birthday provided to be in the future if necessary then returns the
interval to it."
  [bday]
  (let [next-bday (if (> (:now @state) (birthday-this-year bday))
                    (birthday-next-year bday)
                    (birthday-this-year bday))]
    (t/interval (:now @state) next-bday)))

(defn age-of [boy]
  (let [bday (:bday boy)
        ival (t/interval bday (:now @state))
        years (t/in-years ival)
        total-months (t/in-months ival)
        months (mod total-months 12)]
    (str years "y " months "m")))

(defn countdown-table-row
  [boy]
  (let [ival (interval-to-next-birthday (:bday boy))
        fmt (f/formatter "d MMM yyyy")]
    ^{:key (.random js/Math)}
    [:tr
     [:th (:name boy)]
     [:td (f/unparse fmt (:bday boy))]
     [:td (age-of boy)]
     [:td (t/in-weeks ival)]
     [:td (t/in-days ival)]
     [:td (t/in-hours ival)]
     [:td (t/in-minutes ival)]
     [:td (t/in-seconds ival)]]))

(defn countdown-table []
  [:table.table.table-condensed
   [:thead
    [:tr
     [:th {:col-span 3}]
     [:th {:col-span 5} "Time to wait in..."]]
    [:tr
     [:th ""] [:th "Birthday"] [:th "Age"]
     [:th "Weeks"] [:th "Days"] [:th "Hours"] [:th "Minutes"] [:th "Seconds"]]]
   [:tbody
    (doall
     (map countdown-table-row (:bdays @state)))]])

(r/render [:div
           [:h1 "Birthday Countdown"]
           [countdown-table]]
          (.getElementById js/document "app"))

;; ---------------------------------------------------------------------

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
