(ns cljstone.combat
  (:require [schema.core :as s]
            [reagent.ratom :as ratom])
  (:use [cljstone.board :only [Board path-to-character]]
        [cljstone.character :only [Character CharacterModifier]]
        [cljstone.minion :only [get-attack]]))

(s/defn cause-damage!
  [board :- ratom/RAtom
   character-id :- s/Int
   modifier :- CharacterModifier]
  (let [modifiers-path (conj (path-to-character @board character-id) :modifiers)]
    (swap! board update-in modifiers-path conj modifier)))

(defn create-attack-modifier
  [c1 c2]
  ; TODO eventually take divine shield into account
  {:type :attack
   :name nil
   :effect {:health (- (get-attack c1))}})

; TODO - when we get around to implementing secrets - what if get-down is up and bloodfen raptor attacks into something, and get-down kills it?
; how do you prevent its original target from taking damage?
; perhaps an on-before-attack event gets fired and switches the target around - that could work pretty well
; on-before-attack could also work for eg explosive trap, lets you kill the minion before it actually causes any damage
(s/defn attack!
  [board :- ratom/RAtom
   attacker-id :- s/Int
   defender-id :- s/Int]
  (js/console.log "ok we're in attack")
  ; XXXX todo everything's fucked up
  ; gotta clear up where we're in board-land and where we're in atom-land
  ; god is this going to even work? was this idiotic
  ; maybe there should just be pure functions, and the click handlers call them and use swap!
  ; no fuckin clue how channels are gonna work... maybe it's fine
  ; ugh maybe it's not
  ; uuuuuuuughhhhhhhhhhhhhhhhhh
  ; i guess it's probably fine
  ; it's probably fine.
  (let [[attacker defender] (map #(->> %
                                       (path-to-character board)
                                       (get-in board))
                                 [attacker-id defender-id])]
    (js/console.log attacker)
    (js/console.log defender)
    (cause-damage! board defender-id (create-attack-modifier attacker defender))
    (cause-damage! board attacker-id (create-attack-modifier defender attacker))))

; ok ok ok
; it's all just functions that take boards and return boards
; except some of them can trigger events
; anyway there should be a perform-attack function that takes two characters
; and it triggers on-before-attack,
; and then it does (when (look up attacker in board) actually perform attack)
; because the attacker may have died in the meantime
