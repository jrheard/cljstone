(ns cljstone.character
  (:require [schema.core :as s])
  (:use [clojure.set :only [difference]]
        [plumbing.core :only [safe-get safe-get-in]]))

; Schemas

(s/defschema Player (s/enum :player-1 :player-2))

(s/defschema CharacterEffect
  {(s/optional-key :base-health) s/Int ; sets the character's base health to %
   (s/optional-key :base-attack) s/Int ; sets the character's base attack to %
   (s/optional-key :health) s/Int ; increases or decreases the character's current health by %. if this is an :enchantment, can increase the character's maximum health.
   (s/optional-key :attack) s/Int ; increases or decreases the character's current attack by %
   (s/optional-key :cant-attack) (s/enum true) ; if true, the character cannot attack
   (s/optional-key :charge) (s/enum true) ; if true, the character is not affected by summoning sickness
   (s/optional-key :divine-shield) (s/enum true) ; if true, the next attack made to this character will do 0 damage
   (s/optional-key :frozen) (s/enum true) ; TODO - if true, the character cannot attack [and is more vulnerable to certain spells]
   (s/optional-key :stealth) (s/enum true) ; TODO - if true, enemy characters/spells/heropowers cannot target this character. overrides taunt.
   (s/optional-key :taunt) (s/enum true)} ; if true, enemy characters cannot attack other friendly minions that do not have taunt. does not affect spells / hero powers.
)

(s/defschema CharacterModifier
  {:type (s/enum :attack :damage-spell :enchantment :mechanic :aura)
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

; ok so certain buffs can be applied by auras.
; mainly attack, health, and cant-be-targeted-by-spells.
; we have two main options on how to implement auras
; 1) modify all these get-attack, etc functions to take [minion minions-list] (i do not like this)
; 2) give affected minions an aura-modifier, and recalculate all aura modifiers whenever a minion enters or leaves the board
; so if a harvest golem is next to a dire wolf alpha, it will have a {:attack-aura-modifier 1}
; aura modifiers are *not* removed when minions are silenced, or have their attack or health set to 1, etc.

(s/defn get-base-attack :- s/Int
  [character :- Character]
  (if-let [base-attack-modifier (last (filter #(contains? (safe-get % :effect)
                                                    :base-attack)
                                         (safe-get character :modifiers)))]
    (safe-get-in base-attack-modifier [:effect :base-attack])
    (:base-attack character)))

(s/defn get-base-health :- s/Int
  [character :- Character]
  (if-let [base-health-modifier (last (filter #(contains? (safe-get % :effect)
                                                    :base-health)
                                         (safe-get character :modifiers)))]
    (safe-get-in base-health-modifier [:effect :base-health])
    (:base-health character)))

(s/defn get-health :- s/Int
  [character :- Character]
  (+ (get-base-health character)
     (sum-modifiers character :health)))

(s/defn get-max-health :- s/Int
  [character :- Character]
  (+ (get-base-health character)
     (->> character
          :modifiers
          (filter #(= (:type %) :enchantment))
          (map (fn [modifier]
                 (:health (modifier :effect) 0)))
          (apply +))))

(s/defn get-attack :- s/Int
  [character :- Character]
  ; TODO:
  ; one-turn effects like abusive sergeant
  ; aura effects like dire wolf alpha buff
  (+ (get-base-attack character)
     (sum-modifiers character :attack)))

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
