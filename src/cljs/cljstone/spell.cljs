(ns cljstone.spell
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card get-next-card-id]]
        [cljstone.combat :only [cause-damage get-enemy-characters get-enemy-minions]]))

(s/defschema Spell
  {:name s/Str
   :effect s/Any ; (board, player) -> board
   :mana-cost s/Int
   :class (s/enum :neutral :mage :shaman)})

(s/defn spell->card :- Card
  [spell :- Spell]
  (assoc (into {:type :spell
                :id (get-next-card-id)}
               spell)
         :effect
         (fn [board player new-hand]
           (-> board
               ((spell :effect) player)
               (update-in [player :mana-modifiers] conj (- (:mana-cost spell)))
               (assoc-in [player :hand] new-hand)))))
