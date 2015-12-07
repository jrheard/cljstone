(ns cljstone.spellbook
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board draw-a-card path-to-character]]
        [cljstone.character :only [Character CharacterModifier Player other-player]]
        [cljstone.combat :only [all-characters get-all-minions cause-damage get-enemy-characters get-enemy-minions]]
        [plumbing.core :only [safe-get safe-get-in]]))

(s/defn character-modifiers-path :- [s/Any]
  [board :- Board
   character :- Character]
  (conj (path-to-character board (safe-get character :id))
                   :modifiers))

(s/defn add-modifier-to-character :- Board
  [board :- Board
   character :- Character
   modifier :- CharacterModifier]
  (update-in board
             (character-modifiers-path board character)
             conj
             modifier))

(def all-spells
  {:hunters-mark {:name "Hunter's Mark", :class :hunter, :mana-cost 0,
                  :get-targets (fn [board caster]
                                 (get-all-minions board))
                  :effect (fn [board target-character caster]
                            (-> board
                                ; TODO - split this out into a function, clear-modifiers-with-effect or something
                                (update-in (character-modifiers-path board target-character)
                                           (fn [modifiers]
                                             (filter (s/fn :- s/Bool
                                                       [modifier :- CharacterModifier]
                                                       (boolean
                                                         (or (= (modifier :type) :aura)
                                                             (not (contains? (modifier :effect) :health)))))
                                                     modifiers)))
                                (add-modifier-to-character target-character {:type :enchantment
                                                                             :name "Hunter's Mark"
                                                                             :effect {:base-health 1}})))}
   :moonfire {:name "Moonfire", :class :druid, :mana-cost 0,
              :get-targets (fn [board caster]
                             (all-characters board))
              :effect (fn [board target-character caster]
                        (cause-damage board
                                        target-character
                                        {:type :damage-spell
                                         :name "Moonfire"
                                         :effect {:health -1}}))}
   :arcane-missiles {:name "Arcane Missiles"
                     :mana-cost 1
                     :class :mage
                     :effect (fn [board caster]
                               (nth (iterate (fn [board]
                                              (cause-damage board
                                                            (rand-nth (get-enemy-characters board caster))
                                                            {:type :damage-spell
                                                             :name "Arcane Missiles"
                                                             :effect {:health -1}}))
                                            board)
                                    3))}
   :holy-smite {:name "Holy Smite"
                :mana-cost 1
                :class :priest
                :get-targets (fn [board caster]
                               (all-characters board))
                :effect (fn [board target-character caster]
                          (cause-damage board
                                        target-character
                                        {:type :damage-spell
                                         :name "Holy Smite"
                                         :effect {:health -2}}))}
   :humility {:name "Humility", :class :paladin, :mana-cost 1,
              :get-targets (fn [board caster]
                             (get-all-minions board))
              :effect (fn [board target-character caster]
                        ; XXX TODO will probably have to clear all preexisting attack modifiers.
                        ; what's an example of a situation where this implementation of humility wouldn't behave correctly?
                        ; what if the minion's been buffed by lance carrier? then we'd be fucked.
                        ; so clear all preexisting attack modifiers first.
                        (add-modifier-to-character board target-character {:type :enchantment
                                                                           :name "Humility"
                                                                           :effect {:base-attack 1}}))}
  :flamecannon {:name "Flamecannon"
                :mana-cost 2
                :class :mage
                :castable? (s/fn :- s/Bool [board :- Board caster :- Player]
                             (> (count (get-enemy-minions board caster))
                                0))
                :effect (s/fn [board :- Board caster :- Player]
                          (when-let [minions (get-enemy-minions board caster)]
                            (cause-damage board
                                          (rand-nth minions)
                                          {:type :damage-spell
                                           :name "Flamecannon"
                                           :effect {:health -4}})))}
   :shiv {:name "Shiv" :class :rogue :mana-cost 2
          :get-targets (fn [board caster]
                         (all-characters board))
          :effect (fn [board target-character caster]
                    (-> board
                        (cause-damage target-character
                                      {:type :damage-spell
                                       :name "Shiv"
                                       :effect {:health -1}})
                        (draw-a-card caster)))}
   :arcane-intellect {:name "Arcane Intellect"
                      :mana-cost 3
                      :class :mage
                      :effect (fn [board caster]
                                (-> board
                                    (draw-a-card caster)
                                    (draw-a-card caster)))}
   :fireball {:name "Fireball"
              :mana-cost 4
              :class :mage
              :get-targets (fn [board caster]
                             (all-characters board))
              :effect (fn [board target-character caster]
                        (cause-damage board
                                      target-character
                                      {:type :damage-spell
                                       :name "Fireball"
                                       :effect {:health -6}}))}
   :sprint {:name "Sprint"
            :mana-cost 7
            :class :rogue
            :effect (fn [board caster]
                      (-> board
                          (draw-a-card caster)
                          (draw-a-card caster)
                          (draw-a-card caster)
                          (draw-a-card caster)))}})

