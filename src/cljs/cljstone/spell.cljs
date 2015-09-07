(ns cljstone.spell
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board]]
        [cljstone.character :only [Player other-player]]
        [cljstone.combat :only [cause-damage]]))


(s/defschema Spell
  {:name s/Str
   :effect (s/=> Board Board Player)
   :mana-cost s/Int
   :class (s/enum :neutral :mage :shaman)
   ; TODO - an optional :targeting-fn k/v pair?
   }
  )

(def all-spells
  {:flamecannon {:name "Flamecannon"
                 :mana-cost 2
                 :class :mage
                 :effect (s/fn [board caster]
                           (when-let [minions (get-in board [(other-player caster) :minions])]
                             (cause-damage board
                                           (:id (rand-nth minions))
                                           {:type :damage-spell
                                            :name "Flamecannon"
                                            :effect {:health -4}})))}})