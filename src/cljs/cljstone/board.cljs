(ns cljstone.board
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card]]
        [cljstone.character :only [Character CharacterModifier Player get-next-character-id other-player]]
        [cljstone.hero :only [Hero]]
        [cljstone.minion :only [Minion make-minion]]))

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

(s/defschema Board
  {:player-1 BoardHalf
   :player-2 BoardHalf
   :whose-turn Player
   :turn s/Int
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
         :combat-log []}))

(s/defn end-turn :- Board
  [board :- Board]
  (-> board
      (update-in [(:whose-turn board) :minions]
                 (fn [minions]
                   (mapv #(assoc % :attacks-this-turn 0) minions)))
      (update-in [:turn] inc)
      (update-in [:whose-turn] other-player)))

; TODO look into using protocols to express minion/spell cards more cleanly
; they basically just need to implement a (play) function and.. that's it?
(s/defn play-spell :- Board
  [board :- Board
   player :- Player
   card :- Card]
  ((card :effect) board player))

(s/defn play-minion :- Board
  [board :- Board
   player :- Player
   card :- Card]
  (assoc-in board
            [player :minions]
            (conj (vec (get-in board [player :minions]))
                  (make-minion (:minion-schematic card) (get-next-character-id)))))

; TODO - eventually implement several phases to playing a card
; minions have to be first a) positioned, then b) optionally targeted [eg bgh, shattered sun]
; spells have to be optionally targeted (flamecannon vs frostbolt)
; so there's choose-minion-position, choose-target, and play-card
; and this function will just be play-card, and will take optional position / target info.
(s/defn play-card :- Board
  [board :- Board
   player :- Player
   card-index :- s/Int]
  {:pre [(< -1
            card-index
            (count (get-in board [player :hand])))]}
  (let [hand (-> board player :hand)
        card (nth hand card-index)
        new-hand (vec (remove #(= (:id %) (:id card)) hand))
        play-fn (condp = (:type card)
                  :spell play-spell
                  :minion play-minion)]
    (-> board
        (assoc-in [player :hand] new-hand)
        (play-fn player card))))
