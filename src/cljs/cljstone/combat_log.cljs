(ns cljstone.combat-log
  (:require [schema.core :as s])
  (:use [cljstone.board :only [Board]]
        [cljstone.character :only [Character CharacterModifier]]))

(def next-log-entry-id (atom 0))

(defn get-next-log-entry-id []
  (let [id-to-return @next-log-entry-id]
    (swap! next-log-entry-id inc)
    id-to-return))

(s/defn log-an-item :- Board
  [board :- Board
   modifier :- CharacterModifier
   source :- (s/maybe Character)
   target :- Character]
  (update-in board
             [:combat-log]
             conj
             {:modifier modifier
              :id (get-next-log-entry-id)
              :source source
              :target target}))
