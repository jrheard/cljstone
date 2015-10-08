(ns cljstone.bestiary
  (:require [schema.core :as s])
  (:use [cljstone.board :only [add-modifier-to-character]]
        [plumbing.core :only [safe-get-in]]))

(def taunt {:type :buff :effect {:taunt true}})

(def all-minions
  {:wisp {:name "Wisp" :base-attack 1 :base-health 1 :mana-cost 0}
   :shieldbearer {:name "Shieldbearer" :base-attack 0 :base-health 4 :mana-cost 1 :modifiers [taunt]}
   :goldshire-footman {:name "Goldshire Footman" :base-attack 1 :base-health 2 :mana-cost 1 :modifiers [taunt]}
   :voidwalker {:name "Voidwalker", :base-attack 1, :base-health 3, :class :warlock, :mana-cost 1}
   :bloodfen-raptor {:name "Bloodfen Raptor" :base-attack 3 :base-health 2 :mana-cost 2}
   :frostwolf-grunt {:name "Frostwolf Grunt", :base-attack 2, :base-health 2, :mana-cost 2, :modifiers [taunt]}
   :river-crocilisk {:name "River Crocilisk" :base-attack 2 :base-health 3 :mana-cost 2}
   :ironfur-grizzly {:name "Ironfur Grizzly", :base-attack 3, :base-health 3, :mana-cost 3, :modifiers [taunt]}
   :silverback-patriarch {:name "Silverback Patriarch", :base-attack 1, :base-health 4, :mana-cost 3, :modifiers [taunt]}
   :shattered-sun {:name "Shattered Sun Cleric" :base-attack 3 :base-health 2 :mana-cost 3
                   :battlecry-targeting-fn (fn [board player]
                                             (safe-get-in board [player :minions]))
                   :battlecry (fn [board target-minion]
                                (add-modifier-to-character board
                                                           target-minion
                                                           {:type :buff :name "Shattered Sun" :effect {:base-base-health 1 :base-attack 1}}))}
   :magma-rager {:name "Magma Rager" :base-attack 5 :base-health 1 :mana-cost 3}
   :booty-bay {:name "Booty Bay Bodyguard", :base-attack 5, :base-health 4, :mana-cost 5, :modifiers [taunt]}
   :chillwind-yeti {:name "Chillwind Yeti" :base-attack 4 :base-health 5 :mana-cost 4}
   :senjin-shieldmasta {:name "Sen'Jin Shieldmasta" :base-attack 3 :base-health 5 :mana-cost 4 :modifiers [taunt]}
   :oasis-snapjaw {:name "Oasis Snapjaw" :base-attack 2 :base-health 7 :mana-cost 4}
   :pit-fighter {:name "Pit Fighter" :base-attack 5 :base-health 6 :mana-cost 5}
   :lord-of-the-arena {:name "Lord of the Arena", :base-attack 6, :base-health 5, :mana-cost 6, :modifiers [taunt]}
   :boulderfist-ogre {:name "Boulderfist Ogre" :base-attack 6 :base-health 7 :mana-cost 6}
   :war-golem {:name "War Golem" :base-attack 7 :base-health 7 :mana-cost 7}
   :ironbark-protector {:name "Ironbark Protector", :base-attack 8, :base-health 8, :class :druid, :mana-cost 8, :modifiers [taunt]}})

; TODO - minion types like :beast, :dragon, :mech
