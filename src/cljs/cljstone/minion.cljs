(ns cljstone.minion
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card get-next-card-id]]
        [cljstone.character :only [Player Character CharacterModifier get-next-character-id]]))

(def MinionSchematic
  {:name s/Str
   (s/optional-key :class) (s/enum :neutral :mage :shaman)
   :attack s/Int
   :health s/Int
   (s/optional-key :battlecry) s/Any ; (Board, target-character-id) -> Board
   (s/optional-key :battlecry-targeting-fn) s/Any ; (Board, Player) -> [Character]
   (s/optional-key :modifiers) [CharacterModifier]})

(s/defschema Minion
  {:name s/Str
   :class (s/enum :neutral :mage :shaman)
   :base-attack s/Int
   :base-health s/Int
   :attacks-this-turn s/Int
   :attacks-per-turn s/Int
   :id s/Int
   :modifiers [CharacterModifier]})

; hm - how do you implement silencing something that's taken damage and has also had its HP buffed?
; or what about if something's taken damage, had its HP buffed by stormwind champ, and the champ dies?
; what about one-turn attack buffs, like from dark iron dwarf?
; i guess Modifiers will have to have associated :turn-played, :turn-expires?
; yeah, definitely go with :turn-expires.
; also: summoning sickness can be implemented as a one-turn modifier with effect :cant-attack true
; freezing will work similarly

; Schematics


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
        (dissoc schematic :attack :health :battlecry :battlecry-targeting-fn)))


(s/defn play-minion-card
  [board
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
             (if-let [battlecry (:battlecry schematic)]
               (assoc board :mode {:type :targeting
                                   :targets ((schematic :battlecry-targeting-fn) board player)
                                   :continuation (fn [board target-character-id]
                                                   (-> board
                                                       (#((schematic :battlecry) % target-character-id))
                                                       (play-minion board player schematic)))})
               (play-minion-card board player schematic)))})


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
