(ns cljstone.board
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card]]
        [cljstone.character :only [Character CharacterModifier Player other-player]]
        [cljstone.hero :only [Hero]]
        [cljstone.minion :only [Minion]]))

(s/defschema LogEntry
  {:modifier CharacterModifier
   :source (s/maybe Character)
   :target Character
   :id s/Int})

(s/defschema BoardHalf
  {:hero Hero
   :hand [Card]
   :deck [Card]
   :minions [Minion]})

; TODO add a :graveyard to board?
; consider reading logs
; see comments in https://www.reddit.com/r/hearthstone/comments/3k32vh/hearthstone_science_steal_a_card_from_your/

(s/defschema PositioningMode
  {:type :positioning
   :minion Minion
   :continuation s/Any})

(s/defschema TargetingMode
  {:type :targeting
   :targets [Character]
   :continuation s/Any})

(s/defschema MulliganMode
  {:type :mulligan
   :cards [Card]
   :continuation s/Any})

(s/defschema BoardMode
  (s/enum
    {:type :default}
    PositioningMode
    TargetingMode
    MulliganMode))

(s/defschema Board
  {:player-1 BoardHalf
   :player-2 BoardHalf
   :whose-turn Player
   :turn s/Int
   :mode BoardMode
   :combat-log [LogEntry]})

(def STARTING-HAND-SIZE 7)

(s/defn path-to-character :- [s/Any]
  "Returns a vector like [:player-1 :minions 2] telling you where the given character is in the given board."
  [board :- Board
   character-id :- s/Int]
  (let [find-in-board-half (fn [board-half]
                             (if (= character-id (:id (:hero board-half)))
                               [:hero]
                               (let [minions-index (-> board-half
                                               :minions
                                               (#(map :id %))
                                               to-array
                                               (.indexOf character-id))]
                                 (when (not= minions-index -1)
                                   [:minions minions-index]))))
        half-1-path (find-in-board-half (:player-1 board))
        half-2-path (find-in-board-half (:player-2 board))]
    (cond
      half-1-path (vec (concat [:player-1] half-1-path))
      half-2-path (vec (concat [:player-2] half-2-path))
      :else nil)))

(s/defn add-modifier-to-character :- Board
  [board :- Board
   character-id :- s/Int
   modifier :- CharacterModifier]
  (update-in board
             (concat (path-to-character board character-id) [:modifiers])
             conj
             modifier))

(s/defn make-board :- Board
  [hero-1 :- Hero
   deck-1 :- [Card]
   hero-2 :- Hero
   deck-2 :- [Card]]
  (let [make-board-half (fn [hero deck]
                          {:hero hero
                           :hand (vec (take STARTING-HAND-SIZE deck))
                           :deck (vec (drop STARTING-HAND-SIZE deck))
                           :minions []})]
        {:player-1 (make-board-half hero-1 deck-1)
         :player-2 (make-board-half hero-2 deck-2)
         :whose-turn (rand-nth [:player-1 :player-2])
         :turn 0
         :mode {:type :default}
         :combat-log []}))

(s/defn end-turn :- Board
  [board :- Board]
  (-> board
      (update-in [(:whose-turn board) :minions]
                 (fn [minions]
                   (mapv #(assoc % :attacks-this-turn 0) minions)))
      (update-in [:turn] inc)
      (update-in [:whose-turn] other-player)))

; XXXXX TODO TIGHTEN UP
(s/defn run-continuation :- Board
  [board :- Board
   & args]
  {:pre (not= (get-in board [:mode :type]) :default)}
  (apply (get-in board [:mode :continuation])
         (concat [board] args)))

; TODO test modes

(s/defn play-card :- Board
  [board :- Board
   player :- Player
   card-index :- s/Int]
  {:pre [(< -1
            card-index
            (count (get-in board [player :hand])))]}
  (let [hand (-> board player :hand)
        card (nth hand card-index)
        ; TODO - don't remove the card from the player's hand until *after* the targeting phase has completed successfully.
        new-hand (vec (remove #(= (:id %) (:id card)) hand))]
    (-> board
        (assoc-in [player :hand] new-hand)
        ((card :effect) player))))
