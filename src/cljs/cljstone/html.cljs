(ns cljstone.html
  (:require [reagent.core :as r]
            [schema.core :as s])
  (:use [cljstone.minion :only [Minion get-attack get-health]]
        [cljstone.hero :only [Hero]]
        [cljstone.board :only [BoardHalf perform-attack]]))

(defn get-target-dataset [event]
  (-> event
      .-currentTarget
      .-dataset))

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion board-atom]
  [:div.minion {:data-minion-id (:id minion)
                :draggable true
                :on-drag-start (fn [e]
                                (let [minion-id (.-minionId (get-target-dataset e)) ]
                                  (.setData (.-dataTransfer e) "text/plain" minion-id)))
                :on-drag-over #(.preventDefault %)
                :on-drop (fn [e]
                          (let [origin-minion-id (js/parseInt (.getData (.-dataTransfer e) "text/plain"))
                                destination-minion-id (js/parseInt (.-minionId (get-target-dataset e)))]
                            (js/console.log origin-minion-id)
                            (js/console.log destination-minion-id)
                            (perform-attack @board-atom origin-minion-id destination-minion-id)
                            ; XXXX ok figure out path in board-atom to this minion
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
   [draw-board-half board-atom :half-1]
   [draw-board-half board-atom :half-2]])

(defn mount-reagent [board-atom]
  (r/render-component [draw-board board-atom]
                      (js/document.getElementById "content")))
