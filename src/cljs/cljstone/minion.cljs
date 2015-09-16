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
         :class (:class schematic :neutral)}
        (dissoc schematic :attack :health :battlecry :battlecry-targeting-fn)))


(s/defn play-minion-card
  [board
   player :- Player
   schematic :- MinionSchematic]
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
   :class (:class schematic :neutral)
   :attack (:attack schematic)
   :health (:health schematic)
   :effect (fn [board player]
             ; TODO implement positioning by associng :mode PositioningMode
             ; *then* do battlecries/targeting if applicable
             ; *then* play the minion at the right position in the board
             ; *then* take the relevant card out of the player's hand
             (if-let [battlecry (:battlecry schematic)]
               (assoc board :mode {:type :targeting
                                   :targets ((schematic :battlecry-targeting-fn) board player)
                                   :continuation (fn [board target-character-id]
                                                   (-> board
                                                       (assoc :mode {:type :default})
                                                       (#((schematic :battlecry) % target-character-id))
                                                       (play-minion-card player schematic)))})
               (play-minion-card board player schematic)))})


; dire wolf alpha only needs to care about on-summon-friendly-minion, on-friendly-minion-death - no other situations cause a dire wolf alpha buff/debuff
; still need to figure out what happens when you silence something that's had its health buffed and has taken 1 damage, though.
; i guess silencing will involve recomputing base attack and health, and so you can figure it out at recompute-because-of-silence time.
; hm.

(s/defn sum-modifiers :- s/Int
  [minion :- Minion
   kw :- s/Keyword]
  (apply + (map (fn [modifier]
                  (kw (modifier :effect) 0))
                (minion :modifiers))))

(s/defn get-base-attack :- s/Int
  [minion :- Minion]
  (+ (:base-attack minion)
     (sum-modifiers minion :base-attack)))

(s/defn get-base-health :- s/Int
  [minion :- Minion]
  (+ (:base-health minion)
     (sum-modifiers minion :base-health)))

(s/defn get-health :- s/Int
  [minion :- Minion]
  (+ (get-base-health minion)
     (sum-modifiers minion :health)))

(s/defn get-attack :- s/Int
  [minion :- Minion]
  (get-base-attack minion))

(s/defn can-attack :- s/Bool
  [minion :- Minion]
  (and (< (minion :attacks-this-turn)
          (minion :attacks-per-turn))
       (> (get-attack minion) 0)))
