(ns cljstone.board
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s]
            [cljstone.hero :as hero])
  (:use [cljstone.card :only [Card make-random-deck]]
        [cljstone.character :only [Character Player get-next-character-id other-player]]
        [cljstone.minion :only [Minion get-health make-minion]]))

(s/defschema BoardHalf
  {:hero hero/Hero
   :hand [Card]
   :deck [Card]
   :minions [Minion]})

(s/defschema Board
  {:player-1 BoardHalf
   :player-2 BoardHalf
   :whose-turn Player
   :turn s/Int})

(def STARTING-HAND-SIZE 5)

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

(s/defn remove-minion :- Board
  [board :- Board
   minion-id :- s/Int]
  (let [minions-path (take 2 (path-to-character board minion-id))
        vec-without-minion (vec (remove #(= (:id %) minion-id)
                                        (get-in board minions-path)))]
    (assoc-in board minions-path vec-without-minion)))

(s/defn find-a-dead-character-in-board :- (s/maybe Character)
  [board :- Board]
  (let [all-minions (concat (get-in board [:player-1 :minions])
                            (get-in board [:player-2 :minions]))]
    (when-let [dead-minion (first (filter #(<= (get-health %) 0) all-minions))]
      dead-minion)))

(s/defn make-board :- ratom/RAtom
  [hero-1 :- hero/Hero
   hero-2 :- hero/Hero]
  (let [hero-1-deck (make-random-deck)
        hero-2-deck (make-random-deck)
        make-board-half (fn [hero deck]
                          {:hero hero
                           :hand (vec (take STARTING-HAND-SIZE deck))
                           :deck []; (vec (drop STARTING-HAND-SIZE deck))
                           :minions []})
        board (r/atom {:player-1 (make-board-half hero-1 hero-1-deck)
                       :player-2 (make-board-half hero-2 hero-2-deck)
                       :whose-turn (rand-nth [:player-1 :player-2])
                       :turn 0})]
    (add-watch board
               :grim-reaper
               (fn [_ board-atom _ new-val]
                 (when-let [dead-character (find-a-dead-character-in-board new-val)]
                   ; TODO if :character is a hero, end the game
                   ; TODO eventually program in draws if both heroes are dead
                   ; TODO if :character is a minion with a deathrattle, fire deathrattle
                   ; TODO also fire on-minion-death for flesheating ghoul, cult master, etc
                   ; anyway for right now dead-character is always a Minion
                   (swap! board-atom remove-minion (:id dead-character)))))
    board))


; ugh - what does this function end up actually looking like?
; minions have to be played on a board-half's minions list, and can be positioned between any of the current elements in that list, or prepended/appended to it.
; some spells can be just tossed into the ether (arcane missiles)
; some have to be targeted (frostbolt)
; and so for those guys (and for minion summoning!), there's the begin-play-card phase, then the targeting phase, then the commit-to-playing-card [args] phase
; for now, though, we can just pretend that targeting doesn't exist, and that all cards are tossed into the ether a la flamecannon.
; but eventually there'll be multiple phases to playing a card, which some cards can skip (eg flamecannon)
(s/defn play-card :- Board
  [board :- Board
   player :- Player
   card-index :- s/Int]
  {:pre [(< -1
            card-index
            (count (get-in board [player :hand])))]}
  ; TODO support playing spells, weapons
  (let [hand (-> board player :hand)
        card (nth hand card-index)
        new-hand (vec (remove #(= (:id %) (:id card)) hand))
        new-minions-vec (conj (vec (get-in board [player :minions]))
                              (make-minion (:minion-schematic card) (get-next-character-id)))]
    (-> board
        (assoc-in [player :hand] new-hand)
        (assoc-in [player :minions] new-minions-vec))))

(s/defn end-turn :- Board
  [board :- Board]
  (-> board
      (update-in [(:whose-turn board) :minions]
                 (fn [minions]
                   (mapv #(assoc % :attacks-this-turn 0) minions)))
      (update-in [:turn] inc)
      (update-in [:whose-turn] other-player)))
