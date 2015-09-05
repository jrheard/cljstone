(ns cljstone.character
  (:require [schema.core :as s]))

; Schemas

(def Player (s/enum :player-1 :player-2))

(def CharacterEffect
  {(s/optional-key :health) s/Int
   (s/optional-key :attack) s/Int})

(def CharacterModifier
  {:type (s/enum :attack :buff)
   :name (s/maybe s/Str)
   :effect CharacterEffect})

(def Character
  {:id s/Int
   :base-health s/Int
   :base-attack s/Int
   :modifiers [CharacterModifier]
   s/Any s/Any})

; IDs

(def next-character-id (atom 0))

(defn get-next-character-id []
  (let [id-to-return @next-character-id]
    (swap! next-character-id inc)
    id-to-return))

(js/console.log "CHARACTER")
