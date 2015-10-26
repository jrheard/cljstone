(ns cljstone.html
  (:require [cljs.core.match :refer-macros [match]]
            [goog.dom :as dom]
            [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:use [cljs.core.async :only [chan <! >! put!]]
        [cljs.pprint :only [pprint]]
        [cljstone.board :only [Board BoardHalf end-turn play-card path-to-character run-continuation get-mana get-character-by-id toggle-mulligan-card-selected]]
        [cljstone.board-mode :only [DefaultMode MulliganCard]]
        [cljstone.card :only [Card card-is-playable?]]
        [cljstone.character :only [Character Player get-attack get-health can-attack? other-player get-base-health has-summoning-sickness? has-taunt? has-divine-shield?]]
        [cljstone.combat :only [attack enter-targeting-mode-for-attack]]
        [cljstone.hero :only [Hero]]
        [cljstone.utils :only [in?]]
        [plumbing.core :only [safe-get safe-get-in]]))

(s/defschema GameState
  {:board-atom ratom/RAtom
   :game-event-chan s/Any})

(s/defn get-character-id-from-event :- s/Int
  [event]
  (-> event
      .-currentTarget
      .-dataset
      .-characterId
      js/parseInt))

(defn fire-character-selected-event
  [game-event-chan mouse-event]
  (put! game-event-chan {:type :character-selected
                         :character-id (get-character-id-from-event mouse-event)})
  nil)

(s/defn is-targetable? :- s/Bool
  [board :- Board
   character :- Character]
  (and (= (safe-get-in board [:mode :type]) :targeting)
       (in? (safe-get-in board [:mode :targets])
            character)))

(s/defn character-properties
  [board :- Board
   character :- Character
   game-event-chan]
  (let [board-mode-type (safe-get-in board [:mode :type])
        is-owners-turn (= (board :whose-turn)
                          (first (path-to-character board (:id character))))
        can-attack (and is-owners-turn
                        (can-attack? character)
                        (= board-mode-type :default))
        can-be-selected (or can-attack
                            (is-targetable? board character))
        is-attacking (and (= board-mode-type :targeting)
                          (= character (get-in board [:mode :attacker])))
        classes (str
                  (name (character :type))
                  (when (is-targetable? board character) " targetable ")
                  (when is-attacking " attacker ")
                  (when can-attack " can-attack ")
                  (when (has-divine-shield? character) " divine-shield "))
        fire-selected-event (partial fire-character-selected-event game-event-chan)]
    {:class classes
     :data-character-id (character :id)
     :draggable can-attack
     :on-click #(when can-be-selected (fire-selected-event %))
     :on-drag-start #(when can-be-selected (fire-selected-event %))
     :on-drag-over #(.preventDefault %)
     :on-drop (fn [e]
                (when can-be-selected
                  (fire-selected-event e))
                (.preventDefault e))}))

(s/defn draw-character-health [character :- Character]
  (let [health (get-health character)
        base-health (get-base-health character)
        health-class (cond
                       (and (= health base-health)
                            (> base-health (character :base-health))) "buffed"
                       (< health base-health) "damaged"
                       :else "")]
    [:div {:class (str "health " health-class)} health]))

(s/defn draw-minion-card [card :- Card]
  [:div.content
   [:div.mana-cost
    [:div.mana-content (:mana-cost card)]]
   [:div.name (:name card)]
   ; TODO - draw card text like "battlecry. charge. taunt." when relevant
   [:div.attack (:base-attack card)]
   [:div.health (:base-health card)]])

(s/defn draw-spell-card [card :- Card]
  [:div.content
   [:div.mana-cost
    [:div.mana-content (:mana-cost card)]]
   [:div.name (:name card)]])

(defn card-props [card index selectable game-event-chan]
  (let [classes (str
                  "card "
                  (clj->js (:class card))
                  (condp = (:type card) :minion " minion " :spell " spell ")
                  ; XXX rename css class to selectable
                  (when selectable "playable"))]
    {:class classes
     :on-click (fn [e]
                 (when selectable
                   (put! game-event-chan {:type :select-card
                                          :index index}))
                 nil)}))

; xxx spells are selectable when
; (and (you have enough mana)
;      (it's your turn)
;      (or (the spell requires targets and has some)
;          (the spell requires no targets)))
(s/defn draw-card
  [card :- Card
   index :- s/Int
   selectable :- s/Bool
   game-event-chan]
  [:div (card-props card index selectable game-event-chan)
   (condp = (:type card)
     :minion [draw-minion-card card]
     :spell [draw-spell-card card])])

(s/defn draw-hero
  [hero :- Hero
   board :- Board
   game-event-chan]
  (let [hero-is-alive (not (and (= (safe-get-in board [:mode :type])
                                   :game-over)
                                (= (other-player (safe-get-in board [:mode :winner]))
                                   (first (path-to-character board (:id hero))))))]
    (if hero-is-alive
      [:div (character-properties board hero game-event-chan)
       [:div.name (:name hero)]
       (when (> (get-attack hero) 0)
         [:div.attack (get-attack hero)])
       [draw-character-health hero]]

      [:div.hero
         [:div.loser "X"]])))

(defn draw-minion [minion board is-owners-turn game-event-chan]
  [:div (character-properties board minion game-event-chan)
   [:div.name (:name minion)]
   (if (> (get-attack minion)
          (minion :base-attack))
     [:div.attack.buffed (get-attack minion)]
     [:div.attack (get-attack minion)])
   ; XXXX - weapons have deathrattles and powers too, so split this out into something that's reusable for them
   [:div.minion-attributes
    (when (has-taunt? minion)
      [:i {:class "fa fa-shield fa-2x"}])
    (when (has-summoning-sickness? minion)
      [:strong "Z"])
    ; TODO - icon classes - user-times for deathrattles
    ; flash for powers/abilities
    ; TODO stealth, divine shield, windfury, enrage
    ]
   [draw-character-health minion]])

(s/defn draw-mana-tray
  [board-half :- BoardHalf
   player :- Player]
  [:div.mana-tray
   (for [i (range (:max (get-mana board-half)))]
     ^{:key [player i]} [:div {:class (str
                                        "mana-crystal-container "
                                        (when (>= i (:actual (get-mana board-half))) "spent"))}
                         [:i {:class "fa fa-diamond"}]])])

(s/defn draw-board-half
  [board :- Board
   player :- Player
   game-state :- GameState]
  (let [board-half (board player)
        is-owners-turn (= (board :whose-turn) player)]
    [:div {:class (str
                    "board-half "
                    (when is-owners-turn "is-owners-turn"))}
     [:div.hand
      [:h3 (:name (:hero board-half))]
      (for [[index card] (map-indexed vector (:hand board-half))]
        ^{:key (:id card)} [draw-card card index (card-is-playable? card (:actual (get-mana (board player))) board player) (game-state :game-event-chan)])]
     [:div.body
      [draw-hero (:hero board-half) board (game-state :game-event-chan)]
      [draw-mana-tray board-half player]
      [:div.minion-container
        (for [minion (:minions board-half)]
          ^{:key (:id minion)} [draw-minion minion board is-owners-turn (game-state :game-event-chan)])]]]))

(s/defn draw-end-turn-button [game-state :- GameState]
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

(s/defn draw-mulligan-card
  [mulligan-card :- MulliganCard
   index :- s/Int
   game-event-chan]
  (let [card (mulligan-card :card)]
    [:div (card-props (mulligan-card :card) index true game-event-chan)
     (when (not (mulligan-card :selected))
       [:div.unselected-mulligan-card "X"])
     (condp = (:type card)
       :minion [draw-minion-card card]
       :spell [draw-spell-card card])]))

(defn draw-mulligan [board game-state]
  [:div
   [:div.accept-mulligan {:on-click #(do
                                      (put! (game-state :game-event-chan) {:type :accept-mulligan})
                                      nil)}
    "Complete Mulligan"]
   [:div.mulligan-container
     (for [[index card] (map-indexed vector (safe-get-in board [:mode :cards]))]
       ^{:key (str "mulligan" (:id (:card card)))} [draw-mulligan-card card index (game-state :game-event-chan)])]])

(defn draw-board-mode [board game-state]
  (condp = (:type (board :mode))
    :default nil
    :mulligan (draw-mulligan board game-state)
    :targeting (draw-cancel-button board game-state "Cancel Targeting")
    :positioning (draw-cancel-button board game-state "Cancel Positioning")
    :game-over (draw-game-over (:winner (board :mode)))))

(s/defn draw-board [game-state :- GameState]
  (let [board @(game-state :board-atom)
        classes (str
                  "board "
                  (name (safe-get-in board [:mode :type])))]
    [:div
     (when (contains? #{:mulligan :tracking :choose-one} (safe-get-in board [:mode :type]))
       [:div.overlay])
     [:div {:class classes}
      [draw-board-half board :player-1 game-state]
      [draw-board-half board :player-2 game-state]
      (when (= (safe-get-in board [:mode :type]) :default)
        [draw-end-turn-button game-state])
      [draw-combat-log board]
      [draw-board-mode board game-state]]]))

; XXXXX saw a situation where jaina had two cards instead of three in opening hand
; js console had this message
; Warning: flattenChildren(...): Encountered two children with the same key, `.1:$26`. Child keys must be unique; when two children share a key, only the first child will be used.

; looks like she wound up with two copies of the same sen'jin shieldmasta, both with id 26. how?

(defn handle-game-events [{:keys [game-event-chan board-atom]}]
  (go-loop []
    (let [msg (<! game-event-chan)
          board @board-atom
          board-mode (safe-get-in board [:mode :type])]
      (when (not= board-mode :game-over)
        (match [board-mode (:type msg)]
          [:mulligan :select-card] (swap! board-atom toggle-mulligan-card-selected (:index msg))
          [:mulligan :accept-mulligan] (swap! board-atom run-continuation)
          [:default :character-selected] (swap! board-atom enter-targeting-mode-for-attack (get-character-by-id @board-atom (msg :character-id)))
          [:targeting :character-selected] (swap! board-atom run-continuation (get-character-by-id @board-atom (msg :character-id)))
          [_ :select-card] (when (= board-mode :default)
                           (swap! board-atom play-card (:whose-turn board) (msg :index)))
          [_ :end-turn] (when (= board-mode :default)
                          (swap! board-atom end-turn))
          [(_ :guard #(not= :default %)) :cancel-mode] (swap! board-atom assoc :mode DefaultMode))
        (recur)))))

(defn draw-board-atom [board-atom]
  (let [game-state {:board-atom board-atom
                    :game-event-chan (chan)}]

    (r/render-component [draw-board game-state]
                        (js/document.getElementById "content"))

    (handle-game-events game-state)))
