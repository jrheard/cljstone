(ns cljstone.bestiary
  (:require [schema.core :as s])
  (:use [cljstone.board :only [add-modifier-to-character]]
        [plumbing.core :only [safe-get-in]]))

(def taunt {:type :buff :effect {:taunt true}})

(def all-minions
  {:wisp {:name "Wisp" :attack 1 :health 1 :mana-cost 0}
   :shieldbearer {:name "Shieldbearer" :attack 0 :health 4 :mana-cost 1 :modifiers [taunt]}
   :goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2 :mana-cost 1 :modifiers [taunt]}
   :bloodfen-raptor {:name "Bloodfen Raptor" :attack 3 :health 2 :mana-cost 2}
   :river-crocilisk {:name "River Crocilisk" :attack 2 :health 3 :mana-cost 2}
   :shattered-sun {:name "Shattered Sun Cleric" :attack 3 :health 2 :mana-cost 3
                   :battlecry-targeting-fn (fn [board player]
                                             (safe-get-in board [player :minions]))
                   :battlecry (fn [board target-minion]
                                (add-modifier-to-character board
                                                           target-minion
                                                           {:type :buff :name "Shattered Sun" :effect {:base-health 1 :base-attack 1}}))}
   :magma-rager {:name "Magma Rager" :attack 5 :health 1 :mana-cost 3}
   :chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5 :mana-cost 4}
   :senjin-shieldmasta {:name "Sen'Jin Shieldmasta" :attack 3 :health 5 :mana-cost 4 :modifiers [taunt]}
   :oasis-snapjaw {:name "Oasis Snapjaw" :attack 2 :health 7 :mana-cost 4}
   :pit-fighter {:name "Pit Fighter" :attack 5 :health 6 :mana-cost 5}
   :boulderfist-ogre {:name "Boulderfist Ogre" :attack 6 :health 7 :mana-cost 6}
   :war-golem {:name "War Golem" :attack 7 :health 7 :mana-cost 7}})


; TODO - minion types like :beast, :dragon, :mech
