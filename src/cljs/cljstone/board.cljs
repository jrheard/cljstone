(ns cljstone.board
  (:require [schema.core :as s]
            [cljstone.hero :as hero])
  (:use [cljstone.minion :only [MinionSchematic make-minion]]))

(def BoardHalf
  {:index s/Int
   :hero hero/Hero
   :minion-ids [s/Str]})

(def Character
  {:id s/Str
   :health s/Int
   :attack s/Int
   s/Any s/Any})

(def Board
  {:half-1 BoardHalf
   :half-2 BoardHalf
   ; hm - if we made this next field an atom, we could attach watchers to it
   ; might be an interesting way of powering events
   ; doesn't cover everything though (blessing of wisdom)
   :characters-by-id {s/Str Character}})

(s/defn make-board :- Board
  [hero-1 :- hero/Hero
   hero-2 :- hero/Hero]
  {:half-1 {:index 0 :hero hero-1 :minion-ids []}
   :half-2 {:index 1 :hero hero-2 :minion-ids []}
   :characters-by-id {}})

(s/defn summon-minion :- Board
  [board :- Board
   which-board-half :- (s/enum :half-1 :half-2)
   schematic :- MinionSchematic]
  (let [minion (make-minion schematic)]
    (-> board
        (update-in [:characters-by-id] assoc (:id minion) minion)
        (update-in [which-board-half :minion-ids] conj (:id minion)))))
