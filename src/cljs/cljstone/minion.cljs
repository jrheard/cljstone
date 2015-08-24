(ns cljstone.minion
  (:require [schema.core :as s]))

(def Minion
  {:name s/Str
   :id s/Str
   :attack s/Int
   :health s/Int})

(def chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5})
(def goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2})
(def magma-rager {:name "Magma Rager" :attack 5 :health 1})

; todo - a minion also has a list of modifier effects
; eg record that it's taken 4 damage, or that its health has been set to 1, or that its attack is buffed by 1, etc
; todo jesus how do you implement dire wolf alpha
