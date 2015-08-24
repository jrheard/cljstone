(ns cljstone.board
  (:require [schema.core :as s]
            [cljstone.hero :as hero])
  (:use [cljstone.minion :only [MinionSchematic make-minion]]))

(def BoardHalf
  {:index s/Int
   :hero hero/Hero
   :minions [s/Str]})

(def Character
  {:id s/Str
   :health s/Int
   :attack s/Int
   s/Any s/Any})

(def Board
  {:half-1 BoardHalf
   :half-2 BoardHalf
   :characters-by-id {s/Str Character}})

(s/defn make-board :- Board
  [hero-1 :- hero/Hero
   hero-2 :- hero/Hero]
  {:half-1 {:index 0 :hero hero-1 :minions []}
   :half-2 {:index 1 :hero hero-2 :minions []}
   :characters-by-id {}})

(s/defn summon-minion :- Board
  [board :- Board
   ; TODO validate it's one of :half-1, :half-2
   which-board-half :- s/Keyword
   schematic :- MinionSchematic]
  (let [minion (make-minion schematic)
        new-characters-by-id (assoc (:characters-by-id board)
                                    (:id minion)
                                    minion)
        new-minions (conj (:minions (which-board-half board))
                             (:id minion))]
    (assoc board
           :characters-by-id new-characters-by-id
           which-board-half (assoc (which-board-half board)
                                   :minions new-minions))))
