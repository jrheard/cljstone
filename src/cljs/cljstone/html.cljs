(ns cljstone.html
  (:require [goog.dom :as dom]
            [reagent.core :as r]
            [schema.core :as s])
  (:use [cljs.pprint :only [pprint]]
        [cljstone.minion :only [get-attack get-health can-attack]]
        [cljstone.board :only [end-turn play-card path-to-character]]
        [cljstone.combat :only [attack]]))

(defn- get-minion-id-from-event [event]
  (-> event
      .-currentTarget
      .-dataset
      .-minionId
      js/parseInt))

(defn draw-minion-card [card]
  [:div.content
   [:div.name (:name card)]
   [:div.attack (:attack (:minion-schematic card))]
   [:div.health (:health (:minion-schematic card))]])

(defn draw-spell-card [card]
  [:div.content
   [:div.name (:name card)]])

(defn draw-card [card index player board-atom is-owners-turn]
  (let [playable is-owners-turn ; will become more complex later
        classes (str
                  "card "
                  (clj->js (:class card))
                  (condp = (:type card) :minion " minion " :spell " spell ")
                  (when playable "playable"))]
    [:div {:class classes
           :data-card-index index
           :on-click (fn [e]
                       (when playable
                         (swap! board-atom play-card player index)))}
     (condp = (:type card)
       :minion [draw-minion-card card]
       :spell [draw-spell-card card])]))

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion board board-atom is-owners-turn]
  (let [minion-can-attack (and is-owners-turn (can-attack minion))
        classes (str
                  "minion "
                  (when minion-can-attack "can-attack"))]
    [:div {:class classes
           :data-minion-id (:id minion)
           :draggable minion-can-attack
           :on-drag-start (fn [e]
                           (let [minion-id (get-minion-id-from-event e)]
                             (.setData (.-dataTransfer e) "text/plain" minion-id)))
           :on-drag-over (fn [e]
                           (let [origin-minion-id (js/parseInt (.getData (.-dataTransfer e) "text/plain"))
                                 destination-minion-id (get-minion-id-from-event e)]
                             (when (not= (first (path-to-character board origin-minion-id))
                                         (first (path-to-character board destination-minion-id)))
                               (.preventDefault e))))
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
          ^{:key (:id minion)} [draw-minion minion board board-atom is-owners-turn])]]]))

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
     [:div.turn (pr-str (:whose-turn board)) (pr-str (:turn board))]
     #_[:div.debug
      [:pre (with-out-str (pprint board))]]]))

(defn mount-reagent [board-atom]
  (r/render-component [draw-board board-atom]
                      (js/document.getElementById "content")))
