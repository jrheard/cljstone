(ns cljstone.minion
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card CardClass remove-card-from-list]]
        [cljstone.character :only [Player Character CharacterModifier]]
        [cljstone.hero :only [HeroClass]]
        [cljstone.utils :only [get-next-id]]))

; TODO - will have to move this somewhere (character.cljs?) where weapons can use it when we implement weapons
(s/defschema Battlecry
  {(s/optional-key :get-targets) s/Any ; (Board, Player) -> [Character]
   :effect s/Any
   ; if :get-targets exists, :effect will be a function from (Board, target-character) -> Board
   ; if :get-targets does not exist, :effect will be a function from Board -> Board
   })

(s/defschema MinionSchematic
  {:name s/Str
   (s/optional-key :class) HeroClass
   :base-attack s/Int
   :base-health s/Int
   :mana-cost s/Int
   (s/optional-key :battlecry) Battlecry
   (s/optional-key :modifiers) [CharacterModifier]})

(s/defschema Minion
  {:name s/Str
   :class CardClass
   :base-attack s/Int
   :base-health s/Int
   :attacks-this-turn s/Int
   :attacks-per-turn s/Int
   :id s/Int
   :type (s/enum :minion)
   :modifiers [CharacterModifier]})

; hm - how do you implement silencing something that's taken damage and has also had its HP buffed?
; or what about if something's taken damage, had its HP buffed by stormwind champ, and the champ dies?
; what about one-turn attack buffs, like from dark iron dwarf?
; i guess Modifiers will have to have associated :turn-played, :turn-expires?
; yeah, definitely go with :turn-expires.
; also: summoning sickness can be implemented as a one-turn modifier with effect :cant-attack true
; freezing will work similarly

; TODO - on-before-attack for ogre brute, on-after-attack for mistress of pain


(s/defn make-minion :- Minion
  ; TODO - MinionSchematics will eventually have k/v pairs like :on-summon-minion a-fn
  ; and minions will have k/v pairs like :on-summon-minion a-channel;
  ; the machinery that sets up those channels and hooks them up to those functions will live here.
  [schematic :- MinionSchematic
   id :- s/Int]
  (into {:attacks-this-turn 0
         :attacks-per-turn 1 ; TODO - turn into :base-attacks-per-turn, there'll be modifiers that can add 1 to this number (will freezing remove 1 from this number?)
         :id id
         :modifiers []
         :type :minion
         :class (:class schematic :neutral)}
        (dissoc schematic :battlecry :mana-cost)))

(s/defn summon-minion
  [board
   player :- Player
   schematic :- MinionSchematic]
  (let [minion (-> schematic
                   (make-minion (get-next-id))
                   (update-in [:modifiers] (fn [modifiers]
                                             (if (some #(get-in % [:effect :charge]) modifiers)
                                               modifiers
                                               (conj modifiers {:type :mechanic
                                                                :name "Summoning Sickness"
                                                                :turn-begins (:turn board)
                                                                :turn-ends (+ 2 (:turn board))
                                                                :effect {:cant-attack true}})))))]
    (-> board
        (update-in [player :minions] conj minion)
        (update-in [player :mana-modifiers] conj (- (:mana-cost schematic))))))

(s/defn minion-card-effect
  [board
   player :- Player
   card :- Card
   schematic :- MinionSchematic]
   ; TODO implement positioning by associng :mode PositioningMode
   ; *then* do battlecries/targeting if applicable
   ; *then* play the minion at the right position in the board
   ; *then* take the relevant card out of the player's hand
   (if-let [battlecry (:battlecry schematic)]
     ; TODO - handle a few cases we don't currently handle
     ; 1) battlecry has no :get-targets function (in which case just go straight to the :effect function)
     ; 2) battlecry *has* a :get-targets, but it returns [] - in which case we should behave the same as in 1)
     ; right now if you try to play a shattered sun cleric and have no minions, the game crashes
     (assoc board :mode {:type :targeting
                         :targets ((battlecry :get-targets) board player)
                         :continuation (fn [board target-character-id]
                                         (-> board
                                             (assoc :mode {:type :default})
                                             (update-in [player :hand] remove-card-from-list card)
                                             (#((battlecry :effect) % target-character-id))
                                             (summon-minion player schematic)))})
     (-> board
         (update-in [player :hand] remove-card-from-list card)
         (summon-minion player schematic))))

(s/defn minion-schematic->card :- Card
  [schematic :- MinionSchematic]
  {:type :minion
   :name (:name schematic)
   :mana-cost (:mana-cost schematic)
   :id (get-next-id)
   :class (:class schematic :neutral)
   :base-attack (:base-attack schematic)
   :base-health (:base-health schematic)
   :effect (fn [board player card]
             (minion-card-effect board player card schematic))})

; dire wolf alpha only needs to care about on-summon-friendly-minion, on-friendly-minion-death - no other situations cause a dire wolf alpha buff/debuff
; still need to figure out what happens when you silence something that's had its health buffed and has taken 1 damage, though.
; i guess silencing will involve recomputing base attack and health, and so you can figure it out at recompute-because-of-silence time.
; hm.
