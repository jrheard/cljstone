(ns cljstone.html
  (:require [goog.dom :as dom]
            [reagent.core :as r]
            [schema.core :as s])
  (:use [cljs.pprint :only [pprint]]
        [cljstone.minion :only [get-attack get-health can-attack]]
        [cljstone.board :only [attack end-turn play-card]]))

(defn- get-minion-id-from-event [event]
  (-> event
      .-currentTarget
      .-dataset
      .-minionId
      js/parseInt))

(defn draw-card [card index player board-atom whose-turn]
  [:div.card.minion {:data-card-index index
                     :on-click (fn [e]
                                 (let [card (-> e .-target (dom/getAncestorByClass "card"))]
                                   (swap! board-atom play-card player index)))}
   [:div.name (:name card)]
   [:div.cost (:mana-cost card)]
   [:div.attack (:attack (:minion-schematic card))]
   [:div.health (:health (:minion-schematic card))]])

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion board-atom is-owners-turn]
  ; TODO 0-attack minions can't attack
  ; generalize this by adding a .can-attack class to minions that can attack
  ; frozen minions, summoning sickness minions, etc can't attack - nor can minions that've already attacked their max number of times this turn
  (let [minion-can-attack (and is-owners-turn (can-attack minion))
        classes (str
                  "minion "
                  (when minion-can-attack "can-attack"))]
    [:div {:class classes
           :data-minion-id (:id minion)
           :draggable minion-can-attack
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
     [:div.health (get-health minion)]]))

(defn draw-board-half [board board-atom player whose-turn]
  (let [board-half (board player)
        is-owners-turn (= whose-turn player)]
    [:div.board-half
     [:div.hand
      [:h3 (:name (:hero board-half))]
      (for [[index card] (map-indexed vector (:hand board-half))]
        ^{:key (:id card)} [draw-card card index player board-atom is-owners-turn])]
     [:div.body
       [draw-hero (:hero board-half)]
       [:div.minion-container
        (for [minion (:minions board-half)]
          ^{:key (:id minion)} [draw-minion minion board-atom is-owners-turn])]]]))

(defn draw-end-turn-button [board board-atom]
  [:div.end-turn {:on-click (fn [e]
                              (swap! board-atom end-turn))}
   "End Turn"])

(defn draw-board [board-atom]
  (let [board @board-atom]
    [:div.board
     [draw-board-half board board-atom :player-1 (board :whose-turn)]
     [draw-board-half board board-atom :player-2 (board :whose-turn)]
     [draw-end-turn-button board board-atom]
     [:div.turn (pr-str (:whose-turn board)) (pr-str (:turn board))]]))

(defn mount-reagent [board-atom]
  (r/render-component [draw-board board-atom]
                      (js/document.getElementById "content")))
