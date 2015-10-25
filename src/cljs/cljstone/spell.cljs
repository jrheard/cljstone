(ns cljstone.spell
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card CardClass remove-card-from-list]]
        [cljstone.combat :only [cause-damage get-enemy-characters get-enemy-minions]]
        [cljstone.utils :only [get-next-id]]))

(s/defschema Spell
  {:name s/Str
   :effect s/Any ; (board, player) -> board
   :mana-cost s/Int
   :class CardClass})

(s/defn spell->card :- Card
  [spell :- Spell]
  (assoc (into {:type :spell
                :id (get-next-id)}
               spell)
         :effect
         (fn [board player card]
           (-> board
               ((spell :effect) player)
               (update-in [player :mana-modifiers] conj (- (:mana-cost spell)))
               (update-in [player :hand] remove-card-from-list card)))))
