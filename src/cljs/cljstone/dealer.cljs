(ns cljstone.dealer
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card]]
        [cljstone.bestiary :only [all-minions]]
        [cljstone.minion :only [minion-schematic->card]]
        [cljstone.spell :only [Spell spell->card]]
        [cljstone.spellbook :only [all-spells]]))

(def NUM-CARDS-IN-DECK 30)

(def vanilla-minions
  (filter #(not (contains? % :battlecry))
          (vals all-minions)))

(s/defn make-random-deck :- [Card]
  []
  (let [minions (mapv minion-schematic->card (vals all-minions))
        spells (mapv spell->card (vals all-spells))
        all-cards (concat minions spells)]
    (take NUM-CARDS-IN-DECK (shuffle all-cards))))
