(ns cljstone.spell
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board]]
        [cljstone.card :only [Card get-next-card-id]]
        [cljstone.character :only [Player other-player]]
        [cljstone.combat :only [cause-damage get-enemy-characters get-enemy-minions]]))

(s/defschema Spell
  {:name s/Str
   :effect (s/=> Board Board Player)
   :mana-cost s/Int
   :class (s/enum :neutral :mage :shaman)})
; TODO - an optional :targeting-fn k/v pair?

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
                               (let [enemies (get-enemy-characters board caster)]
                                 (take 3
                                       (iterate (fn [board]
                                                  (cause-damage board
                                                                (:id (rand-nth enemies))
                                                                {:type :damage-spell
                                                                 :name "Arcane Missiles"
                                                                 :effect {:health -1}}))
                                                board))))}})

(s/defn spell->card :- Card
  [spell :- Spell]
  (into {:type :spell
         :id (get-next-card-id)}
        spell))
