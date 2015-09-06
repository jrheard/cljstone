(ns cljstone.combat
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board path-to-character]]
        [cljstone.character :only [Character]]
        [cljstone.minion :only [get-attack]]
        )
  )

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

