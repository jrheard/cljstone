(ns cljstone.html
  (:require [reagent.core :as r]
            [schema.core :as s])
  (:use [cljstone.minion :only [get-attack get-health]]
        [cljstone.board :only [attack]]))

(defn- get-minion-id-from-event [event]
  (-> event
      .-currentTarget
      .-dataset
      .-minionId
      js/parseInt))

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion board-atom]
  [:div.minion {:data-minion-id (:id minion)
                :draggable true
                ; todo - any reason to use core.async here, or overengineering?
                :on-drag-start (fn [e]
                                (let [minion-id (get-minion-id-from-event e)]
                                  (.setData (.-dataTransfer e) "text/plain" minion-id)))
                :on-drag-over #(.preventDefault %)
                :on-drop (fn [e]
                          (let [origin-minion-id (js/parseInt (.getData (.-dataTransfer e) "text/plain"))
                                destination-minion-id (get-minion-id-from-event e)]
                            (swap! board-atom attack origin-minion-id destination-minion-id)
                            (.preventDefault e)))}
   [:div.name (:name minion)]
   [:div.attack (get-attack minion)]
   [:div.health (get-health minion)]])

(defn draw-board-half [board-atom which-half]
  (let [board-half (which-half @board-atom)]
    [:div.board-half
     [:div.body
       [draw-hero (:hero board-half)]
       [:div.minion-container
        (for [minion (:minions board-half)]
          ^{:key (:id minion)} [draw-minion minion board-atom])]]]))

(defn draw-board [board-atom]
  [:div.board
   [draw-board-half board-atom :player-1]
   [draw-board-half board-atom :player-2]])

(defn mount-reagent [board-atom]
  (r/render-component [draw-board board-atom]
                      (js/document.getElementById "content")))