(comment
  ; unconverted spells below


 {:name "Ancestral Healing", :text "Restore a minion to full Health and give it <b>Taunt</b>.", :class :shaman, :mana-cost 0}
 {:name "Animal Companion", :text "Summon a random Beast Companion.", :class :hunter, :mana-cost 3}
 {:name "Arcane Explosion", :text "Deal $1 damage to all enemy minions.", :class :mage, :mana-cost 2}
 {:name "Arcane Shot", :text "Deal $2 damage.", :class :hunter, :mana-cost 1}
 {:name "Assassinate", :text "Destroy an enemy minion.", :class :rogue, :mana-cost 5}
 {:name "Backstab", :text "Deal $2 damage to an undamaged minion.", :class :rogue, :mana-cost 0}
 {:name "Blessing of Kings", :text "Give a minion +4/+4. <i>(+4 Attack/+4 Health)</i>", :class :paladin, :mana-cost 4}
 {:name "Blessing of Might", :text "Give a minion +3 Attack.", :class :paladin, :mana-cost 1}
 {:name "Bloodlust", :text "Give your minions +3 Attack this turn.", :class :shaman, :mana-cost 5}
 {:name "Charge", :text "Give a friendly minion +2 Attack and <b>Charge</b>.", :class :warrior, :mana-cost 3}
 {:name "Claw", :text "Give your hero +2 Attack this turn and 2 Armor.", :class :druid, :mana-cost 1}
 {:name "Cleave", :text "Deal $2 damage to two random enemy minions.", :class :warrior, :mana-cost 2}
 {:name "Consecration", :text "Deal $2 damage to all enemies.", :class :paladin, :mana-cost 4}
 {:name "Corruption", :text "Choose an enemy minion. At the start of your turn, destroy it.", :class :warlock, :mana-cost 1}
 {:name "Deadly Poison", :text "Give your weapon +2 Attack.", :class :rogue, :mana-cost 1}
 {:name "Divine Spirit", :text "Double a minion's Health.", :class :priest, :mana-cost 2}
 {:name "Drain Life", :text "Deal $2 damage. Restore #2 Health to your hero.", :class :warlock, :mana-cost 3}
 {:name "Execute", :text "Destroy a damaged enemy minion.", :class :warrior, :mana-cost 1}
 {:name "Fan of Knives", :text "Deal $1 damage to all enemy minions. Draw a card.", :class :rogue, :mana-cost 3}
 {:name "Flamestrike", :text "Deal $4 damage to all enemy minions.", :class :mage, :mana-cost 7}
 {:name "Frost Nova", :mechanics ["Freeze"], :text "<b>Freeze</b> all enemy minions.", :class :mage, :mana-cost 3}
 {:name "Frost Shock", :mechanics ["Freeze"], :text "Deal $1 damage to an enemy character and <b>Freeze</b> it.", :class :shaman, :mana-cost 1}
 {:name "Frostbolt", :mechanics ["Freeze"], :text "Deal $3 damage to a character and <b>Freeze</b> it.", :class :mage, :mana-cost 2}
 {:name "Hammer of Wrath", :text "Deal $3 damage.\nDraw a card.", :class :paladin, :mana-cost 4}
 {:name "Hand of Protection", :text "Give a minion <b>Divine Shield</b>.", :class :paladin, :mana-cost 1}
 {:name "Healing Touch", :text "Restore #8 Health.", :class :druid, :mana-cost 3}
 {:name "Hellfire", :text "Deal $3 damage to ALL characters.", :class :warlock, :mana-cost 4}
 {:name "Heroic Strike", :text "Give your hero +4 Attack this turn.", :class :warrior, :mana-cost 2}
 {:name "Hex", :text "Transform a minion into a 0/1 Frog with <b>Taunt</b>.", :class :shaman, :mana-cost 3}
 {:name "Holy Light", :text "Restore #6 Health.", :class :paladin, :mana-cost 2}
 {:name "Holy Nova", :text "Deal $2 damage to all enemies. Restore #2 Health to all friendly characters.", :class :priest, :mana-cost 5}
 {:name "Holy Smite", :text "Deal $2 damage.", :class :priest, :mana-cost 1}
 {:name "Humility", :text "Change a minion's Attack to 1.", :class :paladin, :mana-cost 1}
 {:name "Innervate", :text "Gain 2 Mana Crystals this turn only.", :class :druid, :mana-cost 0}
 {:name "Kill Command", :text "Deal $3 damage. If you have a Beast, deal $5 damage instead.", :class :hunter, :mana-cost 3}
 {:name "Mark of the Wild", :text "Give a minion <b>Taunt</b> and +2/+2.<i> (+2 Attack/+2 Health)</i>", :class :druid, :mana-cost 2}
 {:name "Mind Blast", :text "Deal $5 damage to the enemy hero.", :class :priest, :mana-cost 2}
 {:name "Mind Control", :text "Take control of an enemy minion.", :class :priest, :mana-cost 10}
 {:name "Mind Vision", :text "Put a copy of a random card in your opponent's hand into your hand.", :class :priest, :mana-cost 1}
 {:name "Mirror Image", :text "Summon two 0/2 minions with <b>Taunt</b>.", :class :mage, :mana-cost 1}
 {:name "Mortal Coil", :text "Deal $1 damage to a minion. If that kills it, draw a card.", :class :warlock, :mana-cost 1}
 {:name "Multi-Shot", :text "Deal $3 damage to two random enemy minions.", :class :hunter, :mana-cost 4}
 {:name "Polymorph", :text "Transform a minion into a 1/1 Sheep.", :class :mage, :mana-cost 4}
 {:name "Power Word: Shield", :text "Give a minion +2 Health.\nDraw a card.", :class :priest, :mana-cost 1}
 {:name "Rockbiter Weapon", :text "Give a friendly character +3 Attack this turn.", :class :shaman, :mana-cost 1}
 {:name "Sacrificial Pact", :text "Destroy a Demon. Restore #5 Health to your hero.", :class :warlock, :mana-cost 0}
 {:name "Sap", :text "Return an enemy minion to your opponent's hand.", :class :rogue, :mana-cost 2}
 {:name "Savage Roar", :text "Give your characters +2 Attack this turn.", :class :druid, :mana-cost 3}
 {:name "Shadow Bolt", :text "Deal $4 damage to a minion.", :class :warlock, :mana-cost 3}
 {:name "Shadow Word: Death", :text "Destroy a minion with an Attack of 5 or more.", :class :priest, :mana-cost 3}
 {:name "Shadow Word: Pain", :text "Destroy a minion with 3 or less Attack.", :class :priest, :mana-cost 2}
 {:name "Shield Block", :text "Gain 5 Armor.\nDraw a card.", :class :warrior, :mana-cost 3}
 {:name "Sinister Strike", :text "Deal $3 damage to the enemy hero.", :class :rogue, :mana-cost 1}
 {:name "Soulfire", :text "Deal $4 damage. Discard a random card.", :class :warlock, :mana-cost 1}
 {:name "Starfire", :text "Deal $5 damage.\nDraw a card.", :class :druid, :mana-cost 6}
 {:name "Swipe", :text "Deal $4 damage to an enemy and $1 damage to all other enemies.", :class :druid, :mana-cost 4}
 {:name "Totemic Might", :text "Give your Totems +2 Health.", :class :shaman, :mana-cost 0}
 {:name "Tracking", :text "Look at the top three cards of your deck. Draw one and discard the others.", :class :hunter, :mana-cost 1}
 {:name "Vanish", :text "Return all minions to their owner's hand.", :class :rogue, :mana-cost 6}
 {:name "Whirlwind", :text "Deal $1 damage to ALL minions.", :class :warrior, :mana-cost 1}
 {:name "Wild Growth", :text "Gain an empty Mana Crystal.", :class :druid, :mana-cost 2}
 {:name "Windfury", :text "Give a minion <b>Windfury</b>.", :class :shaman, :mana-cost 2}
  )
