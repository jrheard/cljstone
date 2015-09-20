(ns cljstone.card
  (:require [schema.core :as s]))

(def next-card-id (atom 0))

(s/defn get-next-card-id :- s/Int []
  (let [id-to-return @next-card-id]
    (swap! next-card-id inc)
    id-to-return))

(s/defschema Card
  {:type (s/enum :minion :spell :weapon)
   :name s/Str
   :mana-cost s/Int
   :id s/Int
   :class s/Any
   :effect s/Any ; a function that takes (board, player) and returns a new Board
   (s/optional-key :attack) s/Int
   (s/optional-key :health) s/Int
   (s/optional-key :durability) s/Int})


; TODO: to implement thaurissan, freezing trap, etc, add a :modifiers list to Cards too, just like minions
; no clue how molten giant, mountain giant, etc will work though.
; i mean there'll definitely have to be a (get-mana-cost card) function - perhaps it also takes the entire board? eg for clockwork giant
; mountain giant schema will just have a :calculate-base-mana-cost -> (fn [board owner] foo) k/v pair,
; and then *that* calculated figure can have modifiers laid on top of it. so in this way, a mountain giant can have
; original base mana cost 12, then *calculated* base mana cost 8, and can have a {:mana-cost -1} modifier in its :modifiers list
; that'll give it a final calculated mana cost value of 7. i like this system, i think it can work, neat.

; TODO - perhaps TargetingStrategies for battlecries/spells/attacking - minion attacking takes taunt into account, spells/battlecries don't
