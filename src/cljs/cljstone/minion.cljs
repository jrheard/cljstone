(ns cljstone.minion
  (:require [cljs-uuid-utils.core :as uuid]
            [schema.core :as s])
  (:use [clojure.set :only [rename-keys]])
  )

(def MinionSchematic
  {:name s/Str
   :base-attack s/Int
   :base-health s/Int})

(def Modifier
  {:type s/Keyword
   ; :type will have values like :buff, :attack - what about event-y things, like on-attack-fn?
   :name (s/maybe s/Str)
   ; TODO - figure out keys/values of effects dict
   ; examples will include {:attack 4}, {:health -3}, etc - but what about stuff like blessing of wisdom?
   :effects {s/Any s/Any}})

; TODO - add notion of "base attack", "base health"
; for use in situations like eg stormwind champion, dire wolf + reversing switch, etc
; they're distinct from "computed attack" and "computed health" - those concepts will have to become functions from minion -> Int
; hm - how do you implement silencing something that's taken damage and has also had its HP buffed?
; or what about if something's taken damage, had its HP buffed by stormwind champ, and the champ dies?
(def Minion
  {:name s/Str
   :base-attack s/Int
   :base-health s/Int
   :id s/Str
   :modifiers [Modifier]})

; Schematics

(def chillwind-yeti {:name "Chillwind Yeti" :base-attack 4 :base-health 5})
(def goldshire-footman {:name "Goldshire Footman" :base-attack 1 :base-health 2})
(def magma-rager {:name "Magma Rager" :base-attack 5 :base-health 1})

(s/defn make-minion :- Minion
  [schematic :- MinionSchematic]
  (-> schematic
      (assoc :id (uuid/uuid-string (uuid/make-random-uuid)))
      (assoc :modifiers [])))

; todo jesus how do you implement dire wolf alpha
; i guess you just add a +1 attack modifier to each of the two adjacent minions, and add a -1 when the wolf dies

(s/defn get-health :- s/Int
  [minion :- Minion]
  (:base-health minion))

(s/defn get-attack :- s/Int
  [minion :- Minion]
  (:base-attack minion))
