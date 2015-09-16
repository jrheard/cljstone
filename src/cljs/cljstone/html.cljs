(ns cljstone.html
  (:require [goog.dom :as dom]
            [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:use [cljs.core.async :only [chan <! >! put!]]
        [cljs.core.async.impl.protocols :only [Channel]]
        [cljs.pprint :only [pprint]]
        [cljstone.minion :only [get-attack get-health can-attack]]
        [cljstone.board :only [Board end-turn play-card path-to-character run-continuation]]
        [cljstone.combat :only [attack]]))

; TODO test this file

(s/defschema GameState
  {:board-atom ratom/RAtom
   :game-event-chan Channel
   :mouse-event-chan Channel})

(defn- get-character-id-from-event [event]
  (-> event
      .-currentTarget
      .-dataset
      .-characterId
      js/parseInt))

(defn draw-minion-card [card]
  [:div.content
   [:div.name (:name card)]
   [:div.attack (:attack card)]
   [:div.health (:health card)]])

(defn draw-spell-card [card]
  [:div.content
   [:div.name (:name card)]])

(defn draw-card [card index player is-owners-turn game-event-chan]
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
                         (put! game-event-chan {:type :play-card
                                                :player player
                                                :index index}))
                       nil)}
     (condp = (:type card)
       :minion [draw-minion-card card]
       :spell [draw-spell-card card])]))

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion board is-owners-turn mouse-event-chan]
  (let [minion-can-attack (and is-owners-turn
                               (can-attack minion)
                               (= (get-in board [:mode :type]) :default))
        classes (str
                  "minion "
                  (when minion-can-attack "can-attack")
                  (when (and (= (get-in board [:mode :type]) :targeting)
                             (contains? (get-in board [:mode :targets])
                                        (:id minion)))
                    " targetable"))
        put-event-in-chan (fn [e]
                            (put! mouse-event-chan {:type :mouse-event
                                                    :mouse-event-type (keyword (.-type e))
                                                    :board board
                                                    :character-id (get-character-id-from-event e)})
                            nil)]
    [:div {:class classes
           :data-character-id (:id minion)
           :draggable minion-can-attack
           :on-click put-event-in-chan
           :on-drag-start put-event-in-chan
           :on-drag-over #(.preventDefault %)
           :on-drop (fn [e]
                      (put-event-in-chan e)
                      (.preventDefault e))}
     [:div.name (:name minion)]
     [:div.attack (get-attack minion)]
     [:div.health (get-health minion)]]))

(defn draw-board-half [board player game-state]
  (let [board-half (board player)
        is-owners-turn (= (board :whose-turn) player)]
    [:div.board-half
     [:div.hand
      [:h3 (:name (:hero board-half))]
      (for [[index card] (map-indexed vector (:hand board-half))]
        ^{:key (:id card)} [draw-card card index player is-owners-turn (game-state :game-event-chan)])]
     [:div.body
       [draw-hero (:hero board-half)]
       [:div.minion-container
        (for [minion (:minions board-half)]
          ^{:key (:id minion)} [draw-minion minion board is-owners-turn (game-state :mouse-event-chan)])]]]))

(defn draw-end-turn-button [game-state]
  [:div.end-turn {:on-click #(put! (game-state :game-event-chan) {:type :end-turn})}
   "End Turn"])

(defn draw-combat-log-entry [board entry]
  [:div.log-entry
   (str
     (-> entry :target :name)
     " was attacked for "
     (- (-> entry :modifier :effect :health))
     " damage")])

(s/defn draw-combat-log [board]
  (let [combat-log (:combat-log board)]
    [:div.combat-log-viewport
     [:div.combat-log
     (for [entry combat-log]
       ^{:key (:id entry)} [draw-combat-log-entry board entry])]]))

(defn draw-board-mode [board])

(defn draw-board [game-state]
  (let [board @(game-state :board-atom)
        classes (str
                  "board "
                  (name (get-in board [:mode :type])))]
    [:div {:class classes}
     [draw-board-mode board]
     [draw-board-half board :player-1 game-state]
     [draw-board-half board :player-2 game-state]
     [draw-end-turn-button game-state]
     [draw-combat-log board]
     [:div.turn (pr-str (:whose-turn board)) (pr-str (:turn board))]]))

; TODO - eventually implement click->click attacking
(defn handle-mouse-events [{:keys [mouse-event-chan game-event-chan]}]
  (go-loop [origin-character-id nil]
    (let [msg (<! mouse-event-chan)]
      (condp = (msg :mouse-event-type)
        :click (do
                 (>! game-event-chan {:type :character-selected
                                      :character-id (:character-id msg)})
                 (recur nil))
        :dragstart (recur (:character-id msg))
        :drop (do
                (when (not= (first (path-to-character (:board msg) origin-character-id))
                            (first (path-to-character (:board msg) (:character-id msg))))
                  (>! game-event-chan {:type :attack
                                       :origin-id origin-character-id
                                       :destination-id (:character-id msg)}))
                (recur nil))))))

(defn handle-game-events [{:keys [game-event-chan board-atom]}]
  (go-loop []
    (let [msg (<! game-event-chan)]
      (if (= (get-in @board-atom [:mode :type]) :default)
        (condp = (:type msg)
          :attack (swap! board-atom attack (msg :origin-id) (msg :destination-id))
          :play-card (swap! board-atom play-card (msg :player) (msg :index))
          :end-turn (swap! board-atom end-turn))
        ; XXX right now the only mode we support is targeting
        (condp = (:type msg)
          :character-selected (swap! board-atom run-continuation (msg :character-id))))
    (recur))))

(defn draw-board-atom [board-atom]
  (let [game-state {:board-atom board-atom
                    :game-event-chan (chan)
                    :mouse-event-chan (chan)}]

    (r/render-component [draw-board game-state]
                        (js/document.getElementById "content"))

    (handle-mouse-events game-state)
    (handle-game-events game-state)))
