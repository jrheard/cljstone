(ns cljstone.character
  (:require [schema.core :as s]))

; Schemas

(s/defschema Player (s/enum :player-1 :player-2))

(s/defschema CharacterEffect
  {(s/optional-key :health) s/Int
   (s/optional-key :attack) s/Int})

(s/defschema CharacterModifier
  {:type (s/enum :attack :buff)
   :name (s/maybe s/Str)
   :effect CharacterEffect})

(s/defschema Character
  {:id s/Int
   :base-health (s/conditional #(>= % 0) s/Int)
   :base-attack (s/conditional #(>= % 0) s/Int)
   :modifiers [CharacterModifier]
   s/Any s/Any})

; IDs

(def next-character-id (atom 0))

(defn get-next-character-id []
  (let [id-to-return @next-character-id]
    (swap! next-character-id inc)
    id-to-return))
