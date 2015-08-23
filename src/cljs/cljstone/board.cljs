(ns cljstone.board
  (:require [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]))

(def BoardHalf
  {:index s/Int
   :hero hero/Hero
   :minions [minion/Minion]})
