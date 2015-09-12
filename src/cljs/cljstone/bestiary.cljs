(ns cljstone.bestiary
  (:require [schema.core :as s])
  (:use [cljstone.board :only [add-modifier-to-character]]))

(def all-minions
  {:wisp {:name "Wisp" :attack 1 :health 1}
   :shieldbearer {:name "Shieldbearer" :attack 0 :health 4}
   :goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2}
   :bloodfen-raptor {:name "Bloodfen Raptor" :attack 3 :health 2}
   :river-crocilisk {:name "River Crocilisk" :attack 2 :health 3}
   :shattered-sun {:name "Shattered Sun Cleric" :attack 3 :health 2
                   :battlecry-targeting-fn (fn [board player]
                                             (get-in board [player :minions]))
                   :battlecry (fn [board target-minion-id]
                                (add-modifier-to-character board
                                                           target-minion-id
                                                           {:type :buff :name "Shattered Sun" :effect {:base-health 1 :base-attack 1}}))}
   :magma-rager {:name "Magma Rager" :attack 5 :health 1}
   :chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5}
   :oasis-snapjaw {:name "Oasis Snapjaw" :attack 2 :health 7}
   :boulderfist-ogre {:name "Boulderfist Ogre" :attack 6 :health 7}
   :war-golem {:name "War Golem" :attack 7 :health 7}})


; TODO - minion types like :beast, :dragon, :mech
