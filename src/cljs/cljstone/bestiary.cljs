(ns cljstone.bestiary
  (:require [schema.core :as s])
  (:use [cljstone.board :only [add-modifier-to-character]]
        [cljstone.combat :only [all-characters cause-damage]]
        [plumbing.core :only [safe-get-in]]))

(def taunt {:type :mechanic :effect {:taunt true}})
(def charge {:type :mechanic :effect {:charge true}})

(def playable-minions
  {:wisp {:name "Wisp" :base-attack 1 :base-health 1 :mana-cost 0}
   ; TOOD - when a minion's battlecry has put us into targeting mode, highlight the minion's card even though it's still in the player's hand
   :elven-archer {:name "Elven Archer", :base-attack 1, :base-health 1, :mana-cost 1,
                  :battlecry {:get-targets (fn [board player]
                                             (all-characters board))
                              :effect (fn [board target-character]
                                        (cause-damage board
                                                      target-character
                                                      {:type :attack
                                                       :name "Elven Archer"
                                                       :effect {:health -1}}))}}
   :murloc-raider {:name "Murloc Raider", :base-attack 2, :base-health 1, :mana-cost 1}
   :shieldbearer {:name "Shieldbearer" :base-attack 0 :base-health 4 :mana-cost 1 :modifiers [taunt]}
   :goldshire-footman {:name "Goldshire Footman" :base-attack 1 :base-health 2 :mana-cost 1 :modifiers [taunt]}
   :stonetusk-boar {:name "Stonetusk Boar", :base-attack 1, :base-health 1, :mana-cost 1, :modifiers [charge]}
   :voidwalker {:name "Voidwalker", :base-attack 1, :base-health 3, :class :warlock, :mana-cost 1, :modifiers [taunt]}
   :bloodfen-raptor {:name "Bloodfen Raptor" :base-attack 3 :base-health 2 :mana-cost 2}
   :bluegill-warrior {:name "Bluegill Warrior", :base-attack 2, :base-health 1, :mana-cost 2, :modifiers [charge]}
   :frostwolf-grunt {:name "Frostwolf Grunt", :base-attack 2, :base-health 2, :mana-cost 2, :modifiers [taunt]}
   :river-crocilisk {:name "River Crocilisk" :base-attack 2 :base-health 3 :mana-cost 2}
   :ironforge-rifleman {:name "Ironforge Rifleman", :base-attack 2, :base-health 2, :mana-cost 3,
                        :battlecry {:get-targets (fn [board player]
                                                   (all-characters board))
                                    :effect (fn [board target-character]
                                              (cause-damage board
                                                            target-character
                                                            {:type :attack
                                                             :name "Ironforge Rifleman"
                                                             :effect {:health -1}}))}}
   :ironfur-grizzly {:name "Ironfur Grizzly", :base-attack 3, :base-health 3, :mana-cost 3, :modifiers [taunt]}
   :silverback-patriarch {:name "Silverback Patriarch", :base-attack 1, :base-health 4, :mana-cost 3, :modifiers [taunt]}
   :shattered-sun {:name "Shattered Sun Cleric", :base-attack 3, :base-health 2, :mana-cost 3,
                   :battlecry {:get-targets (fn [board player]
                                             (safe-get-in board [player :minions]))
                               :effect (fn [board target-minion]
                                (add-modifier-to-character board
                                                           target-minion
                                                           {:type :enchantment :name "Shattered Sun" :effect {:base-health 1 :base-attack 1}}))}}
   :magma-rager {:name "Magma Rager" :base-attack 5 :base-health 1 :mana-cost 3}
   :wolfrider {:name "Wolfrider", :base-attack 3, :base-health 1, :mana-cost 3, :modifiers [charge]}
   :chillwind-yeti {:name "Chillwind Yeti" :base-attack 4 :base-health 5 :mana-cost 4}
   :korkron-elite {:name "Kor'kron Elite", :base-attack 4, :base-health 3, :class :warrior, :mana-cost 4, :modifiers [charge]}
   :oasis-snapjaw {:name "Oasis Snapjaw" :base-attack 2 :base-health 7 :mana-cost 4}
   :senjin-shieldmasta {:name "Sen'Jin Shieldmasta" :base-attack 3 :base-health 5 :mana-cost 4 :modifiers [taunt]}
   :stormwind-knight {:name "Stormwind Knight", :base-attack 2, :base-health 5, :mana-cost 4, :modifiers [charge]}
   :booty-bay {:name "Booty Bay Bodyguard", :base-attack 5, :base-health 4, :mana-cost 5, :modifiers [taunt]}
   :pit-fighter {:name "Pit Fighter" :base-attack 5 :base-health 6 :mana-cost 5}
   :fire-elemental {:name "Fire Elemental", :base-attack 6, :base-health 5, :class :shaman, :mana-cost 6,
                    :battlecry {:get-targets (fn [board player]
                                             (all-characters board))
                                :effect (fn [board target-character]
                                          (cause-damage board
                                                        target-character
                                                        {:type :attack
                                                         :name "Fire Elemental"
                                                         :effect {:health -3}}))}}
   :lord-of-the-arena {:name "Lord of the Arena", :base-attack 6, :base-health 5, :mana-cost 6, :modifiers [taunt]}
   :boulderfist-ogre {:name "Boulderfist Ogre" :base-attack 6 :base-health 7 :mana-cost 6}
   :reckless-rocketeer {:name "Reckless Rocketeer", :base-attack 5, :base-health 2, :mana-cost 6, :modifiers [charge]}
   :core-hound {:name "Core Hound", :base-attack 9, :base-health 5, :mana-cost 7}
   :war-golem {:name "War Golem" :base-attack 7 :base-health 7 :mana-cost 7}
   :ironbark-protector {:name "Ironbark Protector", :base-attack 8, :base-health 8, :class :druid, :mana-cost 8, :modifiers [taunt]}})

