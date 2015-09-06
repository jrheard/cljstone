(ns cljstone.board
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s]
            [cljstone.hero :as hero])
  (:use [clojure.set :only [difference]]
        [cljstone.card :only [Card make-random-deck]]
        [cljstone.character :only [Character Player get-next-character-id]]
        [cljstone.minion :only [Minion get-attack get-health make-minion]]))

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
      half-1-path (concat [:player-1] half-1-path)
      half-2-path (concat [:player-2] half-2-path)
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
                           :deck (vec (drop STARTING-HAND-SIZE deck))
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

(s/defn modify-characters-for-attack :- [(s/one Character "attacker") (s/one Character "attackee")]
  [character-1 :- Character
   character-2 :- Character]
  (let [create-attack-modifier (fn [c1 c2]
                                 {:type :attack
                                  :name nil
                                  :effect {:health (- (get-attack c1))}})
        character-1 (update-in character-1 [:attacks-this-turn] inc)]
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
    ; i guess it won't be a pure function any more - planning on implementing a (cause-damage!) function, so this will become (attack!)
    ; and will call (cause-damage!) once on the attacker, once on the attackee
    ; TODO - when we get around to implementing secrets - what if get-down is up and bloodfen raptor attacks into something, and get-down kills it?
    ; how do you prevent its original target from taking damage?
    ; perhaps an on-before-attack event gets fired and switches the target around - that could work pretty well
    ; on-before-attack could also work for eg explosive trap, lets you kill the minion before it actually causes any damage
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
                   (map #(assoc % :attacks-this-turn 0) minions)))
      (assoc :turn (+ 1 (:turn board))
             :whose-turn (first (difference #{:player-1 :player-2}
                                            #{(:whose-turn board)})))))
