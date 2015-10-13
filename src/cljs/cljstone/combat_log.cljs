(ns cljstone.combat-log
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board]]
        [cljstone.character :only [Character CharacterModifier]]
        [cljstone.utils :only [get-next-id]]))

(s/defn log-an-item :- Board
  [board :- Board
   modifier :- CharacterModifier
   source :- (s/maybe Character)
   target :- Character]
  (update-in board
             [:combat-log]
             (fn [combat-log]
               (if (< (get-in modifier [:effect :health]) 0)
                 (conj combat-log {:modifier modifier
                                   :id (get-next-id)
                                   :source source
                                   :target target})
                 combat-log))))