; TODO - minion types like :beast, :dragon, :mech


(comment
  unconverted minions from basic card set below
   ; TODO - implement a minion whose battlecry takes no targets (nightblade!!!!)

   {:name "Acidic Swamp Ooze", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Destroy your opponent's weapon.", :base-attack 3, :base-health 2, :mana-cost 2}
   {:name "Archmage", :mechanics ["Spellpower"], :text "<b>Spell Damage +1</b>", :base-attack 4, :base-health 7, :mana-cost 6}
   {:name "Dalaran Mage", :mechanics ["Spellpower"], :text "<b>Spell Damage +1</b>", :base-attack 1, :base-health 4, :mana-cost 3}
   {:name "Darkscale Healer", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Restore 2 Health to all friendly characters.", :base-attack 4, :base-health 5, :mana-cost 5}
   {:name "Dragonling Mechanic", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Summon a 2/1 Mechanical Dragonling.", :base-attack 2, :base-health 4, :mana-cost 4}
   {:name "Dread Infernal", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Deal 1 damage to ALL other characters.", :base-attack 6, :base-health 6, :class "Warlock", :mana-cost 6}
   {:name "Flametongue Totem", :mechanics ["AdjacentBuff" "Aura"], :text "Adjacent minions have +2 Attack.", :base-attack 0, :base-health 3, :class "Shaman", :mana-cost 2}
   {:name "Frostwolf Warlord", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Gain +1/+1 for each other friendly minion on the battlefield.", :base-attack 4, :base-health 4, :mana-cost 5}
   {:name "Gnomish Inventor", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Draw a card.", :base-attack 2, :base-health 4, :mana-cost 4}
   {:name "Grimscale Oracle", :text "ALL other Murlocs have +1 Attack.", :base-attack 1, :base-health 1, :mana-cost 1}
   {:name "Guardian of Kings", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Restore 6 Health to your hero.", :base-attack 5, :base-health 6, :class "Paladin", :mana-cost 7}
  {:name "Gurubashi Berserker", :text "Whenever this minion takes damage, gain +3 Attack.", :base-attack 2, :base-health 7, :mana-cost 5}
  {:name "Houndmaster", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Give a friendly Beast +2/+2 and <b>Taunt</b>.", :base-attack 4, :base-health 3, :class "Hunter", :mana-cost 4}
  {:name "Kobold Geomancer", :mechanics ["Spellpower"], :text "<b>Spell Damage +1</b>", :base-attack 2, :base-health 2, :mana-cost 2}
  {:name "Murloc Tidehunter", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Summon a 1/1 Murloc Scout.", :base-attack 2, :base-health 1, :mana-cost 2}
  {:name "Nightblade", :mechanics ["Battlecry"], :text "<b>Battlecry: </b>Deal 3 damage to the enemy hero.", :base-attack 4, :base-health 4, :mana-cost 5}
  {:name "Northshire Cleric", :mechanics ["HealTarget"], :text "Whenever a minion is healed, draw a card.", :base-attack 1, :base-health 3, :class "Priest", :mana-cost 1}
  {:name "Novice Engineer", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Draw a card.", :base-attack 1, :base-health 1, :mana-cost 2}
  {:name "Ogre Magi", :mechanics ["Spellpower"], :text "<b>Spell Damage +1</b>", :base-attack 4, :base-health 4, :mana-cost 4}
  {:name "Raid Leader", :mechanics ["Aura"], :text "Your other minions have +1 Attack.", :base-attack 2, :base-health 2, :mana-cost 3}
  {:name "Razorfen Hunter", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Summon a 1/1 Boar.", :base-attack 2, :base-health 3, :mana-cost 3}
  {:name "Starving Buzzard", :text "Whenever you summon a Beast, draw a card.", :base-attack 3, :base-health 2, :class "Hunter", :mana-cost 5}
  {:name "Stormpike Commando", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Deal 2 damage.", :base-attack 4, :base-health 2, :mana-cost 5}
  {:name "Stormwind Champion", :mechanics ["Aura"], :text "Your other minions have +1/+1.", :base-attack 6, :base-health 6, :mana-cost 7}
  {:name "Succubus", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Discard a random card.", :base-attack 4, :base-health 3, :class "Warlock", :mana-cost 2}
  {:name "Timber Wolf", :mechanics ["Aura"], :text "Your other Beasts have +1 Attack.", :base-attack 1, :base-health 1, :class "Hunter", :mana-cost 1}
  {:name "Tundra Rhino", :text "Your Beasts have <b>Charge</b>.", :base-attack 2, :base-health 5, :class "Hunter", :mana-cost 5}
  {:name "Voodoo Doctor", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Restore 2 Health.", :base-attack 2, :base-health 1, :mana-cost 1}
  {:name "Warsong Commander", :text "Whenever you summon a minion with 3 or less Attack, give it <b>Charge</b>.", :base-attack 2, :base-health 3, :class "Warrior", :mana-cost 3}
  {:name "Water Elemental", :mechanics ["Freeze"], :text "<b>Freeze</b> any character damaged by this minion.", :base-attack 3, :base-health 6, :class "Mage", :mana-cost 4}
  {:name "Windspeaker", :mechanics ["Battlecry"], :text "<b>Battlecry:</b> Give a friendly minion <b>Windfury</b>.", :base-attack 3, :base-health 3, :class "Shaman", :mana-cost 4}
  )

(def all-minions playable-minions)
