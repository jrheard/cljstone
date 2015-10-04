(ns cljstone.html
  (:require [cljs.core.match :refer-macros [match]]
            [goog.dom :as dom]
            [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:use [cljs.core.async :only [chan <! >! put!]]
        [cljs.core.async.impl.protocols :only [Channel]]
        [cljs.pprint :only [pprint]]
        [cljstone.board :only [Board BoardHalf end-turn play-card path-to-character run-continuation get-mana]]
        [cljstone.board-mode :only [DefaultMode]]
        [cljstone.character :only [Character Player get-attack get-health can-attack other-player get-base-health get-base-attack]]
        [cljstone.combat :only [attack enter-targeting-mode-for-attack]]
        [plumbing.core :only [safe-get safe-get-in]]))

(s/defschema GameState
  {:board-atom ratom/RAtom
   :game-event-chan Channel
   :mouse-event-chan Channel})

(defn get-character-id-from-event [event]
  (-> event
      .-currentTarget
      .-dataset
      .-characterId
      js/parseInt))

(defn put-character-mouse-event-in-chan
  [board mouse-event-chan event]
  (put! mouse-event-chan {:type :mouse-event
                          :mouse-event-type (keyword (.-type event))
                          :board board
                          :character-id (get-character-id-from-event event)})
  nil)

(s/defn draw-character-health [character :- Character]
  (let [health (get-health character)
        base-health (get-base-health character)
        health-class (cond
                       (and (= health base-health)
                            (> base-health (character :base-health))) "buffed"
                       (< health base-health) "damaged"
                       :else "")]
    [:div {:class (str "health " health-class)} health]))

(defn draw-minion-card [card]
  [:div.content
   [:div.mana-cost
    [:div.mana-content (:mana-cost card)]]
   [:div.name (:name card)]
   [:div.attack (:attack card)]
   [:div.health (:health card)]])

(defn draw-spell-card [card]
  [:div.content
   [:div.mana-cost
    [:div.mana-content (:mana-cost card)]]
   [:div.name (:name card)]])

(defn draw-card [card index player board-half is-owners-turn game-event-chan]
  (let [playable (and is-owners-turn
                      (>= (:actual (get-mana board-half))
                          (:mana-cost card)))
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

(defn draw-hero [hero board mouse-event-chan]
  ; TODO have a (character-props character) function that spits out the k/v pairs used by both heroes and minions
  (let [hero-is-alive (not (and (= (safe-get-in board [:mode :type])
                                   :game-over)
                                (= (other-player (safe-get-in board [:mode :winner]))
                                   (first (path-to-character board (:id hero))))))]
    (if hero-is-alive
      [:div.hero {:data-character-id (:id hero)
                  :on-drag-over #(.preventDefault %)
                  :on-drop (fn [e]
                             (put-character-mouse-event-in-chan board mouse-event-chan e)
                             (.preventDefault e)) }
       [:div.name (:name hero)]
       (when (> (get-attack hero) 0)
         [:div.attack (get-attack hero)])
       [draw-character-health hero]]

      [:div.hero
         [:div.loser "X"]])))

(defn draw-minion [minion board is-owners-turn mouse-event-chan]
  (let [minion-can-attack (and is-owners-turn
                               (can-attack minion)
                               (= (safe-get-in board [:mode :type]) :default))
        classes (str
                  "minion "
                  (when minion-can-attack "can-attack")
                  (when (and (= (safe-get-in board [:mode :type]) :targeting)
                             (contains? (safe-get-in board [:mode :targets])
                                        (:id minion)))
                    " targetable"))
        put-event-in-chan (partial put-character-mouse-event-in-chan board mouse-event-chan)]
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
     (if (> (get-attack minion)
            (minion :base-attack))
       [:div.attack.buffed (get-attack minion)]
       [:div.attack (get-attack minion)])
     [draw-character-health minion]]))

(s/defn draw-mana-tray
  [board-half :- BoardHalf
   player :- Player]
  [:div.mana-tray
   (for [i (range (:max (get-mana board-half)))]
     ^{:key [player i]} [:div {:class (str
                                        "mana-crystal-container "
                                        (when (>= i (:actual (get-mana board-half))) "spent"))}
                         [:div.mana-crystal]])])

; TODO it's currently super unclear whose turn it is, especially in the early game when you can't play anything
; make this more clear visually
(defn draw-board-half [board player game-state]
  (let [board-half (board player)
        is-owners-turn (= (board :whose-turn) player)]
    [:div.board-half
     [:div.hand
      [:h3 (:name (:hero board-half))]
      (for [[index card] (map-indexed vector (:hand board-half))]
        ^{:key (:id card)} [draw-card card index player board-half is-owners-turn (game-state :game-event-chan)])]
     [:div.body
      [draw-hero (:hero board-half) board (game-state :mouse-event-chan)]
      [draw-mana-tray board-half player]
      [:div.minion-container
        (for [minion (:minions board-half)]
          ^{:key (:id minion)} [draw-minion minion board is-owners-turn (game-state :mouse-event-chan)])]]]))

(defn draw-end-turn-button [game-state]
  [:div.end-turn {:on-click #(do
                               (put! (game-state :game-event-chan) {:type :end-turn})
                               nil)}
   "End Turn"])

(defn draw-combat-log-entry [board entry]
  [:div.log-entry
   (str
     (-> entry :target :name)
     " was attacked for "
     (- (-> entry :modifier :effect :health))
     " damage")])

(defn draw-combat-log [board]
  (let [combat-log (:combat-log board)]
    [:div.combat-log-viewport
     [:div.combat-log
     (for [entry (reverse combat-log)]
       ^{:key (:id entry)} [draw-combat-log-entry board entry])]]))

(defn draw-cancel-button [board game-state button-text]
  [:div.cancel-mode {:on-click #(do
                                  (put! (game-state :game-event-chan) {:type :cancel-mode})
                                  nil)}
   button-text])

(defn draw-game-over [winner]
  [:div.game-over
   (str "HOLY SHIT " winner " WON!!!!!!")])

(defn draw-board-mode [board game-state]
  (condp = (:type (board :mode))
    :default nil
    :targeting (draw-cancel-button board game-state "Cancel Targeting")
    :positioning (draw-cancel-button board game-state "Cancel Positioning")
    :game-over (draw-game-over (:winner (board :mode)))))

(defn draw-board [game-state]
  (let [board @(game-state :board-atom)
        classes (str
                  "board "
                  (name (safe-get-in board [:mode :type])))]
    [:div {:class classes}
     [draw-board-half board :player-1 game-state]
     [draw-board-half board :player-2 game-state]
     [draw-end-turn-button game-state]
     [draw-combat-log board]
     [draw-board-mode board game-state]]))

(defn handle-mouse-events [{:keys [mouse-event-chan game-event-chan]}]
  (go-loop [origin-character-id nil]
    (let [msg (<! mouse-event-chan)
          emit-selected-event (fn [mouse-event]
                                (put! game-event-chan {:type :character-selected
                                      :character-id (:character-id msg)}))]
      (condp = (msg :mouse-event-type)
        :click (do
                 (emit-selected-event msg)
                 (recur nil))
        :dragstart (do
                     (emit-selected-event msg)
                     (recur (:character-id msg)))
        :drop (do (if (= (first (path-to-character (:board msg) origin-character-id))
                         (first (path-to-character (:board msg) (:character-id msg))))
                    (>! game-event-chan {:type :cancel-mode})
                    (emit-selected-event msg))
                (recur nil))))))

(defn handle-game-events [{:keys [game-event-chan board-atom]}]
  (go-loop []
    (let [msg (<! game-event-chan)
          board-mode (safe-get-in @board-atom [:mode :type])]
      (when (not= board-mode :game-over)
        (match [board-mode (:type msg)]
          [:default :play-card] (swap! board-atom play-card (msg :player) (msg :index))
          [:default :end-turn] (swap! board-atom end-turn)
          [:default :character-selected] (swap! board-atom enter-targeting-mode-for-attack (msg :character-id))
          ; TODO at one when playing shattered sun, i got an error: "no clause matching :targeting :play-card". haven't been able to repro.
          [_ :character-selected] (when (not= board-mode :default)
                                    (swap! board-atom run-continuation (msg :character-id)))
          [(_ :guard #(not= :default %)) :cancel-mode] (swap! board-atom assoc :mode DefaultMode))
        (recur)))))

(defn draw-board-atom [board-atom]
  (let [game-state {:board-atom board-atom
                    :game-event-chan (chan)
                    :mouse-event-chan (chan)}]

    (r/render-component [draw-board game-state]
                        (js/document.getElementById "content"))

    (handle-mouse-events game-state)
    (handle-game-events game-state)))
