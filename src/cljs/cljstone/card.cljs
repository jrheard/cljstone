(ns cljstone.card
  (:require [schema.core :as s]))

(def Card
  {:type (s/enum :minion :spell)
   :name s/Str
   :mana-cost s/Int
   :effect-fn s/Any} ; TODO read up on how to document functions using schema
)

; TODO - write a minion-schema->card function
; and also eventually a spell->card function



; TODO: to implement thaurissan, freezing trap, etc, add a :modifiers list to Cards too, just like minions
; no clue how molten giant, mountain giant, etc will work though.
; i mean there'll definitely have to be a (get-mana-cost card) function - perhaps it also takes the entire board? eg for clockwork giant
; god, maybe protocols might be useful here - eh probably overkill actually
; mountain giant schema will just have a :calculate-base-mana-cost -> (fn [board which-half-is-owner] foo) k/v pair,
; and then *that* calculated figure can have modifiers laid on top of it. so in this way, a mountain giant can have
; original base mana cost 12, then *calculated* base mana cost 8, and can have a {:mana-cost -1} modifier in its :modifiers list
; that'll give it a final calculated mana cost value of 7. i like this system, i think it can work, neat.

; todo
; a card is a name, a mana cost, and an effect function
; also a type, spell or minion
; minions all have the same effect function - (summon-minion the-minion)

; TODO - perhaps TargetingStrategies for battlecries/spells/attacking - minion attacking takes taunt into account, spells/battlecries don't
