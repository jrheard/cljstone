(ns cljstone.combat
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board path-to-character]]
        [cljstone.character :only [Character]]
        [cljstone.minion :only [get-attack]]
        )
  )

(s/defn modify-characters-for-attack :- [(s/one Character "attacker") (s/one Character "defender")]
  [character-1 :- Character
   character-2 :- Character]
  (let [create-attack-modifier (fn [c1 c2]
                                 {:type :attack
                                  :name nil
                                  :effect {:health (- (get-attack c1))}})
        character-1 (update-in character-1 [:attacks-this-turn] inc)]
    [(update-in character-1 [:modifiers] conj (create-attack-modifier character-2 character-1))
     (update-in character-2 [:modifiers] conj (create-attack-modifier character-1 character-2))]))

; todo - this is currently a pure function but will eventually want to put messages in channels, which is a side effect. how to reconcile?
; i guess it won't be a pure function any more - planning on implementing a (cause-damage!) function, so this will become (attack!)
; and will call (cause-damage!) once on the attacker, once on the defender
; TODO - when we get around to implementing secrets - what if get-down is up and bloodfen raptor attacks into something, and get-down kills it?
; how do you prevent its original target from taking damage?
; perhaps an on-before-attack event gets fired and switches the target around - that could work pretty well
; on-before-attack could also work for eg explosive trap, lets you kill the minion before it actually causes any damage
(s/defn attack :- Board
  "Takes a board and a couple of characters that're attacking each other, returns a new post-attack board."
  [board :- Board
   attacker-id :- s/Int
   defender-id :- s/Int]
  (let [[attacker-path defender-path] (map #(path-to-character board %) [attacker-id defender-id])
        [attacker defender] (map #(get-in board %) [attacker-path defender-path])
        [attacker defender] (modify-characters-for-attack attacker defender)]
    (-> board
        (assoc-in attacker-path attacker)
        (assoc-in defender-path defender))))
