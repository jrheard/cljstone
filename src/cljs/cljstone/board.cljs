(ns cljstone.board
  (:require [schema.core :as s])
  (:use [cljstone.board-mode :only [BoardMode DefaultMode]]
        [cljstone.card :only [Card]]
        [cljstone.character :only [Character CharacterModifier Player other-player]]
        [cljstone.hero :only [Hero]]
        [cljstone.minion :only [Minion]]
        [plumbing.core :only [safe-get safe-get-in]]))

(s/defschema LogEntry
  {:modifier CharacterModifier
   :source (s/maybe Character)
   :target Character
   :id s/Int})

(s/defschema BoardHalf
  {:hero Hero
   :hand [Card]
   :deck [Card]
   :mana s/Int
   :mana-modifiers [s/Int]
   :minions [Minion]})

; TODO add a :graveyard to board?
; consider reading logs
; see comments in https://www.reddit.com/r/hearthstone/comments/3k32vh/hearthstone_science_steal_a_card_from_your/
(s/defschema Board
  {:player-1 BoardHalf
   :player-2 BoardHalf
   :whose-turn Player
   :turn s/Int
   :mode BoardMode
   :combat-log [LogEntry]})

(def STARTING-HAND-SIZE 7)

; TODO - consider having this guy take an id *or* a character
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

(s/defn get-character-by-id :- Character
  [board :- Board
   character-id :- s/Int]
  (safe-get-in board (path-to-character board character-id)))

(s/defn add-modifier-to-character :- Board
  [board :- Board
   character :- Character
   modifier :- CharacterModifier]
  (update-in board
             (concat (path-to-character board (:id character)) [:modifiers])
             conj
             modifier))

(s/defn begin-turn :- Board
  [board :- Board]
  (-> board
      ; TODO - when we implement eg wild growth, will need to split this out into a standalone increment-mana function
      ; it'll deal with eg giving you an "excess mana" card, etc
      (assoc-in [(board :whose-turn) :mana-modifiers] [])
      (update-in [(board :whose-turn) :mana] #(if (< % 10) (inc %) %))))

(s/defn end-turn :- Board
  [board :- Board]
  (-> board
      (update-in [(:whose-turn board) :minions]
                 (fn [minions]
                   (mapv #(assoc % :attacks-this-turn 0) minions)))
      (update-in [:turn] inc)
      (update-in [:whose-turn] other-player)
      begin-turn))

(s/defn make-board :- Board
  [hero-1 :- Hero
   deck-1 :- [Card]
   hero-2 :- Hero
   deck-2 :- [Card]]
  (let [make-board-half (fn [hero deck]
                          {:hero hero
                           :hand (vec (take STARTING-HAND-SIZE deck))
                           ;:deck [] ; useful for debugging (makes schema error messages much shorter)
                           :deck  (vec (drop STARTING-HAND-SIZE deck))
                           :mana 0
                           :mana-modifiers []
                           :minions []})]
        (begin-turn {:player-1 (make-board-half hero-1 deck-1)
                     :player-2 (make-board-half hero-2 deck-2)
                     :whose-turn (rand-nth [:player-1 :player-2])
                     :turn 0
                     :mode DefaultMode
                     :combat-log []})))

(s/defn run-continuation :- Board
  "Run the board's mode's continuation function. See cljstone.board-mode."
  [board :- Board
   & args]
  {:pre (not= (board :mode) DefaultMode)}
  (apply (safe-get-in board [:mode :continuation])
         (concat [board] args)))

(s/defn get-mana :- {:max s/Int :actual s/Int}
  [board-half :- BoardHalf]
  {:max (+ (:mana board-half)
           (apply + (filter pos? (:mana-modifiers board-half))))
   :actual (+ (:mana board-half)
              (apply + (:mana-modifiers board-half)))})

(s/defn play-card :- Board
  [board :- Board
   player :- Player
   card-index :- s/Int]
  {:pre [(< -1
            card-index
            (count (safe-get-in board [player :hand])))]}

  (let [hand (-> board player :hand)
        card (nth hand card-index)
        new-hand (vec (remove #(= (:id %) (:id card)) hand))]
    ((card :effect) board player new-hand)))
