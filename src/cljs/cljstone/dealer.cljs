(ns cljstone.dealer
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card]]
        [cljstone.bestiary :only [all-minions]]
        [cljstone.minion :only [minion-schematic->card]]
        [cljstone.spell :only [Spell spell->card]]
        [cljstone.spellbook :only [all-spells]]))

(def NUM-CARDS-IN-DECK 30)

(s/defn make-random-deck :- [Card]
  []
  (assoc (mapv minion-schematic->card
              (repeatedly NUM-CARDS-IN-DECK #(rand-nth (vals all-minions))))
         4
         (spell->card (all-spells :flamecannon))))
