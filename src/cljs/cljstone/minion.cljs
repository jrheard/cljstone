(ns cljstone.minion
  (:require [cljs-uuid-utils.core :as uuid]
            [schema.core :as s]))

(def MinionSchematic
  {:name s/Str
   :attack s/Int
   :health s/Int})

(def Minion
  (assoc MinionSchematic :id s/Str))

; Schematics

(def chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5})
(def goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2})
(def magma-rager {:name "Magma Rager" :attack 5 :health 1})

(s/defn make-minion :- Minion
  [schematic :- MinionSchematic]
  (assoc schematic
         :id
         (uuid/uuid-string (uuid/make-random-uuid))))

; todo - a minion also has a list of modifier effects
; eg record that it's taken 4 damage, or that its health has been set to 1, or that its attack is buffed by 1, etc
; todo jesus how do you implement dire wolf alpha
