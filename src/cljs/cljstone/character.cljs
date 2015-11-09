(ns cljstone.character
  (:require [schema.core :as s])
  (:use [clojure.set :only [difference]]
        [plumbing.core :only [safe-get safe-get-in]]))

; Schemas

(s/defschema Player (s/enum :player-1 :player-2))

(s/defschema CharacterEffect
  {(s/optional-key :base-health) s/Int
   (s/optional-key :base-attack) s/Int
   (s/optional-key :base-attack-value) s/Int
   (s/optional-key :health) s/Int
   (s/optional-key :attack) s/Int
   (s/optional-key :cant-attack) (s/enum true)
   (s/optional-key :charge) (s/enum true)
   (s/optional-key :divine-shield) (s/enum true)
   (s/optional-key :frozen) (s/enum true) ; TODO
   (s/optional-key :stealth) (s/enum true) ; TODO
   (s/optional-key :taunt) (s/enum true)})

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

; XXXX this is about to get more complicated.
; talking about implementing hunter's mark.
; change buffs like shattered sun to be :base-attack-modifier
; when hunter's mark is applied, remove all base attack modifiers, add a :base-attack
; XXXXXXXXX BUT THEN REAPPLY BUFFS FOR STORMWIND CHAMPION, DIRE WOLF ALPHA, ETC - FUCK
;
; ok so certain buffs can be applied by auras.
; mainly base attack, base health, and cant-be-targeted-by-spells.
; we have two main options on how to implement auras
; 1) modify all these get-base-attack, etc functions to take [minion minions-list] (i do not like this)
; 2) give affected minions an aura-modifier, and recalculate all aura modifiers whenever a minion enters or leaves the board
; so if a harvest golem is next to a dire wolf alpha, it will have a {:base-attack-aura-modifier 1}
; aura modifiers are *not* removed when minions are silenced, or have their attack or health set to 1, etc.

(s/defn get-base-attack :- s/Int
  [character :- Character]
  (if-let [value-modifier (last (filter #(contains? (safe-get % :effect)
                                                    :base-attack-value)
                                         (safe-get character :modifiers)))]
    (safe-get-in value-modifier [:effect :base-attack-value])
    (+ (:base-attack character)
       (sum-modifiers character :base-attack))))

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
  ; TODO:
  ; one-turn effects like abusive sergeant
  ; aura effects like dire wolf alpha buff
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
