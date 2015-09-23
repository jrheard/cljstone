(ns cljstone.combat
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board path-to-character]]
        [cljstone.character :only [Character CharacterModifier Player other-player get-attack get-health]]
        [cljstone.combat-log :only [log-an-item]]
        [cljstone.minion :only [Minion]]))

(s/defn find-dead-characters-in-board :- [Character]
  [board :- Board]
  ; TODO sort characters by :id ascending
  (let [characters (concat (get-in board [:player-1 :minions])
                           [(get-in board [:player-1 :hero])]
                           (get-in board [:player-2 :minions])
                           [(get-in board [:player-2 :hero])])]
    (let [dead-characters (filter #(<= (get-health %) 0) characters)]
      (or dead-characters []))))

; TODO have a separate kill-minion function: fires deathrattle, on-minion-death [eg flesheating ghoul, cult master], and calls remove-minion
; also will be used by twisting nether, assassinate, etc
(s/defn remove-minion :- Board
  [board :- Board
   minion-id :- s/Int]
  (let [minions-path (take 2 (path-to-character board minion-id))
        vec-without-minion (vec (remove #(= (:id %) minion-id)
                                        (get-in board minions-path)))]
    (assoc-in board minions-path vec-without-minion)))

(s/defn process-death :- Board
  [board :- Board
   character :- Character]
  (condp = (:type character)
    ; TODO game-over mode
    ; TODO eventually program in draws if both heroes are dead
    :hero board
    :minion (remove-minion board (:id character))))

(s/defn cause-damage :- Board
  [board :- Board
   character-id :- s/Int
   modifier :- CharacterModifier
   ; TODO document
   & [delay-death]]
  (let [character-path (path-to-character board character-id)
        modifiers-path (conj character-path :modifiers)]
    (let [board (-> board
                    (update-in modifiers-path conj modifier)
                    (log-an-item modifier nil (get-in board character-path)))]
      (if delay-death
        board
        (reduce process-death board (find-dead-characters-in-board board))))))

(s/defn create-attack-modifier :- CharacterModifier
  [c1 :- Character
   c2 :- Character]
  ; TODO eventually take divine shield into account
  {:type :attack
   :name nil
   :effect {:health (- (get-attack c1))}})

; TODO - when we get around to implementing secrets - what if get-down is up and bloodfen raptor attacks into something, and get-down kills it?
; how do you prevent its original target from taking damage?
; perhaps an on-before-attack event gets fired and switches the target around - that could work pretty well
; on-before-attack could also work for eg explosive trap, lets you kill the minion before it actually causes any damage
(s/defn attack :- Board
  [board :- Board
   attacker-id :- s/Int
   defender-id :- s/Int]
  (let [[attacker defender] (map #(->> %
                                       (path-to-character board)
                                       (get-in board))
                                 [attacker-id defender-id])]
    (-> board
        (cause-damage defender-id (create-attack-modifier attacker defender) true)
        (cause-damage attacker-id (create-attack-modifier defender attacker) true)
        (update-in (conj (path-to-character board attacker-id) :attacks-this-turn) inc)
        (#(reduce process-death % (find-dead-characters-in-board %))))))

; if two magma ragers attack each other and both kill each other, both attacks should go off, and *then* they should die.
; so the first causes damage to the second, the second causes damage to the first
; and THEN 



; ok ok ok
; it's all just functions that take boards and return boards
; except some of them can trigger events
; anyway there should be a perform-attack function that takes two characters
; and it triggers on-before-attack,
; and then it does (when (look up attacker in board) actually perform attack)
; because the attacker may have died in the meantime

(s/defn get-enemy-minions :- [Minion]
  [board :- Board
   player :- Player]
  (get-in board [(other-player player) :minions]))

(s/defn get-enemy-characters :- [Character]
  [board :- Board
   player :- Player]
  ; TODO concat the enemy hero to the result of get-enemy-minions
  (get-enemy-minions board player))
