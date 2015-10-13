(ns cljstone.spellbook
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board]]
        [cljstone.character :only [Player]]
        [cljstone.combat :only [cause-damage get-enemy-characters get-enemy-minions]]))

; TODO utility functions
; let's have a defspell macro that emits like (s/fn :- Board [board :- Board caster :- Player])

(def all-spells
  {:flamecannon {:name "Flamecannon"
                 :mana-cost 2
                 :class :mage
                 ; XXXXXXXX can't cast flamecannon when opponent has an empty board!
                 ; how do we encode this?
                 ; how about a :castable -> Board -> bool predicate fn?
                 ; currently the game crashes if you play flamecannon on an empty board
                 :effect (s/fn [board :- Board caster :- Player]
                           (when-let [minions (get-enemy-minions board caster)]
                             (cause-damage board
                                           (rand-nth minions)
                                           {:type :damage-spell
                                            :name "Flamecannon"
                                            :effect {:health -4}})))}
   :arcane-missiles {:name "Arcane Missiles"
                     :mana-cost 1
                     :class :mage
                     :effect (fn [board caster]
                               ; TODO finish+test
                               (nth (iterate (fn [board]
                                              (cause-damage board
                                                            (rand-nth (get-enemy-characters board caster))
                                                            {:type :damage-spell
                                                             :name "Arcane Missiles"
                                                             :effect {:health -1}}))
                                            board)
                                    3))}})

