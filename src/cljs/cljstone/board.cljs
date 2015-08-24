(ns cljstone.board
  (:require [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]))

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
   ; characters-by-id is an atom mapping str -> Character; schema doesn't support atoms, so we use s/Any here
   :characters-by-id s/Any})

(s/defn make-board :- Board
  [hero-1 :- hero/Hero
   hero-2 :- hero/Hero]
  {:half-1 {:index 0 :hero hero-1 :minions []}
   :half-2 {:index 1 :hero hero-2 :minions []}
   :characters-by-id (atom {})})
