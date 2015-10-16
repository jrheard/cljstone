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
  (-> (mapv minion-schematic->card
            (repeatedly NUM-CARDS-IN-DECK #(rand-nth vanilla-minions)))
      (assoc 4 (spell->card (all-spells :arcane-intellect)))
      (assoc 6 (minion-schematic->card (all-minions :shattered-sun)))))
