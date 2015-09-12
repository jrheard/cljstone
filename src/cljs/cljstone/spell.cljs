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
  (into {:type :spell
         :id (get-next-card-id)}
        spell))
