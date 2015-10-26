(ns cljstone.character
  (:require [schema.core :as s])
  (:use [clojure.set :only [difference]]))

; Schemas

(s/defschema Player (s/enum :player-1 :player-2))

(s/defschema CharacterEffect
  {(s/optional-key :base-health) s/Int
   (s/optional-key :base-attack) s/Int
   (s/optional-key :health) s/Int
   (s/optional-key :attack) s/Int
   (s/optional-key :cant-attack) (s/enum true)
   (s/optional-key :charge) (s/enum true)
   (s/optional-key :divine-shield) (s/enum true)
   (s/optional-key :frozen) (s/enum true) ; TODO
   (s/optional-key :stealth) (s/enum true) ; TODO
   (s/optional-key :taunt) (s/enum true)})

(s/defschema CharacterModifier
  {:type (s/enum :attack :damage-spell :enchantment :mechanic)
   (s/optional-key :name) s/Str
   (s/optional-key :turn-begins) s/Int
   (s/optional-key :turn-ends) s/Int
   :effect CharacterEffect})

; TODO attacks-this-turn, attacks-per-turn

(s/defschema Character
  {:id s/Int
   :type (s/enum :hero :minion)
   :base-health (s/conditional #(>= % 0) s/Int)
   :base-attack (s/conditional #(>= % 0) s/Int)
   :modifiers [CharacterModifier]
   s/Any s/Any})

(s/defn other-player :- Player
  [player :- Player]
  (first (difference #{:player-1 :player-2} #{player})))

(s/defn sum-modifiers :- s/Int
  [character :- Character
   kw :- s/Keyword]
  (apply + (map (fn [modifier]
                  (kw (modifier :effect) 0))
                (character :modifiers))))

(s/defn get-base-attack :- s/Int
  [character :- Character]
  (+ (:base-attack character)
     (sum-modifiers character :base-attack)))

(s/defn get-base-health :- s/Int
  [character :- Character]
  (+ (:base-health character)
     (sum-modifiers character :base-health)))

(s/defn get-health :- s/Int
  [character :- Character]
  (+ (get-base-health character)
     (sum-modifiers character :health)))

(s/defn get-attack :- s/Int
  [character :- Character]
  (get-base-attack character))

(s/defn has-taunt? :- s/Bool
  [character :- Character]
  (boolean (some #(get-in % [:effect :taunt]) (character :modifiers))))

(s/defn has-charge? :- s/Bool
  [character :- Character]
  (boolean (some #(get-in % [:effect :charge]) (character :modifiers))))

(s/defn has-divine-shield? :- s/Bool
  [character :- Character]
  (boolean (some #(get-in % [:effect :divine-shield]) (character :modifiers))))

(s/defn has-summoning-sickness? :- s/Bool
  [character :- Character]
  (boolean (some #(= (:name %) "Summoning Sickness")
                 (character :modifiers))))

(s/defn can-attack? :- s/Bool
  [character :- Character]
  (and (< (character :attacks-this-turn)
          (character :attacks-per-turn))
       (> (get-attack character) 0)
       (not (or (some #(get-in % [:effect :cant-attack])
                      (character :modifiers))
                ; frozen differs from cant-attack in that it's drawn specially, and that it triggers bonus ice lance damage
                (some #(get-in % [:effect :frozen])
                      (character :modifiers))))))
