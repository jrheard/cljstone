(ns cljstone.minion
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board Minion add-modifier-to-character]]
        [cljstone.card :only [Card get-next-card-id]]
        [cljstone.character :only [Player Character CharacterModifier get-next-character-id]]))

(def Battlecry
  {:targeting-fn (s/=> [Character] Board Player)
   :effect-fn (s/=> Board Board s/Int)})

(def MinionSchematic
  {:name s/Str
   (s/optional-key :class) (s/enum :neutral :mage :shaman)
   :attack s/Int
   :health s/Int
   (s/optional-key :battlecry) Battlecry
   (s/optional-key :modifiers) [CharacterModifier]})

; hm - how do you implement silencing something that's taken damage and has also had its HP buffed?
; or what about if something's taken damage, had its HP buffed by stormwind champ, and the champ dies?
; what about one-turn attack buffs, like from dark iron dwarf?
; i guess Modifiers will have to have associated :turn-played, :turn-expires?
; yeah, definitely go with :turn-expires.
; also: summoning sickness can be implemented as a one-turn modifier with effect :cant-attack true
; freezing will work similarly

; Schematics

(def all-minions
  {:wisp {:name "Wisp" :attack 1 :health 1}
   :shieldbearer {:name "Shieldbearer" :attack 0 :health 4}
   :goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2}
   :bloodfen-raptor {:name "Bloodfen Raptor" :attack 3 :health 2}
   :river-crocilisk {:name "River Crocilisk" :attack 2 :health 3}
   :shattered-sun {:name "Shattered Sun Cleric" :attack 3 :health 2
                   :battlecry {:targeting-fn (fn [board player]
                                               (get-in board [player :minions]))
                               :effect-fn (fn [board target-minion-id]
                                            (add-modifier-to-character board
                                                                       target-minion-id
                                                                       {:type :buff :name "Shattered Sun" :effect {:base-health 1 :base-attack 1}}))}}
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
         :attacks-this-turn 0
         :attacks-per-turn 1 ; TODO - turn into :base-attacks-per-turn, there'll be modifiers that can add 1 to this number (will freezing remove 1 from this number?)
         :id id
         :modifiers []
         :class :neutral}
        (dissoc schematic :attack :health :battlecry)))


(s/defn play-minion-card :- Board
  [board :- Board
   player :- Player
   schematic :- MinionSchematic]
  ; TODO how do we implement battlecries?
  ; check to see if schematic has a :battlecry
  ; call (battlecry :targeting-fn), returns a list of character ids
  ; set board state to :targeting
  ; add .targeting class to main board div
  ; add .acceptable-target class to minions/heroes that can be targeted
  ;
  ; XXXX SOMEHOW BLOCK AND WAIT FOR USER INPUT
  ; ??????? somehow accept user input
  ;
  ; call ((schematic :effect-fn) target-character-id)
  ; play minion
  (update-in board
             [player :minions]
             conj
             (make-minion schematic (get-next-character-id))))

(s/defn minion-schematic->card :- Card
  [schematic :- MinionSchematic]
  {:type :minion
   :name (:name schematic)
   :mana-cost (rand-int 10)
   :id (get-next-card-id)
   :class :neutral ; XXXXX TODO standardize on where this is handled
   :attack (:attack schematic)
   :health (:health schematic)
   :effect (fn [board player]
             (play-minion-card board player schematic))})


; dire wolf alpha only needs to care about on-summon-friendly-minion, on-friendly-minion-death - no other situations cause a dire wolf alpha buff/debuff
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

(s/defn can-attack :- s/Bool
  [minion :- Minion]
  (and (< (minion :attacks-this-turn)
          (minion :attacks-per-turn))
       (> (get-attack minion) 0)))

; these'll eventually actually do stuff - base attack / health can be modified by eg reversing switch, shattered sun cleric, etc
(s/defn get-base-attack :- s/Int
  [minion :- Minion]
  (:base-attack minion))

(s/defn get-base-health :- s/Int
  [minion :- Minion]
  (:base-health minion))
