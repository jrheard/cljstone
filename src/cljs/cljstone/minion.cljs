(ns cljstone.minion
  (:require [schema.core :as s]))

(def Minion
  {:name s/Str
   :attack s/Int
   :health s/Int})

(def chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5})
(def goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2})
(def magma-rager {:name "Magma Rager" :attack 5 :health 1})
