(ns cljstone.board
  (:require [reagent.core :as r]
            [schema.core :as s]
            [cljstone.hero :as hero])
  (:use [cljstone.card :only [Card make-random-deck]]
        [cljstone.character :only [Character Player get-next-character-id]]
        [cljstone.minion :only [Minion get-attack get-health make-minion]]))

(def BoardHalf
  {:hero hero/Hero
   :hand [Card]
   :deck [Card]
   :minions [Minion]})

(def Board
  {:player-1 BoardHalf
   :player-2 BoardHalf})

; TODO - eventually have this detect dead heroes and trigger the end of the game
(s/defn find-a-dead-character-in-board :- (s/maybe Character)
  [board :- Board]
  (let [minions (concat (get-in board [:player-1 :minions])
                        (get-in board [:player-2 :minions]))]
    ; TODO - why doesn't (some) work here? kills the entire board-half, not sure why
    (first (filter #(<= (get-health %) 0)
                   minions))))

(s/defn path-to-character :- [s/Any]
  "Returns a vector like [:player-1 :minions 2] telling you where the given character is in the given board."
  [board :- Board
   character-id :- s/Int]
  (let [find-in-board-half (fn [board-half]
                             ; TODO support looking up heroes
                             (let [index (-> board-half
                                             :minions
                                             (#(map :id %))
                                             to-array
                                             (.indexOf character-id))]
                               (when (not= index -1)
                                 [:minions index])))
        half-1-path (find-in-board-half (:player-1 board))
        half-2-path (find-in-board-half (:player-2 board))]
    (if half-1-path
      (concat [:player-1] half-1-path)
      (concat [:player-2] half-2-path))))

(s/defn trim-dead-minion
  [board :- Board
   minion :- Minion]
  (let [minions-vec-path (take 2 (path-to-character board (:id minion)))
        minions-vec-without-dead-minion (vec (remove #(= (:id %) (:id minion))
                                                     (get-in board minions-vec-path)))]
    [minions-vec-path minions-vec-without-dead-minion]))

(s/defn make-board
  [hero-1 :- hero/Hero
   hero-2 :- hero/Hero]
  (let [hero-1-deck (make-random-deck)
        hero-2-deck (make-random-deck)
        make-board-half (fn [hero deck]
                          {:hero hero
                           :hand (vec (take 5 deck))
                           :deck (vec (drop 5 deck))
                           :minions []})
        board (r/atom {:player-1 (make-board-half hero-1 hero-1-deck)
                       :player-2 (make-board-half hero-2 hero-2-deck)})]
    (add-watch board
               :grim-reaper
               (fn [_ board-atom _ new-val]
                 (when-let [dead-minion (find-a-dead-character-in-board new-val)]
                   ; TODO fire deathrattle for dead minion
                   (let [[minions-path minions-vec] (trim-dead-minion new-val dead-minion)]
                     (swap! board-atom assoc-in minions-path minions-vec)))))
    board))

; XXXXXX maybe not all of these functions need to return new Boards, amirite? maybe just like new minions vectors, etc

(s/defn modify-characters-for-attack :- [(s/one Character "attacker") (s/one Character "attackee")]
  [character-1 :- Character
   character-2 :- Character]
  (let [create-attack-modifier (fn [c1 c2]
                                 {:type :attack
                                  :name nil
                                  :effects {:health (- (get-attack c1))}})]
    [(update-in character-1 [:modifiers] conj (create-attack-modifier character-2 character-1))
     (update-in character-2 [:modifiers] conj (create-attack-modifier character-1 character-2))]))

(s/defn attack :- Board
  "Takes a board and a couple of characters that're attacking each other, returns a new post-attack board."
  [board :- Board
   character-id-1 :- s/Int
   character-id-2 :- s/Int]
  (let [[character-1-path character-2-path] (map #(path-to-character board %) [character-id-1 character-id-2])
        [character-1 character-2] (map #(get-in board %) [character-1-path character-2-path])
        [attacked-character-1 attacked-character-2] (modify-characters-for-attack character-1 character-2)]
    ; todo - this is currently a pure function but will eventually want to put messages in channels, which is a side effect. how to reconcile?
    (-> board
        (assoc-in character-1-path attacked-character-1)
        (assoc-in character-2-path attacked-character-2))))

; ugh - what does this function end up actually looking like?
; minions have to be played on a board-half's minions list, and can be positioned between any of the current elements in that list, or prepended/appended to it.
; some spells can be just tossed into the ether (arcane missiles)
; some have to be targeted (frostbolt)
; and so for those guys (and for minion summoning!), there's the begin-play-card phase, then the targeting phase, then the commit-to-playing-card [args] phase
; for now, though, we can just pretend that targeting doesn't exist, and that all cards are tossed into the ether a la flamecannon.
; but eventually there'll be multiple phases to playing a card, which some cards can skip (eg flamecannon)
(s/defn play-card :- Board
  ; TODO - use preconditions to assert that card-index is within the right bounds
  [board :- Board
   player :- Player
   card-index :- s/Int]
  ; TODO support playing spells, weapons
  (let [hand (:hand (player board))
        card (nth hand card-index)
        new-hand (vec (concat (subvec hand 0 card-index) (subvec hand (+ 1 card-index))))
        new-minions-vec (conj (get-in board [player :minions])
                              (make-minion (:minion-schematic card) (get-next-character-id)))]
    (-> board
        (assoc-in [player :hand] new-hand)
        (assoc-in [player :minions] new-minions-vec))))
