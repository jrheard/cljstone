(ns cljstone.html
  (:require [goog.dom :as dom]
            [reagent.core :as r]
            [schema.core :as s])
  (:use [cljs.pprint :only [pprint]]
        [cljstone.minion :only [get-attack get-health]]
        [cljstone.board :only [attack play-card]]))

(defn- get-minion-id-from-event [event]
  (-> event
      .-currentTarget
      .-dataset
      .-minionId
      js/parseInt))

(defn draw-card [card index player board-atom]
  [:div.card.minion {:data-card-index index
                     :on-click (fn [e]
                                 (let [card (-> e .-target (dom/getAncestorByClass "card"))]
                                   (swap! board-atom play-card player index)))}
   [:div.name (:name card)]
   [:div.cost (:mana-cost card)]
   [:div.attack (:base-attack (:minion-schematic card))]
   [:div.health (:base-health (:minion-schematic card))]])

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion board-atom]
  ; TODO 0-attack minions can't attack
  ; generalize this by adding a .can-attack class to minions that can attack
  ; frozen minions, summoning sickness minions, etc can't attack - nor can minions that've already attacked their max number of times this turn
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

(defn draw-board-half [board-atom player]
  (let [board-half (player @board-atom)]
    [:div.board-half
     [:div.hand
      [:h3 (:name (:hero board-half))]
      (for [[index card] (map-indexed vector (:hand board-half))]
        ^{:key (:id card)} [draw-card card index player board-atom])]
     [:div.body
       [draw-hero (:hero board-half)]
       [:div.minion-container
        (for [minion (:minions board-half)]
          ^{:key (:id minion)} [draw-minion minion board-atom])]]]))

(defn draw-board [board-atom]
  [:div.board
   [draw-board-half board-atom :player-1]
   [draw-board-half board-atom :player-2]
   [:div.debug
     [:pre (with-out-str (pprint @board-atom))]]])

(defn mount-reagent [board-atom]
  (r/render-component [draw-board board-atom]
                      (js/document.getElementById "content")))

(js/console.log "HTML")
