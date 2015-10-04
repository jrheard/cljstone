(ns cljstone.bestiary
  (:require [schema.core :as s])
  (:use [cljstone.board :only [add-modifier-to-character]]))

(def all-minions
  {:wisp {:name "Wisp" :attack 1 :health 1 :mana-cost 0}
   :shieldbearer {:name "Shieldbearer" :attack 0 :health 4 :mana-cost 1}
   :goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2 :mana-cost 1}
   :bloodfen-raptor {:name "Bloodfen Raptor" :attack 3 :health 2 :mana-cost 2}
   :river-crocilisk {:name "River Crocilisk" :attack 2 :health 3 :mana-cost 2}
   :shattered-sun {:name "Shattered Sun Cleric" :attack 3 :health 2 :mana-cost 3
                   ; XXXX why ids? why make a set?
                   ; TODO - look into whether targeting functions should return ids or Characters
                   ; ok right. it has to be a set so that contains? will work. duh.
                   :battlecry-targeting-fn (fn [board player]
                                             (apply hash-set (map :id (get-in board [player :minions]))))
                   :battlecry (fn [board target-minion-id]
                                (add-modifier-to-character board
                                                           target-minion-id
                                                           {:type :buff :name "Shattered Sun" :effect {:base-health 1 :base-attack 1}}))}
   :magma-rager {:name "Magma Rager" :attack 5 :health 1 :mana-cost 3}
   :chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5 :mana-cost 4}
   :senjin-shieldmasta {:name "Sen'Jin Shieldmasta" :attack 3 :health 5 :mana-cost 4 :modifiers [{:type :buff :effect {:taunt true}}]}
   :oasis-snapjaw {:name "Oasis Snapjaw" :attack 2 :health 7 :mana-cost 4}
   :boulderfist-ogre {:name "Boulderfist Ogre" :attack 6 :health 7 :mana-cost 6}
   :war-golem {:name "War Golem" :attack 7 :health 7 :mana-cost 7}})


; TODO - minion types like :beast, :dragon, :mech
