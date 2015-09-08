(ns cljstone.combat
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board path-to-character]]
        [cljstone.character :only [Character CharacterModifier Player other-player]]
        [cljstone.combat-log :only [log-an-item]]
        [cljstone.minion :only [Minion get-attack get-health]]))

(s/defn cause-damage :- Board
  [board :- Board
   character-id :- s/Int
   modifier :- CharacterModifier]
  (let [modifiers-path (conj (path-to-character board character-id) :modifiers)]
    (-> board
        (update-in modifiers-path conj modifier)
        (log-an-item modifier nil (get-in board (path-to-character board character-id))))))

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
        (cause-damage defender-id (create-attack-modifier attacker defender))
        (cause-damage attacker-id (create-attack-modifier defender attacker))
        (update-in (conj (path-to-character board attacker-id) :attacks-this-turn) inc))))

; ok ok ok
; it's all just functions that take boards and return boards
; except some of them can trigger events
; anyway there should be a perform-attack function that takes two characters
; and it triggers on-before-attack,
; and then it does (when (look up attacker in board) actually perform attack)
; because the attacker may have died in the meantime

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

(s/defn get-enemy-minions :- [Minion]
  [board :- Board
   player :- Player]
  (get-in board [(other-player player) :minions]))

(s/defn get-enemy-characters :- [Character]
  [board :- Board
   player :- Player]
  ; TODO concat the enemy hero to the result of get-enemy-minions
  (get-enemy-minions board player))
