(ns cljstone.hero
  (:require [schema.core :as s])
  (:use [cljstone.character :only [CharacterModifier get-next-character-id]]))

(s/defschema HeroClass
  (s/enum :druid :hunter :rogue :warlock :priest :mage :warrior :shaman :paladin))

(s/defschema Hero
  {:name s/Str
   :class HeroClass
   :base-health s/Int
   :base-attack s/Int
   :modifiers [CharacterModifier]
   :id s/Int})

; todo hero powers

(s/defn make-hero :- Hero
  [hero-name hero-class]
  {:name hero-name
   :class hero-class
   :base-health 30
   :base-attack 0
   :modifiers []
   :id (get-next-character-id)})
