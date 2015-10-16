(ns cljstone.card
  (:require [schema.core :as s]))

(s/defschema Card
  {:type (s/enum :minion :spell :weapon)
   :name s/Str
   :mana-cost s/Int
   :id s/Int
   :class s/Any
   :effect s/Any ; a function that takes (board, player) and returns a new Board
   (s/optional-key :base-attack) s/Int
   (s/optional-key :base-health) s/Int
   (s/optional-key :durability) s/Int})

(s/defschema CardClass
  (s/enum :neutral :druid :hunter :rogue :warlock :priest :mage :warrior :shaman :paladin))

(s/defn remove-card-from-hand :- [Card]
  [hand :- [Card]
   card :- Card]
  (vec (remove #(= (:id %) (:id card)) hand)))

; TODO: to implement thaurissan, freezing trap, etc, add a :modifiers list to Cards too, just like minions
; no clue how molten giant, mountain giant, etc will work though.
; i mean there'll definitely have to be a (get-mana-cost card) function - perhaps it also takes the entire board? eg for clockwork giant
; mountain giant schema will just have a :calculate-base-mana-cost -> (fn [board owner] foo) k/v pair,
; and then *that* calculated figure can have modifiers laid on top of it. so in this way, a mountain giant can have
; original base mana cost 12, then *calculated* base mana cost 8, and can have a {:mana-cost -1} modifier in its :modifiers list
; that'll give it a final calculated mana cost value of 7. i like this system, i think it can work, neat.
