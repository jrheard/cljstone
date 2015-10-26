(ns cljstone.spell
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card CardClass remove-card-from-list]]
        [cljstone.combat :only [cause-damage get-enemy-characters get-enemy-minions]]
        [cljstone.utils :only [get-next-id]]))

(s/defschema Spell
  {:name s/Str
   :mana-cost s/Int
   (s/optional-key :castable?) s/Any ; (Board, Player) -> Bool
   (s/optional-key :get-targets) s/Any ; (Board, Player) -> [Character]
   :effect s/Any
   ; if :get-targets exists, :effect will be a function from (Board, target-character) -> Board
   ; if :get-targets does not exist, :effect will be a function from (Board, caster) -> Board
   :class CardClass})

(s/defn spell->card :- Card
  [spell :- Spell]
  (assoc (into {:type :spell
                :id (get-next-id)}
               spell)
         :effect
         ; TODO actually test this logic.
         (fn [board player card]
           (if-let [targeting-fn (:get-targets card)]
             (assoc board :mode {:type :targeting
                                 :targets (targeting-fn board player)
                                 :continuation (fn [board target-character-id]
                                                 (-> board
                                                     (assoc :mode {:type :default})
                                                     (#((spell :effect) % target-character-id))
                                                     (update-in [player :mana-modifiers] conj (- (:mana-cost card)))
                                                     (update-in [player :hand] remove-card-from-list card)))})
             (-> board
                 ((spell :effect) player)
                 (update-in [player :mana-modifiers] conj (- (:mana-cost spell)))
                 (update-in [player :hand] remove-card-from-list card))))))
