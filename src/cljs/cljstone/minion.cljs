(ns cljstone.minion
  (:require [schema.core :as s])
  (:use [cljstone.character :only [CharacterModifier]]))

; todo - what about aldor peacekeeper? it sets a new base attack
; what about blessing of wisdom? a :buff with an :on-attack -> function k/v pair?

(def MinionSchematic
  {:name s/Str
   (s/optional-key :class) (s/enum :neutral :mage :shaman)
   :attack s/Int
   :health s/Int
   (s/optional-key :modifiers) [CharacterModifier]})

; TODO - charge? freeze? divine shield? taunt? stealth?
; nvm they'll just be modifiers

; hm - how do you implement silencing something that's taken damage and has also had its HP buffed?
; or what about if something's taken damage, had its HP buffed by stormwind champ, and the champ dies?
; what about one-turn attack buffs, like from dark iron dwarf?
; i guess Modifiers will have to have associated :turn-played, :turn-expires?
; yeah, definitely go with :turn-expires.
; also: summoning sickness can be implemented as a one-turn modifier with effect :cant-attack true
; freezing will work similarly
; charge minions can be denoted in their schematics as (s/Maybe :charge) s/Bool
(def Minion
  {:name s/Str
   :class (s/enum :neutral :mage :shaman)
   :base-attack s/Int
   :base-health s/Int
   :id s/Int
   :modifiers [CharacterModifier]})

; Schematics

(def all-minions
  {:wisp {:name "Wisp" :attack 1 :health 1}
   :shieldbearer {:name "Shieldbearer" :attack 0 :health 4}
   :goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2}
   :bloodfen-raptor {:name "Bloodfen Raptor" :attack 3 :health 2}
   :river-crocilisk {:name "River Crocilisk" :attack 2 :health 3}
   :magma-rager {:name "Magma Rager" :attack 5 :health 1}
   :chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5}
   :oasis-snapjaw {:name "Oasis Snapjaw" :attack 2 :health 7}
   :boulderfist-ogre {:name "Boulderfist Ogre" :attack 6 :health 7}
   :war-golem {:name "War Golem" :attack 7 :health 7}})


; TODO - minion types like :beast, :dragon, :mech


; TODO - on-before-attack for ogre brute, on-after-attack for mistress of pain

(s/defn make-minion :- Minion
  ; TODO - MinionSchematics will eventually have k/v pairs like :on-summon-minion a-fn
  ; and minions will have k/v pairs like :on-summon-minion a-channel;
  ; the machinery that sets up those channels and hooks them up to those functions will live here.
  [schematic :- MinionSchematic
   id :- s/Int]
  (into {:base-attack (:attack schematic)
         :base-health (:health schematic)
         :id id
         :modifiers []
         :class :neutral}
        (dissoc schematic :attack :health)))

; todo jesus how do you implement dire wolf alpha
; i guess you just add a +1 attack modifier to each of the two adjacent minions, and add a -1 when the wolf dies
; nah, i don't like that. what turn would the effect expire on? are there other effects that can be lost on the same turn that they're gained on?
; divine shield, i guess, but that's different calculated at attack time
; hm - silence can cause you to lose modifiers that you've gaind on this turn / aren't slated to expire yet
; i guess: in addition to having a :turn-expires, effects can have an :active
; still need to figure out what happens when you silence something that's had its health buffed and has taken 1 damage, though.
; i guess silencing will involve recomputing base attack and health, and so you can figure it out at recompute-because-of-silence time.
; hm.

(s/defn get-health :- s/Int
  [minion :- Minion]
  (+ (minion :base-health)
     (apply + (map (fn [modifier]
                     (:health (modifier :effect) 0))
                   (minion :modifiers)))))

(s/defn get-attack :- s/Int
  [minion :- Minion]
  (minion :base-attack))

; these'll eventually actually do stuff - base attack / health can be modified by eg reversing switch, shattered sun cleric, etc
(s/defn get-base-attack :- s/Int
  [minion :- Minion]
  (:base-attack minion))

(s/defn get-base-health :- s/Int
  [minion :- Minion]
  (:base-health minion))
