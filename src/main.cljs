(ns main
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [cljs.core.async :refer (chan put! <! go go-loop timeout)]))

(def modal-display (r/atom false))

(def event-queue (chan))

(go-loop [[event payload] (<! event-queue)]
  (case event
    :show-modal (reset! modal-display true)
    :hide-modal (reset! modal-display false))
  (recur (<! event-queue)))

(defn title-component []
  [:h1.p-2.text-3xl "Modal Example"])

(defn show-modal-button []
  [:button.m-2.p-2.border.border-solid.border-black.rounded-md.bg-gray-200
   {:on-click #(do (.activeElement.blur js/document)
                   (put! event-queue [:show-modal]))}
   "Open Modal"])

(defn modal-dialog
  "A modal dialog with text and a button to close it"
  []
  [:div ;; container to hold contents
   {:class "mx-auto p-4 modal-content flex items-center w-5/6 mt-32
            bg-gray-500 border border-solid border-black"
    :on-click #(.stopPropagation %)} ; disable closing *inside* modal
   [:p
    {:class "w-11/12"} "Some Text in the Modal..."]
   ;; button to close modal
   [:button
    {:class "px-3 py-2 ml-auto border border-solid border-black
             rounded-md bg-gray-400 hover:bg-gray-300 focus:bg-gray-300"
     :on-click #(put! event-queue [:hide-modal])} 
    "Close"]])

(defn modal-container [dialog display-flag]
  ;; modal background container
  (when @display-flag
    [:div
     {:class "fixed z-10 top-0 left-0 w-full h-full overflow-auto" 
      :style {:background-color "rgba(0,0,0,0.4)"}
      ;; clicking outside of modal closes the modal
      :on-click #(put! event-queue [:hide-modal])}
     [dialog]]))

(defn main-component []
  [:div 
   [title-component]
   [show-modal-button]
   [modal-container modal-dialog modal-display]])

(defn mount [c]
  (rdom/render [c] (.getElementById js/document "app")))

(defn reload! []
  (mount main-component)
  (print "Hello reload!"))

(defn main! []
  (mount main-component)
  (print "Hello Main"))
