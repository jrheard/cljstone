(ns cljstone.card
  (:require [schema.core :as s])
  (:use [cljstone.minion :only [MinionSchematic all-minions]]))

(def NUM-CARDS-IN-DECK 30)

(def next-card-id (atom 0))

(s/defn get-next-card-id :- s/Int []
  (let [id-to-return @next-card-id]
    (swap! next-card-id inc)
    id-to-return))

(def Card
  {:type (s/enum :minion :spell :weapon)
   :name s/Str
   :mana-cost s/Int
   :id s/Int
   s/Any s/Any})

(s/defn minion-schematic->card :- Card
  [schematic :- MinionSchematic]
  {:type :minion
   :name (:name schematic)
   :mana-cost (rand-int 10)
   :id (get-next-card-id)
   :minion-schematic schematic})

; TODO also eventually a spell->card function

(s/defn make-random-deck :- [Card]
  []
  (map minion-schematic->card
       (repeatedly NUM-CARDS-IN-DECK #(rand-nth (vals all-minions)))))


; TODO: to implement thaurissan, freezing trap, etc, add a :modifiers list to Cards too, just like minions
; no clue how molten giant, mountain giant, etc will work though.
; i mean there'll definitely have to be a (get-mana-cost card) function - perhaps it also takes the entire board? eg for clockwork giant
; god, maybe protocols might be useful here - eh probably overkill actually
; mountain giant schema will just have a :calculate-base-mana-cost -> (fn [board which-half-is-owner] foo) k/v pair,
; and then *that* calculated figure can have modifiers laid on top of it. so in this way, a mountain giant can have
; original base mana cost 12, then *calculated* base mana cost 8, and can have a {:mana-cost -1} modifier in its :modifiers list
; that'll give it a final calculated mana cost value of 7. i like this system, i think it can work, neat.

; TODO - perhaps TargetingStrategies for battlecries/spells/attacking - minion attacking takes taunt into account, spells/battlecries don't
