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
                 :effect (s/fn [board :- Board caster :- Player]
                           (when-let [minions (get-enemy-minions board caster)]
                             (cause-damage board
                                           (:id (rand-nth minions))
                                           {:type :damage-spell
                                            :name "Flamecannon"
                                            :effect {:health -4}})))}
   :arcane-missiles {:name "Arcane Missiles"
                     :mana-cost 1
                     :class :mage
                     :effect (fn [board caster]
                               ; xxxxxxxxxxxxxxxx this won't work
                               ; grim-reaper only exists on the board *atom*
                               ; so i guess we just update get-enemy-characters to filter out 0-health ones?
                               ; xxxxx i guess cause-damage can *also* invoke grim-reaper
                               ; can't think of any reason why not
                               (nth (iterate (fn [board]
                                              (cause-damage board
                                                            (:id (rand-nth (get-enemy-characters board caster)))
                                                            {:type :damage-spell
                                                             :name "Arcane Missiles"
                                                             :effect {:health -1}}))
                                            board)
                                    3))}})

