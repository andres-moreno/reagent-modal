(ns main
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [goog.string :as gstring]
            [cljs.core.async :refer (chan put! <! go go-loop timeout)]))


(def modal-display (r/atom 0))

(def event-queue (chan))

(go-loop [[event payload] (<! event-queue)]
  (case event
    :inc (swap! modal-display #(+ % payload))
    :dec (swap! modal-display #(- % payload)))
  (recur (<! event-queue)))

(defn simple-component []
  [:h1.p-2 {:class "text-3xl"} "Modal Example"])

(defn main-component []
  [:div 
   [simple-component]
   [:div {:on-click #(let [mod (js/document.getElementById "myModal")]
                      (do (set! (.-style.display mod) "none")
                          (.stopPropagation %)))}
    [:button#myBtn.m-2.p-2.border.border-solid.border-black.rounded-md
     {:on-click #(let [mod (js/document.getElementById "myModal")]
                                 (do (set! (.-style.display mod) "block")
                                     (.stopPropagation %)))} "Open Modal"]
    [:div#myModal {:class "modal"}
     [:div.modal-content.flex.items-center.bg-gray-400
      [:p {:on-click #(.stopPropagation %)} "Some Text in the Modal..."]
      [:span.close.ml-auto
       {:on-click #(let [mod (js/document.getElementById "myModal")]
                     (set! (.-style.display mod) "none")) } 
       (gstring/unescapeEntities "&times;")]]]]])

(defn mount [c]
  (rdom/render [c] (.getElementById js/document "app"))
  )

(defn reload! []
  (mount main-component)
  (print "Hello reload!"))

(defn main! []
  (mount main-component)
  (print "Hello Main"))
