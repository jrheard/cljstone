(ns cljstone.board
  (:require [reagent.core :as r]
            [schema.core :as s]
            [cljstone.hero :as hero])
  (:use [cljstone.minion :only [Minion MinionSchematic Modifier get-attack get-health make-minion]]))

(def BoardHalf
  {:hero hero/Hero
   :minions [Minion]})

(def Character
  {:id s/Int
   :base-health s/Int
   :base-attack s/Int
   :modifiers [Modifier]
   s/Any s/Any})

(def Board
  {:half-1 BoardHalf
   :half-2 BoardHalf})

; TODO - eventually have this detect dead heroes and trigger the end of the game
(s/defn find-a-dead-character-in-board :- (s/maybe Character)
  [board :- Board]
  (let [minions (concat (get-in board [:half-1 :minions])
                        (get-in board [:half-2 :minions]))]
    (first (filter #(<= (get-health %) 0)
                   minions))))

(s/defn path-to-character :- [s/Any]
  "Returns a vector like [:half-1 :minions 2] telling you where the given character is in the given board."
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
        half-1-path (find-in-board-half (:half-1 board))
        half-2-path (find-in-board-half (:half-2 board))]
    (if half-1-path
      (concat [:half-1] half-1-path)
      (concat [:half-2] half-2-path))))

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
  (let [board (r/atom {:half-1 {:hero hero-1 :minions []}
                       :half-2 {:hero hero-2 :minions []}})]
    (add-watch board
               :grim-reaper
               (fn [_ board-atom _ new-val]
                 (when-let [dead-minion (find-a-dead-character-in-board new-val)]
                   ; TODO fire deathrattle for dead minion
                   (let [[minions-path minions-vec] (trim-dead-minion new-val dead-minion)]
                     (swap! board-atom assoc-in minions-path minions-vec)))))
    board))

(s/defn summon-minion :- Board
  [board :- Board
   which-board-half :- (s/enum :half-1 :half-2)
   schematic :- MinionSchematic
   id :- s/Int]
  ; TODO - MinionSchematics will eventually have k/v pairs like :on-summon-minion a-fn
  ; and minions will have k/v pairs like :on-summon-minion a-channel;
  ; the machinery that sets up those channels and hooks them up to those functions will live here.
  (let [minion (make-minion schematic id)]
    (-> board
        (update-in [which-board-half :minions] conj minion))))

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
